package com.nector.catalogservice.specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.nector.catalogservice.entity.ProductVariant;

import jakarta.persistence.criteria.Predicate;

public class ProductVariantSpecification {

	public static Specification<ProductVariant> filterVariants(UUID companyId, UUID productId, Boolean active,
			Boolean serialized, Boolean batchTracked, String search, BigDecimal minPrice, BigDecimal maxPrice) {

		return (root, query, cb) -> {

			List<Predicate> predicates = new ArrayList<>();

			predicates.add(cb.isNull(root.get("deletedAt")));

			if (companyId != null) {
				predicates.add(cb.equal(root.get("companyId"), companyId));
			}

			if (productId != null) {
				predicates.add(cb.equal(root.get("productId"), productId));
			}

			if (active != null) {
				predicates.add(cb.equal(root.get("active"), active));
			}

			if (serialized != null) {
				predicates.add(cb.equal(root.get("serialized"), serialized));
			}

			if (batchTracked != null) {
				predicates.add(cb.equal(root.get("batchTracked"), batchTracked));
			}

			if (search != null && !search.isBlank()) {
				String likePattern = "%" + search.toLowerCase() + "%";

				Predicate searchPredicate = cb.or(
						cb.like(cb.lower(root.get("skuCode")), likePattern),
						cb.like(cb.lower(root.get("variantName")), likePattern),
						cb.like(cb.lower(root.get("color")), likePattern),
						cb.like(cb.lower(root.get("size")), likePattern));

				predicates.add(searchPredicate);
			}

			if (minPrice != null) {
				predicates.add(cb.greaterThanOrEqualTo(root.get("sellingPrice"), minPrice));
			}

			if (maxPrice != null) {
				predicates.add(cb.lessThanOrEqualTo(root.get("sellingPrice"), maxPrice));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}
