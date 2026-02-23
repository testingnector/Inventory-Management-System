package com.nector.catalogservice.service;

import java.util.List;
import java.util.UUID;

import com.nector.catalogservice.dto.request.internal.ProductTaxMappingBulkCreateRequest;
import com.nector.catalogservice.dto.request.internal.ProductTaxMappingCreateRequest;
import com.nector.catalogservice.dto.request.internal.ProductTaxMappingUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface ProductTaxMappingService {

	ApiResponse<?> createProductTaxMapping(@Valid ProductTaxMappingCreateRequest request, UUID userId);

	ApiResponse<?> updateProductTaxMapping(UUID companyId, UUID id, @Valid ProductTaxMappingUpdateRequest request, UUID userId);

	ApiResponse<?> deleteProductTaxMapping(UUID id, UUID companyId, UUID userId);

	ApiResponse<?> getByCompanyAndId(UUID id, UUID companyId);

	ApiResponse<?> getByCompanyIdAndProductId(UUID companyId, UUID productId);

	ApiResponse<?> getByCompanyIdAndVariantId(UUID companyId, UUID variantId);

	ApiResponse<?> getByCompanyIdAndTaxCategoryId(UUID companyId, UUID taxCategoryId);

	ApiResponse<?> getAllByCompanyId(UUID companyId, int page, int size);

	ApiResponse<List<Object>> createBulk(ProductTaxMappingBulkCreateRequest request, UUID userId);

	ApiResponse<Void> deleteBulk(List<@NotNull(message = "Mapping ID cannot be null") UUID> ids, UUID userId);

	ApiResponse<Boolean> existsMapping(UUID companyId, UUID productId, UUID variantId, UUID taxCategoryId);

	ApiResponse<?> searchProductTaxMappings(UUID companyId, UUID productId, UUID variantId, UUID taxCategoryId,
			boolean includeInactiveCompanies, boolean includeInactiveProducts, boolean includeInactiveVariants,
			boolean includeInactiveCompanyTaxCategories, int page, int size, String sortBy, String sortDir);

}
