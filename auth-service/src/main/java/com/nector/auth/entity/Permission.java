package com.nector.auth.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 30, name = "permission_code", unique = true, nullable = false)
    private String permissionCode;

    @Column(length = 40, name = "permission_name", nullable = false)
    private String permissionName;

    @Column(length = 150, nullable = false)
    private String description;

    @Column(length = 30, name = "module_name", nullable = false)
    private String moduleName;

    private Boolean active = true;

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



