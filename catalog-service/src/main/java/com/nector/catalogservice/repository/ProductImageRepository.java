package com.nector.catalogservice.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nector.catalogservice.entity.ProductImage;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    List<ProductImage> findByProductIdAndDeletedAtIsNull(UUID productId);

    List<ProductImage> findByProductVariantIdAndDeletedAtIsNull(UUID productVariantId);
}
