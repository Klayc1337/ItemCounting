package org.example.itemcounting.business.service;
import lombok.RequiredArgsConstructor;
import org.example.itemcounting.enums.InvoiceStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.itemcounting.entity.*;
import org.example.itemcounting.repository.*;
import org.example.itemcounting.rest.dto.WaitOrderItemDTO;
import org.example.itemcounting.rest.dto.ReplenishmentRequestDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final InvoiceService invoiceService;
    private final StockRepository stockRepository;
    private final WarehouseRepository warehouseRepository;
    private final ReplenishmentRequestRepository requestRepository;
    private final ProductRepository productRepository;


    @Transactional(readOnly = true)
    public List<InvoiceItem> getInvoiceItem(InvoiceStatus status){
        return invoiceService.getByStatus(status).stream()
                .map(Invoice::getItems)
                .flatMap(Collection::stream)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WaitOrderItemDTO> getWaitItems() {
        List<InvoiceItem> waitItems = getInvoiceItem(InvoiceStatus.WAIT);

        if (waitItems.isEmpty()) {
            return List.of();
        }

        // TODO: сделать чтоб под один магазин был
        List<Stock> stocks = stockRepository.findByWarehouseIn(warehouses);

        return waitItems.stream()
                // ключи ID товара
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getId()
                ))
                .values().stream()
                //делаем в DTO
                .map(items -> {
                    Product product = items.getFirst().getProduct();

                    //суммируем количество
                    BigDecimal waitQuantity = items.stream()
                            .map(InvoiceItem::getQuantity)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    //текущие остатки
                    BigDecimal stockQuantity = stocks.stream()
                            .filter(stock -> stock.getProduct().getId().equals(product.getId()))
                            .map(Stock::getQuantity)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    //ID товара, название товара, сколько на складе, сколько надо заказать
                    return new WaitOrderItemDTO(
                            product.getId(),
                            product.getName(),
                            product.getSku(),
                            product.getUnit(),
                            stockQuantity,
                            product.getMinStockLevel(),
                            waitQuantity
                    );
                })
                .sorted(Comparator.comparing(WaitOrderItemDTO::productName))
                .toList();
    }

    @Transactional
    public ReplenishmentRequest createReplenishmentRequest(ReplenishmentRequestDTO input) {
        Warehouse warehouse = warehouseRepository.findById(input.warehouseId())
                .orElseThrow(() -> new IllegalArgumentException("склад магазина не найден"));
        Warehouse central = warehouseRepository.findById(input.centralWarehouseId())
                .orElseThrow(() -> new IllegalArgumentException("главный склад не найден"));

        ReplenishmentRequest request = new ReplenishmentRequest();
        request.setWarehouse(warehouse);
        request.setCentralWarehouse(central);


        for (ReplenishmentRequestDTO.Item selected : input.items()) {
            BigDecimal quantity = selected.quantity().setScale(3, RoundingMode.UNNECESSARY);

            Product product = productRepository.findById(selected.productId()).orElseThrow();

            if (quantity.signum() == 0) continue;
            ReplenishmentRequestItem item = new ReplenishmentRequestItem();
            item.setRequest(request);
            item.setProduct(product);
            item.setQuantity(quantity);
            request.getItems().add(item);
        }
       
        return requestRepository.save(request);
    }
}
