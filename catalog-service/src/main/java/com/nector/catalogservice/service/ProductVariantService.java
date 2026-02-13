package com.nector.catalogservice.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.nector.catalogservice.dto.request.internal.BulkVariantsStatusRequest;
import com.nector.catalogservice.dto.request.internal.ProductVariantBulkUpdateRequest;
import com.nector.catalogservice.dto.request.internal.ProductVariantCreateRequest;
import com.nector.catalogservice.dto.request.internal.ProductVariantPriceUpdateRequest;
import com.nector.catalogservice.dto.request.internal.ProductVariantUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CompanyProductVariantsResponse;
import com.nector.catalogservice.dto.response.internal.CompanyProductsResponse;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponse;

import jakarta.validation.Valid;

public interface ProductVariantService {

    ApiResponse<ProductVariantResponse> createProductVariant(ProductVariantCreateRequest request, UUID createdBy);

	ApiResponse<ProductVariantResponse> updateProductVariant(UUID variantId, @Valid ProductVariantUpdateRequest request,
			UUID updatedBy);

	ApiResponse<Void> deleteProductVariant(UUID variantId, UUID deletedBy);

	ApiResponse<ProductVariantResponse> getVariantByVariantId(UUID variantId);

	ApiResponse<?> getVariantsByProductId(UUID productId);

	ApiResponse<Boolean> isSkuAvailable(String skuCode, UUID companyId);

	ApiResponse<List<ProductVariantResponse>> bulkUpdateProductVariants(@Valid ProductVariantBulkUpdateRequest request,
			UUID updatedBy);

	ApiResponse<Void> bulkDeleteVariants(List<UUID> variantIds, UUID deletedBy);

	ApiResponse<ProductVariantResponse> getVariantBySkuAndCompanyId(String skuCode, UUID companyId);

	ApiResponse<?> getVariantsByCompanyId(UUID companyId);
	
	ApiResponse<ProductVariantResponse> updateVariantPrice(UUID variantId,
			@Valid ProductVariantPriceUpdateRequest request, UUID updatedBy);

	ApiResponse<Long> countVariants(UUID companyId, UUID productId, Boolean active);

	ApiResponse<Page<ProductVariantResponse>> getAllVariants(UUID companyId, UUID productId, Boolean active,
			Boolean serialized, Boolean batchTracked, String search, BigDecimal minPrice, BigDecimal maxPrice, int page,
			int size, String sortBy, String sortDir, boolean includeInactiveCompanies, boolean includeInactiveProducts,
			boolean includeInactiveUoms);

	ApiResponse<?> bulkUpdateProductStatusByCompany(UUID companyId,
			@Valid BulkVariantsStatusRequest request, boolean activeStatus, UUID updatedBy);

	ApiResponse<CompanyProductVariantsResponse> getVariantsByCompanyAndStatus(UUID companyId, boolean activeStatus);


}
