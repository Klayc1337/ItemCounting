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

        boolean enoughStock = true;

        StoreOrder storeOrder = new StoreOrder();
        storeOrder.setWarehouse(warehouse);
        storeOrder.setStatus(RequestStatus.PENDING);
        storeOrder.setRequestedBy(user);
        storeOrder.setComment(order.getComment());
        storeOrder.setCreatedAt(LocalDateTime.now());
        storeOrder.setUpdatedAt(LocalDateTime.now());

        for (StoreOrderCreateDTO.RequestItemRequest itemReq : order.getItems()) {

            StoreRequestItem requestItem = new StoreRequestItem();
            requestItem.setRequest(storeOrder);
            requestItem.setProduct(getProductById(itemReq));
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
            invoiceItem.setQuantityNeed(getAvailable(requestItem.getProduct(), warehouse).add(requestItem.getRequestedQuantity()).negate());
            invoiceItem.setPrice(requestItem.getProduct().getPrice());
            invoiceItem.setCreatedAt(LocalDateTime.now());
            enoughStock = invoiceItem.getQuantityNeed().compareTo(BigDecimal.valueOf(0)) < 0 ? false : true;

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

        if (enoughStock) {
            invoice.setStatus(InvoiceStatus.COMPLETED);
            invoice.setConfirmedBy(user);
            invoice.setConfirmedAt(LocalDateTime.now());

            storeOrder.setStatus(RequestStatus.APPROVED);
            storeOrder.setApprovedBy(user);
        } else {
            invoice.setStatus(InvoiceStatus.WAIT);
        }

        invoice.setUpdatedAt(LocalDateTime.now());
        invoiceRepository.save(invoice);

        storeOrder.setUpdatedAt(LocalDateTime.now());


        return requestRepository.save(storeOrder);
    }

    private BigDecimal getAvailable(Product product, Warehouse warehouse){
        return stockService.getTotalStockInWarehouse(
                product.getId(),
                warehouse.getId()
        );
    }

    private Product getProductById(StoreOrderCreateDTO.RequestItemRequest itemReq){
        return productRepository.findById(itemReq.getProductId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Товар не найден: " + itemReq.getProductId())
                );
    }

}