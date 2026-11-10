package org.example.itemcounting.business.service;

import lombok.RequiredArgsConstructor;
import org.example.itemcounting.entity.InvoiceItem;
import org.example.itemcounting.entity.Stock;
import org.example.itemcounting.enums.InvoiceStatus;
import org.example.itemcounting.repository.InvoiceRepository;
import org.example.itemcounting.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WaitingInvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final StockRepository stockRepository;

    @Transactional
    public void completeIfStockAvailable(Long invoiceId) {
        var invoice = invoiceRepository.findLockedById(invoiceId).orElse(null);
        if (invoice == null || invoice.getStatus() != InvoiceStatus.WAIT
                || invoice.getFromWarehouse() == null || invoice.getItems().isEmpty()) {
            return;
        }

        Map<Long, BigDecimal> required = new HashMap<>();
        for (InvoiceItem item : invoice.getItems()) {
            if (item.getQuantity() == null || item.getQuantity().signum() <= 0) {
                throw new IllegalStateException("неправильное количество товаров в invoice #" + invoiceId);
            }
            required.merge(item.getProduct().getId(), item.getQuantity(), BigDecimal::add);
        }

        List<Stock> stocks = stockRepository.findLockedByWarehouseId(invoice.getFromWarehouse().getId());
        Map<Long, BigDecimal> available = new HashMap<>();
        for (Stock stock : stocks) {
            available.merge(stock.getProduct().getId(), stock.getQuantity(), BigDecimal::add);
        }
        for (var entry : required.entrySet()) {
            if (available.getOrDefault(entry.getKey(), BigDecimal.ZERO).compareTo(entry.getValue()) < 0) {
                return;
            }
        }

        LocalDateTime now = LocalDateTime.now();
        for (Stock stock : stocks) {
            Long productId = stock.getProduct().getId();
            BigDecimal remaining = required.getOrDefault(productId, BigDecimal.ZERO);
            if (remaining.signum() <= 0 || stock.getQuantity().signum() <= 0) {
                continue;
            }
            BigDecimal quantity = remaining.min(stock.getQuantity());
            stock.setQuantity(stock.getQuantity().subtract(quantity));
            stock.setUpdatedAt(now);
            required.put(productId, remaining.subtract(quantity));
        }

        invoice.getItems().forEach(item -> item.setQuantityNeed(BigDecimal.ZERO));
        invoice.setStatus(InvoiceStatus.COMPLETED);
        invoice.setConfirmedAt(now);
        invoice.setUpdatedAt(now);
    }
}
