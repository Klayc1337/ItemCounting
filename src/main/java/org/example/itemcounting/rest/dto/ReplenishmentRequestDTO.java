package org.example.itemcounting.rest.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReplenishmentRequestDTO(Long centralWarehouseId, List<Item> items) {
    public record Item(Long productId, BigDecimal quantity) {}
}
