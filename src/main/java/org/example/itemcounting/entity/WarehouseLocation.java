package org.example.itemcounting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "warehouse_location")
public class WarehouseLocation extends AuditEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(nullable = false)
    private String rack; // Стеллаж A, B

    @Column(nullable = false)
    private String shelf; // A-01, A-02

    @Column(name = "max_width", precision = 10, scale = 2)
    private BigDecimal maxWidth;

    @Column(name = "max_height", precision = 10, scale = 2)
    private BigDecimal maxHeight;

    @Column(name = "max_depth", precision = 10, scale = 2)
    private BigDecimal maxDepth;

    @Column(name = "max_weight", precision = 10, scale = 2)
    private BigDecimal maxWeight;

    @Column(name = "is_active")
    private Boolean isActive = true;

}
