package com.nector.catalogservice.service;

import java.util.List;
import java.util.UUID;

import com.nector.catalogservice.dto.request.internal.CompanyTaxCategoryCreateRequest;
import com.nector.catalogservice.dto.request.internal.CompanyTaxCategoryUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CompanyTaxCategoryPageResponse;
import com.nector.catalogservice.dto.response.internal.CompanyTaxCategoryResponse;
import com.nector.catalogservice.dto.response.internal.CompanyWithTaxCategoriesResponse;
import com.nector.catalogservice.dto.response.internal.PagedResponse;

import jakarta.validation.Valid;

public interface CompanyTaxCategoryService {

	ApiResponse<CompanyTaxCategoryResponse> createCompanyTaxCategory(CompanyTaxCategoryCreateRequest request,
			UUID createdBy);

	ApiResponse<CompanyTaxCategoryResponse> updateCompanyTaxCategory(UUID id,
			@Valid CompanyTaxCategoryUpdateRequest request, UUID updatedBy);

	ApiResponse<List<Object>> deleteCompanyTaxCategory(UUID id, UUID deletedBy);

	ApiResponse<CompanyTaxCategoryResponse> getCompanyTaxCategoryById(UUID id);

	ApiResponse<CompanyTaxCategoryPageResponse> getCompanyTaxCategoryByCompany(UUID companyId,
			Boolean active, int page, int size, String sort);

	ApiResponse<CompanyTaxCategoryResponse> getByCompanyAndTax(UUID companyId, UUID taxMasterId);

	ApiResponse<PagedResponse<CompanyWithTaxCategoriesResponse>> getAllTaxCategories(UUID companyId, Boolean active, int page,
			int size, String sort);

}
