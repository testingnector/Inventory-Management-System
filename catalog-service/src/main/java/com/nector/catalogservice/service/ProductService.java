package com.nector.catalogservice.service;

import java.util.List;
import java.util.UUID;

import com.nector.catalogservice.dto.request.internal.BulkProductStatusRequest;
import com.nector.catalogservice.dto.request.internal.ProductCreateRequest;
import com.nector.catalogservice.dto.request.internal.ProductUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CompanyProductsDetailsResponseDto1;
import com.nector.catalogservice.dto.response.internal.ProductAggregateResponse;

import jakarta.validation.Valid;

public interface ProductService {

	ApiResponse<ProductAggregateResponse> createProduct(ProductCreateRequest request, UUID createdBy);

	ApiResponse<ProductAggregateResponse> updateProduct(UUID productId, @Valid ProductUpdateRequest request,
			UUID updatedBy);

	ApiResponse<List<Object>> deleteProduct(UUID productId, UUID deletedBy);

	ApiResponse<ProductAggregateResponse> getProductByProductId(UUID productId);

	ApiResponse<CompanyProductsDetailsResponseDto1> getAllActiveProductsByCompanyId(UUID companyId);

	ApiResponse<CompanyProductsDetailsResponseDto1> getAllInactiveProductsByCompanyId(UUID companyId);

	ApiResponse<CompanyProductsDetailsResponseDto1> bulkUpdateProductStatusByCompany(UUID companyId,
			@Valid BulkProductStatusRequest request, boolean b, UUID updatedBy);

	ApiResponse<ProductAggregateResponse> getProductByProductCode(String productCode);

	ApiResponse<List<Object>> bulkDeletionOfProductsByCompanyId(UUID companyId, @Valid BulkProductStatusRequest request, UUID deletedBy);
}
