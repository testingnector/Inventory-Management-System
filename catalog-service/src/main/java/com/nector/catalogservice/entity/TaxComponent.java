package com.nector.catalogservice.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.nector.catalogservice.enums.TaxComponentType;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tax_components", uniqueConstraints = @UniqueConstraint(columnNames = { "company_tax_category_id",
		"component_type" }))
public class TaxComponent {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "company_tax_category_id", nullable = false)
	private UUID companyTaxCategoryId;

	@Enumerated(EnumType.STRING)
	@Column(name = "component_type", nullable = false)
	private TaxComponentType componentType;

	@Column(name = "component_rate", nullable = false)
	private Double componentRate;
	
	@Column(name = "is_active", nullable = false)
	private Boolean active = true;

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
		createdAt = LocalDateTime.now();
		this.active = active;
	}

	@PreUpdate
	public void preUpdate() {
		updatedAt = LocalDateTime.now();
	}
}
