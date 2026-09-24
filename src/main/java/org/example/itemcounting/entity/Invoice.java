package org.example.itemcounting.entity;
import jakarta.persistence.*;
import lombok.*;
import org.example.itemcounting.enums.InvoiceStatus;
import org.example.itemcounting.enums.InvoiceType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.dialect.type.PostgreSQLEnumJdbcType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "invoice")
@Builder
public class Invoice extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private InvoiceType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(name = "supplier_name")
    private String supplierName;

    @ManyToOne
    @JoinColumn(name = "from_warehouse_id")
    private Warehouse fromWarehouse;

    @ManyToOne
    @JoinColumn(name = "to_warehouse_id")
    private Warehouse toWarehouse;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "confirmed_by_user_id")
    private User confirmedBy;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String comment;
}
