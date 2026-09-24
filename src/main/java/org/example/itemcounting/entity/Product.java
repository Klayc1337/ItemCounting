package org.example.itemcounting.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "product")
public class Product extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(nullable = false, length = 20)
    private String unit;

    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal depth;
    private BigDecimal weight;

    @Column(precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "min_stock_level", precision = 15, scale = 3)
    private BigDecimal minStockLevel = BigDecimal.ZERO;

}
