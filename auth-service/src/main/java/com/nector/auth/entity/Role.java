package com.nector.auth.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "role_code", nullable = false, unique = true, length = 25)
    private String roleCode;

    @Column(name = "role_name", nullable = false, length = 40)
    private String roleName;

    @Column(name = "description", length = 100)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "is_system_role", nullable = false)
    private Boolean systemRole = false;

    private LocalDateTime createdAt;
    private UUID createdBy;

    private LocalDateTime updatedAt;
    private UUID updatedBy;

    private LocalDateTime deletedAt;
    private UUID deletedBy;


    // Auto timestamp
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        active = true;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
