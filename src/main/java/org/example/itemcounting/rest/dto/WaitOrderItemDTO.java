package org.example.itemcounting.rest.dto;

import java.math.BigDecimal;

public record WaitOrderItemDTO(
        Long productId, String productName,
        String sku, String unit,
        BigDecimal currentStock, BigDecimal minStockLevel,
        BigDecimal waitQuantity
) { }
