package org.example.itemcounting.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.example.itemcounting.enums.RequestStatus;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "store_request")
@Data
public class StoreOrder extends AuditEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "warehouse", nullable = false)
    private Warehouse Warehouse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private User requestedBy;

    @ManyToOne
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy;

    private String comment;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoreRequestItem> items = new ArrayList<>();
}
