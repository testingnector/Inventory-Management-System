package com.nector.catalogservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_images", 
	indexes = { 
		@Index(name = "idx_product_id", columnList = "product_id"),
		@Index(name = "idx_product_variant_id", columnList = "product_variant_id"),
		@Index(name = "idx_product_image_company_id", columnList = "company_id"),
		@Index(name = "idx_is_active", columnList = "is_active") 
	})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductImage {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "product_id")
	private UUID productId;

	@Column(name = "product_variant_id")
	private UUID productVariantId;
	
	@Column(name = "company_id", nullable = false)
	private UUID companyId;

	@Column(name = "image_url", nullable = false, length = 600)
	private String imageUrl;

	@Column(name = "image_type", length = 50)
	private String imageType;

	@Column(name = "alt_text", length = 255)
	private String altText;

	@Column(name = "is_primary", nullable = false)
	private Boolean primary = false;

	@Column(name = "display_order")
	private Integer displayOrder;

	@Column(name = "is_active", nullable = false)
	private Boolean active = true;
	
	@Column(name = "file_hash", length = 64, nullable = false)
	private String fileHash;

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
		if ((productId == null && productVariantId == null) || (productId != null && productVariantId != null)) {
			throw new IllegalStateException("Exactly one of productId or productVariantId must be set");
		}
	}

}
