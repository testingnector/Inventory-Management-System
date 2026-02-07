package com.nector.catalogservice.service;

import java.util.List;
import java.util.UUID;

import com.nector.catalogservice.dto.request.internal.BulkCompanyCategoryStatusRequest;
import com.nector.catalogservice.dto.request.internal.CompanyCategoryCreateRequest;
import com.nector.catalogservice.dto.request.internal.CompanyCategoryUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CompanyCategoriesCreationResponse;
import com.nector.catalogservice.dto.response.internal.CompanyCategoriesResponse;
import com.nector.catalogservice.dto.response.internal.Company_CategoryResponse;

import jakarta.validation.Valid;

public interface CompanyCategoryService {

    ApiResponse<CompanyCategoriesCreationResponse> createCompanyCategories(CompanyCategoryCreateRequest request, UUID createdBy);

    ApiResponse<Company_CategoryResponse> updateCompanyCategory(UUID id, CompanyCategoryUpdateRequest request, UUID updatedBy);

    ApiResponse<List<Object>> deleteCompanyCategory(UUID id, UUID deletedBy);

    ApiResponse<Company_CategoryResponse> getCompanyCategoryById(UUID id);

    ApiResponse<CompanyCategoriesResponse> getAllActiveCompanyCategoriesByCompanyId(UUID companyId);

    ApiResponse<CompanyCategoriesResponse> getAllInactiveCompanyCategoriesByCompanyId(UUID companyId);

	ApiResponse<CompanyCategoriesResponse> bulkUpdateCompanyCategoryActiveStatus(
			@Valid BulkCompanyCategoryStatusRequest request, boolean b, UUID updatedBy);

	ApiResponse<List<Object>> bulkDeleteCompanyCategoriesByCompanyId(UUID companyId,
			@Valid BulkCompanyCategoryStatusRequest request, UUID deletedBy);


}
