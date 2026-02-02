package com.nector.catalogservice.service;

import java.util.List;
import java.util.UUID;

import com.nector.catalogservice.dto.request.internal.BulkCompanyCategoryStatusRequest;
import com.nector.catalogservice.dto.request.internal.CompanyCategoryCreateRequest;
import com.nector.catalogservice.dto.request.internal.CompanyCategoryUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CompanyCCsCategoriesResponseDto1;
import com.nector.catalogservice.dto.response.internal.CompanyCategoriesResponseDto1;
import com.nector.catalogservice.dto.response.internal.Company_CategoryResponse;

import jakarta.validation.Valid;

public interface CompanyCategoryService {

    ApiResponse<CompanyCategoriesResponseDto1> createCompanyCategories(CompanyCategoryCreateRequest request, UUID createdBy);

    ApiResponse<Company_CategoryResponse> updateCompanyCategory(UUID id, CompanyCategoryUpdateRequest request, UUID updatedBy);

    ApiResponse<List<Object>> deleteCompanyCategory(UUID id, UUID deletedBy);

    ApiResponse<Company_CategoryResponse> getCompanyCategoryById(UUID id);

    ApiResponse<CompanyCCsCategoriesResponseDto1> getAllActiveCompanyCategoriesByCompanyId(UUID companyId);

    ApiResponse<CompanyCCsCategoriesResponseDto1> getAllInactiveCompanyCategoriesByCompanyId(UUID companyId);

	ApiResponse<CompanyCCsCategoriesResponseDto1> bulkUpdateCompanyCategoryActiveStatus(
			@Valid BulkCompanyCategoryStatusRequest request, boolean b, UUID updatedBy);


}
