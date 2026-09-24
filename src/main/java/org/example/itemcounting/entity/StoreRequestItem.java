package org.example.itemcounting.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "store_request_item")
@Data
public class StoreRequestItem extends CreateOnlyEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "request_id", nullable = false)
    private StoreOrder request;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "requested_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal requestedQuantity;
}
