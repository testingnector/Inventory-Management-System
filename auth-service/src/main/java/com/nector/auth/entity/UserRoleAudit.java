package com.nector.auth.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user_role_audit")
public class UserRoleAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userRoleId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID roleId;

    @Column(nullable = false)
    private UUID companyId;

    @Column
    private UUID branchId;

    @Column(nullable = false)
    private String action; // ASSIGN, REVOKE, REACTIVATE

    @Column(nullable = false)
    private UUID performedBy;

    @Column(nullable = false)
    private LocalDateTime performedAt;

    @Column(nullable = false)
    private Boolean oldIsActive;

    @Column(nullable = false)
    private Boolean newIsActive;
}
