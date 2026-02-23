package com.nector.catalogservice.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.nector.catalogservice.entity.ProductTaxMapping;

import jakarta.persistence.criteria.Predicate;

public class ProductTaxMappingSpecification {

	public static Specification<ProductTaxMapping> search(UUID companyId, UUID productId, UUID variantId,
			UUID taxCategoryId) {

		return (root, query, cb) -> {

			List<Predicate> predicates = new ArrayList<>();

			predicates.add(cb.isNull(root.get("deletedAt")));

			if (companyId != null)
				predicates.add(cb.equal(root.get("companyId"), companyId));

			if (productId != null)
				predicates.add(cb.equal(root.get("productId"), productId));

			if (variantId != null)
				predicates.add(cb.equal(root.get("productVariantId"), variantId));

			if (taxCategoryId != null)
				predicates.add(cb.equal(root.get("companyTaxCategoryId"), taxCategoryId));

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}

}
