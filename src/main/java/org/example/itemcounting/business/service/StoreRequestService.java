package org.example.itemcounting.business.service;

import lombok.RequiredArgsConstructor;
import org.example.itemcounting.entity.*;
import org.example.itemcounting.enums.InvoiceStatus;
import org.example.itemcounting.enums.InvoiceType;
import org.example.itemcounting.enums.RequestStatus;
import org.example.itemcounting.repository.*;
import org.example.itemcounting.rest.dto.StoreOrderCreateDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StoreRequestService {
    private final StoreRequestRepository requestRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final StockService stockService;
    private final InvoiceRepository invoiceRepository;

    @Transactional
    public StoreOrder createStoreOrder(StoreOrderCreateDTO order, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        Warehouse warehouse = warehouseRepository.findById(order.getWarehouseId())
                .orElseThrow(() -> new IllegalArgumentException("Склад магазина не найден"));

        StoreOrder storeOrder = new StoreOrder();
        storeOrder.setWarehouse(warehouse);
        storeOrder.setStatus(RequestStatus.PENDING);
        storeOrder.setRequestedBy(user);
        storeOrder.setComment(order.getComment());
        storeOrder.setCreatedAt(LocalDateTime.now());
        storeOrder.setUpdatedAt(LocalDateTime.now());

        for (StoreOrderCreateDTO.RequestItemRequest itemReq : order.getItems()) {

            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException(
                                        "Товар не найден: " + itemReq.getProductId())
                    );

            BigDecimal available = stockService.getTotalStockInWarehouse(
                    product.getId(),
                    warehouse.getId()
            );

            if (available.compareTo(itemReq.getQuantity()) < 0) {
                throw new IllegalStateException(
                        String.format(
                                "Недостаточно товара '%s' на складе магазина. " +
                                        "Доступно: %s, запрошено: %s",
                                product.getName(),
                                available,
                                itemReq.getQuantity()
                        )
                );
            }

            StoreRequestItem requestItem = new StoreRequestItem();
            requestItem.setRequest(storeOrder);
            requestItem.setProduct(product);
            requestItem.setRequestedQuantity(itemReq.getQuantity());
            requestItem.setCreatedAt(LocalDateTime.now());

            storeOrder.getItems().add(requestItem);
        }

        requestRepository.save(storeOrder);

        Invoice invoice = new Invoice();
        invoice.setType(InvoiceType.SHIPMENT);
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setFromWarehouse(warehouse);
        invoice.setToWarehouse(warehouse);
        invoice.setCreatedBy(user);
        invoice.setComment("Создано по заявке #" + storeOrder.getId());
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());

        for (StoreRequestItem requestItem : storeOrder.getItems()) {

            InvoiceItem invoiceItem = new InvoiceItem();
            invoiceItem.setInvoice(invoice);
            invoiceItem.setProduct(requestItem.getProduct());
            invoiceItem.setQuantity(requestItem.getRequestedQuantity());
            invoiceItem.setPrice(requestItem.getProduct().getPrice());
            invoiceItem.setCreatedAt(LocalDateTime.now());

            invoice.getItems().add(invoiceItem);
        }

        invoiceRepository.save(invoice);

        for (InvoiceItem item : invoice.getItems()) {
            stockService.removeStock(
                    item.getProduct().getId(),
                    warehouse.getId(),
                    item.getQuantity(),
                    "INVOICE",
                    invoice.getId(),
                    userId,
                    "Расход по накладной #" + invoice.getId()
            );
        }

        invoice.setStatus(InvoiceStatus.COMPLETED);
        invoice.setConfirmedBy(user);
        invoice.setConfirmedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());

        invoiceRepository.save(invoice);

        storeOrder.setStatus(RequestStatus.APPROVED);
        storeOrder.setApprovedBy(user);
        storeOrder.setUpdatedAt(LocalDateTime.now());

        return requestRepository.save(storeOrder);
    }
}