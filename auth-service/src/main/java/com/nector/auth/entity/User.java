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
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, length = 35)
	private String name;

	@Column(nullable = false, length = 40, unique = true)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(name = "password_algorithm", length = 30)
	private String passwordAlgorithm;
	
	@Column(length = 13, nullable = false)
	private String mobileNumber;

	@Column(name = "is_active")
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
