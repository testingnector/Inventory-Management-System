package com.nector.catalogservice.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import com.nector.catalogservice.converter.JsonConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "product_variants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "sku_code"}),
        indexes = {
                @Index(name = "idx_variant_product_id", columnList = "product_id"),
                @Index(name = "idx_variant_company_id", columnList = "company_id"),
                @Index(name = "idx_variant_sku", columnList = "sku_code")
        }
)
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "sku_code", nullable = false, length = 50)
    private String skuCode;

    @NotBlank
    @Size(max = 100)
    @Column(name = "variant_name", nullable = false, length = 100)
    private String variantName;

    @NotNull
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @NotNull
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Size(max = 30)
    @Column(name = "color", length = 30)
    private String color;

    @Size(max = 20)
    @Column(name = "size", length = 20)
    private String size;

    @Convert(converter = JsonConverter.class)
    @Column(name = "custom_attributes", columnDefinition = "JSON")
    private Map<String, Object> customAttributes;


    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "mrp", nullable = false, precision = 15, scale = 2)
    private BigDecimal mrp;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "selling_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal sellingPrice;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(name = "purchase_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal purchasePrice;

    @NotNull
    @Column(name = "uom_id", nullable = false)
    private UUID uomId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(name = "conversion_factor", nullable = false, precision = 10, scale = 4)
    private BigDecimal conversionFactor;

    @Column(name = "is_serialized", nullable = false)
    private Boolean serialized = false;

    @Column(name = "is_batch_tracked", nullable = false)
    private Boolean batchTracked = false;

    @Column(name = "is_expiry_tracked", nullable = false)
    private Boolean expiryTracked = false;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.active = active;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
