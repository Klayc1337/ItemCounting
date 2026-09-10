package org.example.itemcounting.strategy;

import org.example.itemcounting.rest.dto.InvoiceDTO;

import java.math.BigDecimal;

public interface InvoicePricingStrategy {
    BigDecimal calculateTotal(InvoiceDTO invoice, BigDecimal userCoefficient);
}
