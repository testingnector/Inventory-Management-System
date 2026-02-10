package com.nector.catalogservice.service;

import java.util.UUID;

import com.nector.catalogservice.dto.request.internal.ProductVariantCreateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponse;

public interface ProductVariantService {

    ApiResponse<ProductVariantResponse> createProductVariant(ProductVariantCreateRequest request, UUID createdBy);

//    ApiResponse<ProductVariantResponse> updateProductVariant(UUID variantId, ProductVariantUpdateRequest request, UUID updatedBy);
//
//    ApiResponse<List<ProductVariantResponse>> getVariantsByProduct(UUID productId, UUID companyId, Boolean active);
//
//    ApiResponse<Void> deleteProductVariant(UUID variantId, UUID deletedBy);
}
