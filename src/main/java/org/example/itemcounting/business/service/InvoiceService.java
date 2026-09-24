package org.example.itemcounting.business.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.itemcounting.entity.*;
import org.example.itemcounting.enums.InvoiceStatus;
import org.example.itemcounting.enums.InvoiceType;
import org.example.itemcounting.event.GoodsReceivedEvent;
import org.example.itemcounting.exception.EntityNotFoundException;
import org.example.itemcounting.exception.IllegalInvoiceStateException;
import org.example.itemcounting.exception.InvalidQuantityException;
import org.example.itemcounting.repository.*;
import org.example.itemcounting.rest.dto.ArrivalInvoiceRequestDTO;
import org.example.itemcounting.rest.dto.InvoiceDTO;
import org.example.itemcounting.rest.dto.InvoiceItemDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;

    private final OutboxEventService outboxEventService;


    // создание приходной накладной (DRAFT)
    @Transactional
    public Invoice createInvoice(ArrivalInvoiceRequestDTO order, Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        Warehouse warehouse = warehouseRepository.findById(order.getToWarehouseId())
                .orElseThrow(() -> new IllegalArgumentException("Склад не найден"));

        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new InvalidQuantityException("накладная должна содержать хотя бы 1 товар");
        }

        Invoice invoice = saveInvoice(order, user, warehouse);

        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice confirmArrivalInvoice(Long invoiceId, Long userId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Накладная не найдена"));

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Подтвердить можно только черновик");
        }

        if (invoice.getType() != InvoiceType.ARRIVAL) {
            throw new IllegalStateException("Это не приходная накладная");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        invoice.setStatus(InvoiceStatus.CONFIRMED);
        invoice.setConfirmedBy(user);
        invoice.setConfirmedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());

        for (InvoiceItem item : invoice.getItems()) {
            stockService.addStock(
                    item.getProduct().getId(),
                    invoice.getToWarehouse().getId(),
                    null,
                    item.getQuantity(),
                    "INVOICE",
                    invoice.getId(),
                    userId,
                    "Приход по накладной " + invoice.getId()
            );
        }

        return invoiceRepository.save(invoice);
    }

    private Invoice saveInvoice(ArrivalInvoiceRequestDTO order, User user, Warehouse warehouse){
        Invoice invoice = new Invoice();
        invoice.setType(InvoiceType.ARRIVAL);
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setSupplierName(order.getSupplierName());
        invoice.setToWarehouse(warehouse);
        invoice.setCreatedBy(user);
        invoice.setComment(order.getComment());

        for (ArrivalInvoiceRequestDTO.InvoiceItemRequest itemReq : order.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Товар не найден: " + itemReq.getProductId()));

            InvoiceItem item = new InvoiceItem();
            item.setInvoice(invoice);
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setPrice(itemReq.getPrice());
            item.setCreatedAt(LocalDateTime.now());

            invoice.getItems().add(item);
        }
        return invoice;
    }
}
