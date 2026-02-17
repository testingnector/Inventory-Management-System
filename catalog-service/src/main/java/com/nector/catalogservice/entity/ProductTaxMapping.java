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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "product_tax_mapping",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_company_product_tax",
            columnNames = {"company_id", "product_id", "company_tax_category_id"}
        ),
        @UniqueConstraint(
            name = "uk_company_variant_tax",
            columnNames = {"company_id", "product_variant_id", "company_tax_category_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductTaxMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_variant_id")
    private UUID productVariantId;

    @Column(name = "company_tax_category_id", nullable = false)
    private UUID companyTaxCategoryId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false)
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
        validateRelation();
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        validateRelation();
        this.updatedAt = LocalDateTime.now();
    }

    private void validateRelation() {
        if ((productId == null && productVariantId == null) ||
            (productId != null && productVariantId != null)) {
            throw new IllegalStateException(
                "Exactly one of productId or productVariantId must be provided"
            );
        }
    }
}
