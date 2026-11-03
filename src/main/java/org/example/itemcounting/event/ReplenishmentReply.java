package org.example.itemcounting.event;

import java.math.BigDecimal;
import java.util.List;

public record ReplenishmentReply(Long requestId,
                                 Long warehouseId,
                                 Long centralWarehouseId,
                                 String status,
                                 Long invoiceId,
                                 List<Item> items) {
    public record Item(Long productId, BigDecimal quantity) {}
}