package com.nector.catalogservice.entity;

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
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "product_code", nullable = false, unique = true, length = 30)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "sub_category_id")
    private UUID subCategoryId;

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "brand_name", length = 70)
    private String brandName;

    @Column(name = "model_number", length = 100)
    private String modelNumber;

    @Column(name = "is_variant_based", nullable = false)
    private Boolean variantBased = false;

    @Column(name = "is_serialized", nullable = false)
    private Boolean serialized = false;

    @Column(name = "is_batch_tracked", nullable = false)
    private Boolean batchTracked = false;

    @Column(name = "is_expiry_tracked", nullable = false)
    private Boolean expiryTracked = false;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.active == null) this.active = true;
        if (this.variantBased == null) this.variantBased = false;
        if (this.serialized == null) this.serialized = false;
        if (this.batchTracked == null) this.batchTracked = false;
        if (this.expiryTracked == null) this.expiryTracked = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
