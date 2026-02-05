package com.nector.catalogservice.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nector.catalogservice.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

	Optional<Product> findByProductCode(String productCode);

	Optional<Product> findByIdAndDeletedAtIsNull(UUID productId);

	List<Product> findByCompanyIdAndDeletedAtIsNullAndActiveTrue(UUID companyId);

	List<Product> findByCompanyIdAndDeletedAtIsNullAndActiveFalse(UUID companyId);

	List<Product> findByIdInAndDeletedAtIsNull(List<UUID> productIds);

	Optional<Product> findByProductCodeAndDeletedAtIsNull(String productCode);

	List<Product> findByIdInAndCompanyIdAndDeletedAtIsNull(List<UUID> productIds, UUID companyId);

}
