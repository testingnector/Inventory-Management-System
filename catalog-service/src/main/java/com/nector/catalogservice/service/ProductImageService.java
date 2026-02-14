package com.nector.catalogservice.service;

import java.util.List;
import java.util.UUID;

import com.nector.catalogservice.dto.request.internal.ProductImageCreateRequest;
import com.nector.catalogservice.dto.request.internal.ProductImageUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.ProductImageResponseWithCompanyProductProductVariant;

public interface ProductImageService {


	ApiResponse<ProductImageResponseWithCompanyProductProductVariant> saveAndUploadProductImage(
			ProductImageCreateRequest request, UUID createdBy);

	ApiResponse<ProductImageResponseWithCompanyProductProductVariant> updateProductImage(UUID id,
			ProductImageUpdateRequest request, UUID updatedBy);

	ApiResponse<Void> deleteProductImage(UUID id, UUID deletedBy);

	ApiResponse<ProductImageResponseWithCompanyProductProductVariant> getByProductImageId(UUID id);

	ApiResponse<List<ProductImageResponseWithCompanyProductProductVariant>> getByProductOrVariantId(UUID productId,
			UUID productVariantId);


}
