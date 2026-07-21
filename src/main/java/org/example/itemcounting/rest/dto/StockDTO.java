package org.example.itemcounting.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.itemcounting.entity.Product;
import org.example.itemcounting.entity.Stock;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal quantity;
    private LocalDateTime updatedAt;

    public static StockDTO fromEntity(Stock stock) {
        return new StockDTO(
                stock.getId(),
                stock.getProduct().getId(),
                stock.getProduct().getName(),
                stock.getProduct().getSku(),
                stock.getQuantity(),
                stock.getUpdatedAt()
        );
    }
}
