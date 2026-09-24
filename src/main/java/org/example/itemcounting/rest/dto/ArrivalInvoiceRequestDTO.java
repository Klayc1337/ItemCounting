package org.example.itemcounting.rest.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ArrivalInvoiceRequestDTO {
    private String supplierName;
    private Long toWarehouseId;
    private List<InvoiceItemRequest> items;
    private String comment;

    @Data
    public static class InvoiceItemRequest {
        private Long productId;
        private BigDecimal quantity;
        private BigDecimal price;
    }
}

