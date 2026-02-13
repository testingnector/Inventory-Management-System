package com.nector.catalogservice.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nector.catalogservice.dto.request.internal.ProductImageCreateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.entity.ProductImage;
import com.nector.catalogservice.mapper.ProductImageMapping;
import com.nector.catalogservice.repository.ProductImageRepository;
import com.nector.catalogservice.service.ProductImageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;

    @Override
    @Transactional
    public ApiResponse<ProductImage> createProductImage(ProductImageCreateRequest request, UUID createdBy) {

        if ((request.getProductId() == null && request.getProductVariantId() == null)
                || (request.getProductId() != null && request.getProductVariantId() != null)) {
            throw new IllegalArgumentException(
                    "Exactly one of productId or productVariantId must be provided");
        }
        
        ProductImage image = ProductImageMapping.toEntityWithCreation(request, createdBy);
        ProductImage savedImage = productImageRepository.save(image);

        return new ApiResponse<>(true, "Product image created successfully", "OK", 200, savedImage);
    }

}
