package com.nector.orgservice.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
		name = "companies",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_company_code", columnNames = "company_code"),
				@UniqueConstraint(name = "uk_company_gst", columnNames = "company_gst"),
				@UniqueConstraint(name = "uk_company_pan", columnNames = "company_pan")
		},
		
		indexes = {
				@Index(name = "idx_company_is_active", columnList = "is_active"),
				@Index(name = "idx_company_state_city", columnList = "state, city")
		}
)
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_code", nullable = false, length = 20)
    private String companyCode;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Column(name = "legal_name", nullable = false, length = 150)
    private String legalName;

    @Column(name = "company_type", nullable = false, length = 30)
    private String companyType;

    @Column(name = "gst_number", nullable = false, length = 15)
    private String gstNumber;

    @Column(name = "pan_number", nullable = false, length = 10)
    private String panNumber;

    @Column(name = "email", nullable = false, length = 50)
    private String email;

    @Column(name = "phone", nullable = false, length = 15)
    private String phone;

    @Column(name = "address", nullable = false, length = 200)
    private String address;

    @Column(name = "country", nullable = false, length = 30)
    private String country = "India";

    @Column(name = "state", nullable = false, length = 50)
    private String state;

    @Column(name = "city", nullable = false, length = 50)
    private String city;

    @Column(name = "pincode", nullable = false, length = 6)
    private String pincode;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    //  Audit fields
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @Column(name = "deleted_by")
    private UUID deletedBy;

    //  Lifecycle hooks
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.active = true;
        if (this.country == null) {
            this.country = "India";
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
