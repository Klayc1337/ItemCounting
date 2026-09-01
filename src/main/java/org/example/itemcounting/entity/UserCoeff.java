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
@Table(name = "user_coeff")
public class UserCoeff extends AuditEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_name", unique = true, nullable = false, length = 50)
    private String groupName;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal coefficient;
}
