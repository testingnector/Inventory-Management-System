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
@Table(name = "company_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(name = "company_id", nullable = false)
	private UUID companyId;

	@Column(name = "category_id", nullable = false)
	private UUID categoryId;

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
		if (this.active == null)
			this.active = true;
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
