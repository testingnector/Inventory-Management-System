package com.nector.catalogservice.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.nector.catalogservice.dto.request.internal.BulkSubCategoryStatusRequest;
import com.nector.catalogservice.dto.request.internal.SubCategoryCreateRequest;
import com.nector.catalogservice.dto.request.internal.SubCategoryUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CategorySubCategoriesResponse;
import com.nector.catalogservice.dto.response.internal.SubCategoryCategoryResponse;

import jakarta.validation.Valid;

public interface SubCategoryService {

	ApiResponse<SubCategoryCategoryResponse> createSubCategory(
			@Valid SubCategoryCreateRequest subCategoryCreateRequest, UUID createdBy);

	ApiResponse<SubCategoryCategoryResponse> updateSubCategory(UUID subCategoryId,
			@Valid SubCategoryUpdateRequest subCategoryUpdateRequest, UUID updatedBy);

	ApiResponse<List<Object>> deleteSubCategory(UUID subCategoryId, UUID deletedBy);

	ApiResponse<SubCategoryCategoryResponse> getSubCategoryBySubCategoryId(UUID subCategoryId);

	ApiResponse<CategorySubCategoriesResponse> getAllActiveSubCategoriesByCategoryId(UUID categoryId);

	ApiResponse<CategorySubCategoriesResponse> getAllInactiveSubCategoriesByCategoryId(UUID categoryId);

	ApiResponse<CategorySubCategoriesResponse> bulkUpdateSubCategoryStatusByCategory(UUID categoryId,
			BulkSubCategoryStatusRequest request, boolean activeStatus, UUID updatedBy);

	ApiResponse<List<Object>> bulkDeleteSubCategoriesByCategory(UUID categoryId,
			@Valid BulkSubCategoryStatusRequest request, UUID deletedBy);

	ApiResponse<Page<SubCategoryCategoryResponse>> getSubCategories(Boolean active, int page, int size);

	ApiResponse<SubCategoryCategoryResponse> getSubCategoryByCode(String subCategoryCode);

}
