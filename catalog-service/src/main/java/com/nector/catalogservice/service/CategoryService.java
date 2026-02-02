package com.nector.catalogservice.service;

import java.util.List;
import java.util.UUID;

import com.nector.catalogservice.dto.request.internal.BulkCategoryStatusRequest;
import com.nector.catalogservice.dto.request.internal.CategoryCreateRequest;
import com.nector.catalogservice.dto.request.internal.CategoryUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CategoryResponse;

import jakarta.validation.Valid;

public interface CategoryService {

	ApiResponse<CategoryResponse> createCategory(CategoryCreateRequest categoryCreateRequest,
			UUID createdBy);

	ApiResponse<CategoryResponse> updateCategory(UUID categoryId, @Valid CategoryUpdateRequest categoryUpdateRequest,
			UUID updatedBy);

	ApiResponse<List<Object>> deleteCategory(UUID categoryId, UUID deletedBy);

	ApiResponse<CategoryResponse> getCategoryByCategoryId(UUID categoryId);

	ApiResponse<CategoryResponse> getCategoryByCategoryCode(String categoryCode);

	ApiResponse<List<CategoryResponse>> getAllActiveCategories();

	ApiResponse<List<CategoryResponse>> getAllInactiveCategories();

	ApiResponse<List<CategoryResponse>> bulkUpdateActiveStatus(@Valid BulkCategoryStatusRequest request, boolean activeStatus,
			UUID updatedBy);

	ApiResponse<List<Object>> bulkDeleteCategories(@Valid BulkCategoryStatusRequest request,
			UUID deletedBy);


}
