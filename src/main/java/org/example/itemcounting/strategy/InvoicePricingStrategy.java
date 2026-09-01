package org.example.itemcounting.strategy;

import org.example.itemcounting.entity.InvoiceItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface InvoicePricingStrategy {
    BigDecimal calculateTotal(List<InvoiceItem> items, BigDecimal userCoefficient, LocalDateTime deliveryTime);
}
