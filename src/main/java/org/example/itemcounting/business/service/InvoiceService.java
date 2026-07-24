package org.example.itemcounting.business.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.itemcounting.entity.Invoice;
import org.example.itemcounting.entity.InvoiceItem;
import org.example.itemcounting.entity.Product;
import org.example.itemcounting.enums.InvoiceStatus;
import org.example.itemcounting.enums.InvoiceType;
import org.example.itemcounting.event.GoodsReceivedEvent;
import org.example.itemcounting.exception.EntityNotFoundException;
import org.example.itemcounting.exception.IllegalInvoiceStateException;
import org.example.itemcounting.exception.InvalidQuantityException;
import org.example.itemcounting.kafka.GoodsReceivedProducer;
import org.example.itemcounting.repository.InvoiceItemRepository;
import org.example.itemcounting.repository.InvoiceRepository;
import org.example.itemcounting.repository.ProductRepository;
import org.example.itemcounting.rest.dto.InvoiceDTO;
import org.example.itemcounting.rest.dto.InvoiceItemDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;
    private final GoodsReceivedProducer goodsReceivedProducer;

    // создание накладной
    @Transactional
    public InvoiceDTO createInvoice(InvoiceDTO requestDto) {
        InvoiceType type = requestDto.getType();
        if (type == null) {
            throw new IllegalArgumentException("у накладной должен быть тип");
        }

        if (requestDto.getItems() == null || requestDto.getItems().isEmpty()) {
            throw new InvalidQuantityException("накладная должна содержать хотя бы 1 товар");
        }

        Invoice invoice = new Invoice();
        invoice.setType(type);
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setComment(requestDto.getComment());
        invoice = invoiceRepository.save(invoice);

        List<InvoiceItem> items = new ArrayList<>();
        if (requestDto.getItems() != null) {
            for (InvoiceItemDTO itemDto : requestDto.getItems()) {
                Product product = productRepository.findById(itemDto.getProductId())
                        .orElseThrow(() -> new EntityNotFoundException("нет продукта с id: " + itemDto.getProductId()));

                InvoiceItem item = itemDto.toEntity(invoice, product);
                items.add(item);
            }
        }
        items = invoiceItemRepository.saveAll(items);

        if (type == InvoiceType.ARRIVAL) {
            for (InvoiceItem item : items) {
                stockService.increaseStock(item.getProduct().getId(), item.getQuantity());
            }
        } else if (type == InvoiceType.SHIPMENT) {
            for (InvoiceItem item : items) {
                stockService.decreaseStock(item.getProduct().getId(), item.getQuantity());
            }
        }

        invoice.setStatus(InvoiceStatus.COMPLETED);
        invoice = invoiceRepository.save(invoice);

        if (type == InvoiceType.ARRIVAL) {
            GoodsReceivedEvent event = GoodsReceivedEvent.mapToEvent(invoice, items);
            goodsReceivedProducer.sendSync(event);
            log.info("событие отправлено для накладной id={}", invoice.getId());
        }

        return InvoiceDTO.fromEntity(invoice, items);
    }

    
    // накладная по id
    @Transactional(readOnly = true)
    public InvoiceDTO getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("нет элемента с id: " + id));

        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(id);
        return InvoiceDTO.fromEntity(invoice, items);
    }

    
    // все накладные
    @Transactional(readOnly = true)
    public List<InvoiceDTO> getAllInvoices(InvoiceType type, InvoiceStatus status) {
        List<Invoice> invoices;

        if (type != null && status != null) {
            invoices = invoiceRepository.findByTypeAndStatus(type, status);
        } else if (type != null) {
            invoices = invoiceRepository.findByType(type);
        } else if (status != null) {
            invoices = invoiceRepository.findByStatus(status);
        } else {
            invoices = invoiceRepository.findAll();
        }

        return invoices.stream()
                .map(invoice -> {
                    List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(invoice.getId());
                    return InvoiceDTO.fromEntity(invoice, items);
                })
                .collect(Collectors.toList());
    }

    
    //отмена накладной
    @Transactional
    public void cancelInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("нет элемента с id: " + id));

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new IllegalInvoiceStateException("накладная уже отменена");
        }
        if (invoice.getStatus() == InvoiceStatus.DRAFT) {
            invoice.setStatus(InvoiceStatus.CANCELLED);
            invoiceRepository.save(invoice);
            return;
        }
        if (invoice.getStatus() == InvoiceStatus.COMPLETED) {
            List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(id);
            if (invoice.getType() == InvoiceType.ARRIVAL) {
                for (InvoiceItem item : items) {
                    stockService.decreaseStock(item.getProduct().getId(), item.getQuantity());
                }
            } else if (invoice.getType() == InvoiceType.SHIPMENT) {
                for (InvoiceItem item : items) {
                    stockService.increaseStock(item.getProduct().getId(), item.getQuantity());
                }
            }
            invoice.setStatus(InvoiceStatus.CANCELLED);
            invoiceRepository.save(invoice);
        }
    }
}
