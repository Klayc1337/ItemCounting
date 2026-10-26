package org.example.itemcounting.event;
import java.math.BigDecimal;
import java.util.List;
public record ReplenishmentRequested(Long requestId,
                                     Long warehouseId,
                                     Long centralWarehouseId,
                                     List<Item> items
) {
 public record Item(Long productId,
                    BigDecimal quantity) {}
}
