package org.example.itemcounting.event;
public record ReplenishmentReply(Long requestId,
                                 Long warehouseId,
                                 Long centralWarehouseId,
                                 String status
) {}
