package com.nector.catalogservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "product_variants",
       uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "sku_code"}))
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "sku_code", nullable = false, length = 50)
    private String skuCode;

    @Column(name = "variant_name", length = 100)
    private String variantName;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "color", length = 30)
    private String color;

    @Column(name = "size", length = 20)
    private String size;

    @Column(name = "custom_attributes", columnDefinition = "jsonb")
    private String customAttributes; 

    @Column(name = "mrp", nullable = false)
    @Min(0)
    private BigDecimal mrp;

    @Column(name = "selling_price", nullable = false)
    @Min(0)
    private BigDecimal sellingPrice;

    @Column(name = "purchase_price", nullable = false)
    @Min(0)
    private BigDecimal purchasePrice;

    @Column(name = "uom_id", nullable = false)
    private UUID uomId;

    @Column(name = "conversion_factor", nullable = false)
    @Min(1)
    private BigDecimal conversionFactor;

    @Column(name = "is_serialized", nullable = false)
    private Boolean serialized = false;

    @Column(name = "is_batch_tracked", nullable = false)
    private Boolean batchTracked = false;

    @Column(name = "is_expiry_tracked", nullable = false)
    private Boolean expiryTracked = false;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    // --------------------- Hooks ---------------------
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        if (active == null) active = true;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
