package com.nector.catalogservice.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.nector.catalogservice.dto.request.internal.BulkSubCategoryStatusRequest;
import com.nector.catalogservice.dto.request.internal.SubCategoryCreateRequest;
import com.nector.catalogservice.dto.request.internal.SubCategoryUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CategorySubCategoriesResponseDto1;
import com.nector.catalogservice.dto.response.internal.SubCategoryCategoryResponseDto1;

import jakarta.validation.Valid;

public interface SubCategoryService {

	ApiResponse<SubCategoryCategoryResponseDto1> createSubCategory(
			@Valid SubCategoryCreateRequest subCategoryCreateRequest, UUID createdBy);

	ApiResponse<SubCategoryCategoryResponseDto1> updateSubCategory(UUID subCategoryId,
			@Valid SubCategoryUpdateRequest subCategoryUpdateRequest, UUID updatedBy);

	ApiResponse<List<Object>> deleteSubCategory(UUID subCategoryId, UUID deletedBy);

	ApiResponse<SubCategoryCategoryResponseDto1> getSubCategoryBySubCategoryId(UUID subCategoryId);

	ApiResponse<CategorySubCategoriesResponseDto1> getAllActiveSubCategoriesByCategoryId(UUID categoryId);

	ApiResponse<CategorySubCategoriesResponseDto1> getAllInactiveSubCategoriesByCategoryId(UUID categoryId);

	ApiResponse<CategorySubCategoriesResponseDto1> bulkUpdateSubCategoryStatusByCategory(UUID categoryId,
			BulkSubCategoryStatusRequest request, boolean activeStatus, UUID updatedBy);

	ApiResponse<List<Object>> bulkDeleteSubCategoriesByCategory(UUID categoryId,
			@Valid BulkSubCategoryStatusRequest request, UUID deletedBy);

	ApiResponse<Page<SubCategoryCategoryResponseDto1>> getSubCategories(Boolean active, int page, int size);

	ApiResponse<SubCategoryCategoryResponseDto1> getSubCategoryByCode(String subCategoryCode);

}
