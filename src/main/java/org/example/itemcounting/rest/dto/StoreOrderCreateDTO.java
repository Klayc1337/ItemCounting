package org.example.itemcounting.rest.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StoreOrderCreateDTO {
    private Long fromWarehouseId;
    private Long toWarehouseId;
    private List<RequestItemRequest> items;
    private String comment;
    private Long warehouseId;

    @Data
    public static class RequestItemRequest {
        private Long productId;
        private BigDecimal quantity;
    }
}

