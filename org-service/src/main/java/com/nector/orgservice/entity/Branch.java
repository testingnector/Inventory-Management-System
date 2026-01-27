package com.nector.orgservice.entity;

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
@Table(name = "branches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "branch_code", length = 20, nullable = false, unique = true)
    private String branchCode;

    @Column(name = "branch_name", length = 100, nullable = false)
    private String branchName;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(length = 200)
    private String address;
    
    @Column(length = 30)
    private String country;
    
    @Column(length = 50)
    private String state;
    
    @Column(length = 50)
    private String city;
    
    @Column(length = 6)
    private String pincode;
    
    @Column(length = 13)
    private String phone;
    
    @Column(length = 50)
    private String email;

    @Column(name = "is_head_office")
    private Boolean headOffice = false;

    @Column(name = "is_active")
    private Boolean active = true;

    private LocalDateTime createdAt;
    private UUID createdBy;
    private LocalDateTime updatedAt;
    private UUID updatedBy;
    private LocalDateTime deletedAt;
    private UUID deletedBy;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
