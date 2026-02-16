package com.nector.catalogservice.specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.nector.catalogservice.entity.ProductImage;

import jakarta.persistence.criteria.Predicate;

public class ProductImageSpecification {

	public static Specification<ProductImage> filterProductImages(UUID companyId, UUID productId, UUID productVariantId,
			Boolean active, Boolean primary, String imageType, String altText, LocalDateTime createdAfter,
			LocalDateTime createdBefore) {
		
		return (root, query, cb) -> {

			List<Predicate> predicates = new ArrayList<>();

			predicates.add(cb.isNull(root.get("deletedAt")));

			if (companyId != null) {
				predicates.add(cb.equal(root.get("companyId"), companyId));
			}

			if (productId != null) {
				predicates.add(cb.equal(root.get("productId"), productId));
			}

			if (productVariantId != null) {
				predicates.add(cb.equal(root.get("productVariantId"), productVariantId));
			}

			if (active != null) {
				predicates.add(cb.equal(root.get("active"), active));
			}

			if (primary != null) {
				predicates.add(cb.equal(root.get("primary"), primary));
			}

			if (imageType != null && !imageType.isBlank()) {
				predicates.add(cb.like(cb.lower(root.get("imageType")), "%" + imageType.toLowerCase() + "%"));
			}

			if (altText != null && !altText.isBlank()) {
				predicates.add(cb.like(cb.lower(root.get("altText")), "%" + altText.toLowerCase() + "%"));
			}

			if (createdAfter != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdAfter));
			}
			if (createdBefore != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdBefore));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
