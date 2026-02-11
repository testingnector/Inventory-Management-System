package com.nector.catalogservice.service;

import java.util.List;
import java.util.UUID;

import com.nector.catalogservice.dto.request.internal.ProductVariantBulkUpdateRequest;
import com.nector.catalogservice.dto.request.internal.ProductVariantCreateRequest;
import com.nector.catalogservice.dto.request.internal.ProductVariantUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponse;

import jakarta.validation.Valid;

public interface ProductVariantService {

    ApiResponse<ProductVariantResponse> createProductVariant(ProductVariantCreateRequest request, UUID createdBy);

	ApiResponse<ProductVariantResponse> updateProductVariant(UUID variantId, @Valid ProductVariantUpdateRequest request,
			UUID updatedBy);

	ApiResponse<Void> deleteProductVariant(UUID variantId, UUID deletedBy);

	ApiResponse<ProductVariantResponse> getVariantByVariantId(UUID variantId);

	ApiResponse<?> getVariantsByProductId(UUID productId);

	ApiResponse<List<ProductVariantResponse>> bulkUpdateProductVariants(@Valid ProductVariantBulkUpdateRequest request,
			UUID updatedBy);

	ApiResponse<Boolean> isSkuAvailable(String skuCode, UUID companyId);

	ApiResponse<Void> bulkDeleteVariants(List<UUID> variantIds, UUID deletedBy);

}
