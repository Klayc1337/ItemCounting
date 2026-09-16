package org.example.itemcounting.strategy;

import org.example.itemcounting.enums.PricingStrategyType;
import org.example.itemcounting.rest.dto.InvoiceDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("defaultPricingStrategy")
public class DefaultPricingStrategy implements InvoicePricingStrategy {
    @Override
    public BigDecimal calculateTotal(InvoiceDTO invoice, BigDecimal userCoefficient) {
        BigDecimal itemsSum = invoice.getItems().stream()
                .map(item -> item.getQuantity().multiply(item.getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return itemsSum.multiply(userCoefficient);
    }

    @Override
    public PricingStrategyType getType() {
        return PricingStrategyType.DEFAULT;
    }
}
