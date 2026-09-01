package org.example.itemcounting.strategy;

import org.example.itemcounting.entity.InvoiceItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component("defaultPricingStrategy")
public class DefaultPricingStrategy implements InvoicePricingStrategy {
    @Override
    public BigDecimal calculateTotal(List<InvoiceItem> items, BigDecimal userCoefficient, LocalDateTime deliveryTime) {
        BigDecimal itemsSum = items.stream()
                .map(item -> item.getQuantity().multiply(item.getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return itemsSum.multiply(userCoefficient);
    }
}
