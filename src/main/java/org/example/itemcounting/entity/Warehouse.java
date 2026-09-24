package org.example.itemcounting.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "warehouse")
@Builder
public class Warehouse extends AuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;

    @Column(nullable = false)
    private String type; // CENTRAL, STORE
}
