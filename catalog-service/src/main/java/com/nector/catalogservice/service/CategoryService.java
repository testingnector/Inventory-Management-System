package com.nector.catalogservice.service;

import java.util.List;
import java.util.UUID;

import com.nector.catalogservice.dto.request.internal.BulkCategoryStatusRequest;
import com.nector.catalogservice.dto.request.internal.CategoryCreateRequest;
import com.nector.catalogservice.dto.request.internal.CategoryUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CategoryCompanyResponseDto1;
import com.nector.catalogservice.dto.response.internal.CompanyCategoriesResponseDto1;

import jakarta.validation.Valid;

public interface CategoryService {

	ApiResponse<CategoryCompanyResponseDto1> createCategory(CategoryCreateRequest categoryCreateRequest,
			UUID createdBy);

	ApiResponse<CategoryCompanyResponseDto1> updateCategory(UUID categoryId, @Valid CategoryUpdateRequest categoryUpdateRequest,
			UUID updatedBy);

	ApiResponse<List<Object>> deleteCategory(UUID categoryId, UUID deletedBy);

	ApiResponse<CategoryCompanyResponseDto1> getCategoryByCategoryId(UUID categoryId);

	ApiResponse<CategoryCompanyResponseDto1> getCategoryByCategoryCode(String categoryCode);

	ApiResponse<CompanyCategoriesResponseDto1> getActiveCategoriesByCompanyId(UUID companyId);

	ApiResponse<CompanyCategoriesResponseDto1> getInactiveCategoriesByCompanyId(UUID companyId);

	ApiResponse<CompanyCategoriesResponseDto1> bulkUpdateActiveStatus(@Valid BulkCategoryStatusRequest request, boolean activeStatus,
			UUID updatedBy);

	ApiResponse<CompanyCategoriesResponseDto1> bulkDeleteCategories(@Valid BulkCategoryStatusRequest request,
			UUID deletedBy);


}
