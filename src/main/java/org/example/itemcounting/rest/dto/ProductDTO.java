package org.example.itemcounting.rest.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.itemcounting.entity.Product;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;

    private String name;

    private String sku;

    private String unit;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static ProductDTO fromEntity(Product product) {
        return new ProductDTO(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getUnit(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    public Product toEntity() {
        Product product = new Product();
        product.setName(this.name);
        product.setSku(this.sku);
        product.setUnit(this.unit);
        return product;
    }
}
