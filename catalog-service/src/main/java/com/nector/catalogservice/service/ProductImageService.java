package com.nector.catalogservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.nector.catalogservice.dto.request.internal.BulkProductImageUpdateRequest;
import com.nector.catalogservice.dto.request.internal.ProductImageCreateRequest;
import com.nector.catalogservice.dto.request.internal.ProductImageUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.ProductImageResponseWithCompanyProductProductVariant;
import com.nector.catalogservice.dto.response.internal.ProductResponseWithCompanyImagesVariants;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponseWithCompanyImagesProduct;

public interface ProductImageService {


	ApiResponse<ProductImageResponseWithCompanyProductProductVariant> saveAndUploadProductImage(
			ProductImageCreateRequest request, UUID createdBy);

	ApiResponse<ProductImageResponseWithCompanyProductProductVariant> updateProductImage(UUID id,
			UUID companyId, ProductImageUpdateRequest request, UUID updatedBy);

	ApiResponse<Void> deleteProductImage(UUID id, UUID companyId, UUID deletedBy);

	ApiResponse<ProductImageResponseWithCompanyProductProductVariant> getByProductImageId(UUID id, UUID companyId);

	ApiResponse<?> getByProductOrVariantId(UUID productId,
			UUID productVariantId);

	ApiResponse<ProductResponseWithCompanyImagesVariants> getByCompanyIdAndProductId(UUID companyId, UUID productId);

	ApiResponse<ProductVariantResponseWithCompanyImagesProduct> getByCompanyIdAndVariantId(UUID companyId, UUID variantId);

	ApiResponse<?> getPrimaryImageByCompanyAndProductOrVariant(UUID companyId, UUID productId, UUID productVariantId);

//	ApiResponse<Page<ProductImageResponseWithCompanyProductProductVariant>> searchProductImages(UUID companyId,
//			UUID productId, UUID productVariantId, Boolean active, Boolean primary, String imageType, String altText,
//			LocalDateTime createdAfter, LocalDateTime createdBefore, int page, int size, String sortBy, String sortDir);

	ApiResponse<Page<ProductImageResponseWithCompanyProductProductVariant>> searchProductImages(UUID companyId,
			UUID productId, UUID variantId, Boolean active, Boolean primary, boolean includeInactiveCompanies,
			boolean includeInactiveProducts, boolean includeInactiveVariants, int page, int size, String sortBy,
			String sortDir);

	ApiResponse<List<ProductImageResponseWithCompanyProductProductVariant>> bulkUploadProductImages(
			List<ProductImageCreateRequest> requests, UUID createdBy);

	ApiResponse<List<ProductImageResponseWithCompanyProductProductVariant>> bulkUpdateProductImages(
			UUID companyId, List<BulkProductImageUpdateRequest> requests, UUID updatedBy);



}
