package com.nector.catalogservice.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nector.catalogservice.entity.Product;
import com.nector.catalogservice.entity.ProductImage;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    List<ProductImage> findByProductIdAndDeletedAtIsNull(UUID productId);

    List<ProductImage> findByProductVariantIdAndDeletedAtIsNull(UUID productVariantId);

	Optional<ProductImage> findByProductIdAndPrimaryTrueAndDeletedAtIsNull(UUID productId);

	Optional<ProductImage> findByProductVariantIdAndPrimaryTrueAndDeletedAtIsNull(UUID productVariantId);

	Optional<ProductImage> findByProductIdAndFileHashAndDeletedAtIsNull(UUID productId, String fileHash);

	Optional<ProductImage> findByProductVariantIdAndFileHashAndDeletedAtIsNull(UUID productVariantId, String fileHash);

	Optional<ProductImage> findByIdAndDeletedAtIsNull(UUID productImageId);

	Optional<ProductImage> findByIdAndDeletedAtIsNullAndActiveTrue(UUID id);

	List<ProductImage> findByProductIdAndDeletedAtIsNullAndActiveTrue(UUID productId);

	List<ProductImage> findByProductVariantIdAndDeletedAtIsNullAndActiveTrue(UUID productVariantId);

	Page<ProductImage> findAll(Specification<ProductImage> spec, Pageable pageable);
}
