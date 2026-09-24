package org.example.itemcounting.entity;
import jakarta.persistence.*;
import lombok.Data;
import org.example.itemcounting.enums.UserRole;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
public class User extends AuditEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "is_active")
    private Boolean isActive = true;
}