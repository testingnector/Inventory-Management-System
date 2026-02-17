package com.nector.catalogservice.service;

import java.util.UUID;

import com.nector.catalogservice.dto.request.internal.ProductTaxMappingCreateRequest;
import com.nector.catalogservice.dto.request.internal.ProductTaxMappingUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;

import jakarta.validation.Valid;

public interface ProductTaxMappingService {

	ApiResponse<?> createProductTaxMapping(@Valid ProductTaxMappingCreateRequest request, UUID userId);

	ApiResponse<?> updateProductTaxMapping(UUID companyId, UUID id, @Valid ProductTaxMappingUpdateRequest request, UUID userId);

	ApiResponse<?> deleteProductTaxMapping(UUID id, UUID companyId, UUID userId);

	ApiResponse<?> getByCompanyAndId(UUID id, UUID companyId);

	ApiResponse<?> getByCompanyIdAndProductId(UUID companyId, UUID productId);

	ApiResponse<?> getByCompanyIdAndVariantId(UUID companyId, UUID variantId);


}
