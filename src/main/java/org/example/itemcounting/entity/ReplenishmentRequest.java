package org.example.itemcounting.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.itemcounting.enums.RequestStatus;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "replenishment_request")
@Getter
@Setter
public class ReplenishmentRequest extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "central_warehouse_id", nullable = false)
    private Warehouse centralWarehouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status = RequestStatus.PENDING;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL)
    private List<ReplenishmentRequestItem> items = new ArrayList<>();
}
