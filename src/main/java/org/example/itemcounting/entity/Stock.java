package org.example.itemcounting.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "stock",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "product_id")
        }
)
public class Stock extends UpdateOnlyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_stock_product")
    )
    private Product product;

    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private WarehouseLocation location;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity;

    public void addQuantity(BigDecimal amount) {
        if (amount == null) return;
        this.quantity = this.quantity.add(amount);
        if (this.quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("колличество должно быть больше 0");
        }
    }
}
