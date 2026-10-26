package org.example.itemcounting.business.service;

import lombok.RequiredArgsConstructor;
import org.example.itemcounting.entity.*;
import org.example.itemcounting.enums.InvoiceStatus;
import org.example.itemcounting.event.ReplenishmentRequested;
import org.example.itemcounting.repository.*;
import org.example.itemcounting.rest.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final int THREAD_COUNT = 5;

    private final InvoiceRepository invoiceRepository;
    private final CurrentUserService currentUserService;
    private final StockRepository stockRepository;
    private final WarehouseRepository warehouseRepository;
    private final ReplenishmentRequestRepository requestRepository;
    private final ProductRepository productRepository;
    private final OutboxEventService outboxEventService;

    @Transactional(readOnly = true)
    public List<InvoiceItem> getInvoiceItem(InvoiceStatus status) {
        return invoiceRepository.findByStatusAndFromWarehouseId(status, currentUserService.getWarehouseId()).stream().map(Invoice::getItems).flatMap(Collection::stream).toList();
    }

    @Transactional(readOnly = true)
    public List<WaitOrderItemDTO> getWaitItems() {
        List<InvoiceItem> waitItems = getInvoiceItem(InvoiceStatus.WAIT);

        if (waitItems.isEmpty()) {
            return List.of();
        }

        List<WaitItemData> items = waitItems.stream().map(this::toWaitItemData).toList();

        List<List<WaitItemData>> parts = splitIntoParts(items);

        Map<Long, BigDecimal> stockMap = getStockMap();

        return processParts(parts, stockMap).stream().sorted(Comparator.comparing(WaitOrderItemDTO::productName)).toList();
    }

    // получение остатков товаров
    private Map<Long, BigDecimal> getStockMap() {
        return stockRepository.findByWarehouseId(currentUserService.getWarehouseId()).stream().collect(Collectors.toMap(stock -> stock.getProduct().getId(), Stock::getQuantity, BigDecimal::add));
    }

    // преобразование сущности в DTO
    private WaitItemData toWaitItemData(InvoiceItem item) {
        Product product = item.getProduct();

        return new WaitItemData(product.getId(), product.getName(), product.getSku(), product.getUnit(), product.getMinStockLevel(), item.getQuantity());
    }

    // распределение между потоками
    private List<List<WaitItemData>> splitIntoParts(List<WaitItemData> items) {
        List<List<WaitItemData>> parts = new ArrayList<>();

        for (int i = 0; i < THREAD_COUNT; i++) {
            parts.add(new ArrayList<>());
        }

        for (WaitItemData item : items) {
            int index = Math.floorMod(item.productId(), THREAD_COUNT);
            parts.get(index).add(item);
        }

        return parts;
    }

    // запуск обработки
    private List<WaitOrderItemDTO> processParts(List<List<WaitItemData>> parts, Map<Long, BigDecimal> stockMap) {
        List<Callable<List<WaitOrderItemDTO>>> tasks = new ArrayList<>();

        for (List<WaitItemData> part : parts) {
            if (!part.isEmpty()) {
                tasks.add(() -> processPart(part, stockMap));
            }
        }

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        try {
            List<Future<List<WaitOrderItemDTO>>> futures = executor.invokeAll(tasks);

            List<WaitOrderItemDTO> result = new ArrayList<>();

            for (Future<List<WaitOrderItemDTO>> future : futures) {
                result.addAll(future.get());
            }

            return result;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("поток прерван", e);

        } catch (ExecutionException e) {
            throw new IllegalStateException("ошибка формирования", e);

        } finally {
            executor.shutdown();
        }
    }

    // группировка товаров
    private List<WaitOrderItemDTO> processPart(List<WaitItemData> part, Map<Long, BigDecimal> stockMap) {
        return part.stream().collect(Collectors.groupingBy(WaitItemData::productId)).values().stream().map(group -> createWaitOrderItem(group, stockMap)).toList();
    }

    // формирование итогового DTO
    private WaitOrderItemDTO createWaitOrderItem(List<WaitItemData> group, Map<Long, BigDecimal> stockMap) {
        WaitItemData product = group.getFirst();

        BigDecimal waitQuantity = group.stream().map(WaitItemData::quantity).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal stockQuantity = stockMap.getOrDefault(product.productId(), BigDecimal.ZERO);

        return new WaitOrderItemDTO(product.productId(), product.name(), product.sku(), product.unit(), stockQuantity, product.minStockLevel(), waitQuantity);
    }
    
    //TODO: ответ из склада раскидать по WAIT накладным и поменять им статус, остатки отправить на склад магазина
    //TODO: тип: постака, больше не поставляется (придумать что какой то товар больше не доставляется)
    @Transactional
    public ReplenishmentRequest createReplenishmentRequest(ReplenishmentRequestDTO input) {
        Warehouse warehouse = getWarehouse(currentUserService.getWarehouseId(), "склад магазина не найден");

        Warehouse central = getWarehouse(input.centralWarehouseId(), "главный склад не найден");

        ReplenishmentRequest request = new ReplenishmentRequest();
        request.setWarehouse(warehouse);
        request.setCentralWarehouse(central);

        addRequestItems(request, input.items());

        if (request.getItems().isEmpty()) throw new IllegalArgumentException("Заявка не содержит товаров");

        outboxEventService.saveReplenishmentEvent(
                new ReplenishmentRequested(request.getId(),
                        warehouse.getId(),
                        central.getId(),
                        request.getItems().stream()
                                .map(item ->
                                        new ReplenishmentRequested.Item(item.getProduct().getId(),
                                                item.getQuantity())
                                ).toList())
        );

        return request;
    }

    @Transactional(readOnly = true)
    public ReplenishmentStatusDTO getReplenishmentStatus(Long id) {
        var request = requestRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));
        if (!request.getWarehouse().getId().equals(currentUserService.getWarehouseId()))
            throw new IllegalArgumentException("заявка не найдена");
        return new ReplenishmentStatusDTO(request.getId(), request.getStatus());
    }

    private Warehouse getWarehouse(Long id, String errorMessage) {
        return warehouseRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(errorMessage));
    }

    // добавление товаров в заявку
    private void addRequestItems(ReplenishmentRequest request, List<ReplenishmentRequestDTO.Item> items) {
        if (items == null) throw new IllegalArgumentException("Не указаны товары");
        Set<Long> productIds = new HashSet<>();
        for (ReplenishmentRequestDTO.Item selected : items) {
            if (selected == null || selected.productId() == null || selected.quantity() == null || selected.quantity().signum() < 0 || !productIds.add(selected.productId()))
                throw new IllegalArgumentException("Некорректные или повторяющиеся товары");
            BigDecimal quantity = selected.quantity().setScale(3, RoundingMode.UNNECESSARY);

            if (quantity.signum() == 0) {
                continue;
            }

            Product product = productRepository.findById(selected.productId()).orElseThrow(() -> new IllegalArgumentException("товар не найден"));

            ReplenishmentRequestItem item = new ReplenishmentRequestItem();
            item.setRequest(request);
            item.setProduct(product);
            item.setQuantity(quantity);

            request.getItems().add(item);
        }
    }
}
