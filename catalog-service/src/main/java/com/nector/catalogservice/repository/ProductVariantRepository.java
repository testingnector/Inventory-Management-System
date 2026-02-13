package com.nector.catalogservice.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nector.catalogservice.entity.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

	Optional<ProductVariant> findBySkuCodeAndCompanyId(String skuCode, UUID companyId);

	List<ProductVariant> findAllByProductIdAndCompanyIdAndActive(UUID productId, UUID companyId, Boolean active);

	Optional<ProductVariant> findByIdAndDeletedAtIsNull(UUID variantId);

	List<ProductVariant> findByProductIdAndDeletedAtIsNullAndActiveTrue(UUID productId);

	Optional<ProductVariant> findBySkuCodeAndCompanyIdAndDeletedAtIsNull(String skuCode, UUID companyId);

	List<ProductVariant> findAllByIdInAndDeletedAtIsNull(List<UUID> variantIds);

	long countByDeletedAtIsNull();

	long countByCompanyIdAndDeletedAtIsNull(UUID companyId);

	long countByProductIdAndDeletedAtIsNull(UUID productId);

	long countByCompanyIdAndProductIdAndDeletedAtIsNull(UUID companyId, UUID productId);

	long countByCompanyIdAndActiveAndDeletedAtIsNull(UUID companyId, Boolean active);

	long countByProductIdAndActiveAndDeletedAtIsNull(UUID productId, Boolean active);

	long countByCompanyIdAndProductIdAndActiveAndDeletedAtIsNull(UUID companyId, UUID productId, Boolean active);

	long countByActiveAndDeletedAtIsNull(Boolean active);

	Page<ProductVariant> findAll(Specification<ProductVariant> spec, Pageable pageable);

	List<ProductVariant> findByCompanyIdAndActiveTrueAndDeletedAtIsNull(UUID companyId);

	List<ProductVariant> findByIdInAndDeletedAtIsNull(List<UUID> variantIds);

	List<ProductVariant> findByCompanyIdAndIdInAndDeletedAtIsNull(UUID companyId, List<UUID> variantIds);

	List<ProductVariant> findByCompanyIdAndDeletedAtIsNullAndActiveTrue(UUID companyId);

	List<ProductVariant> findByCompanyIdAndDeletedAtIsNullAndActiveFalse(UUID companyId);

}
