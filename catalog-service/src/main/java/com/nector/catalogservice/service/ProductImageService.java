package com.nector.catalogservice.service;

import com.nector.catalogservice.dto.request.internal.ProductImageCreateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.entity.ProductImage;

import java.util.UUID;

public interface ProductImageService {

    ApiResponse<ProductImage> createProductImage(ProductImageCreateRequest request, UUID createdBy);

}
