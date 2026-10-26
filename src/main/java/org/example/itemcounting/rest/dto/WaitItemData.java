package org.example.itemcounting.rest.dto;

import java.math.BigDecimal;

public record WaitItemData(
        Long productId,
        String name,
        String sku,
        String unit,
        BigDecimal minStockLevel,
        BigDecimal quantity
) {}
