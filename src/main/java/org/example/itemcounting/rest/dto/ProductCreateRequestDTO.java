package org.example.itemcounting.rest.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCreateRequestDTO {
    private String name;
    private String sku;
    private String unit = "шт";
    private BigDecimal price;
    private BigDecimal minStockLevel;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal depth;
    private BigDecimal weight;
}
