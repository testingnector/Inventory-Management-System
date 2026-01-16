package com.nector.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user_sessions",
       indexes = {
           @Index(name = "idx_user_sessions_user_id", columnList = "user_id"),
           @Index(name = "idx_user_sessions_token", columnList = "session_token"),
           @Index(name = "idx_user_sessions_active", columnList = "is_active"),
           @Index(name = "idx_user_sessions_expires", columnList = "expires_at")
       })
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "session_token", nullable = false, length = 512)
    private String sessionToken;

    @Column(name = "token_type", nullable = false, length = 20)
    private String tokenType; // ACCESS / REFRESH

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "login_at", nullable = false)
    private LocalDateTime loginAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "logout_at")
    private LocalDateTime logoutAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revoked_by")
    private UUID revokedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.loginAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}

