package com.nector.catalogservice.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nector.catalogservice.entity.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

    Optional<ProductVariant> findBySkuCodeAndCompanyId(String skuCode, UUID companyId);

    List<ProductVariant> findAllByProductIdAndCompanyIdAndIsActive(UUID productId, UUID companyId, Boolean active);
}
