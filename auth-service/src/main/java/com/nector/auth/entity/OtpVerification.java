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
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "otp_verification")
public class OtpVerification {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	
	@Column(nullable = true)
	private String email;
	
	@Column(nullable = true)
	private String otp;
	
	@Column(nullable = true)
	private LocalDateTime expiryTime;
	
	@Column(nullable = true)
	private boolean used = false;

    // 🔹 Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // 🔹 Automatically set on insert
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // 🔹 Automatically set on update
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
	
	
}
