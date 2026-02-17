package com.nector.catalogservice.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nector.catalogservice.entity.ProductTaxMapping;

public interface ProductTaxMappingRepository extends JpaRepository<ProductTaxMapping, UUID> {

	Optional<ProductTaxMapping> findByIdAndDeletedAtIsNull(UUID id);

	List<ProductTaxMapping> findByCompanyIdAndProductIdAndDeletedAtIsNull(UUID companyId, UUID productId);

	List<ProductTaxMapping> findByCompanyIdAndProductVariantIdAndDeletedAtIsNull(UUID companyId, UUID variantId);

	boolean existsByCompanyIdAndProductIdAndCompanyTaxCategoryIdAndDeletedAtIsNull(UUID companyId, UUID productId,
			UUID taxCategoryId);

	boolean existsByCompanyIdAndProductVariantIdAndCompanyTaxCategoryIdAndDeletedAtIsNull(UUID companyId,
			UUID variantId, UUID taxCategoryId);

	List<ProductTaxMapping> findAllByCompanyIdAndProductIdAndDeletedAtIsNull(UUID companyId, UUID productId);
}
