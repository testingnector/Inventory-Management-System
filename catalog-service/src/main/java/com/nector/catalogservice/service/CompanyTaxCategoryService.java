package com.nector.catalogservice.service;

import java.util.List;
import java.util.UUID;

import com.nector.catalogservice.dto.request.internal.CompanyTaxCategoryCreateRequest;
import com.nector.catalogservice.dto.request.internal.CompanyTaxCategoryUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CompanyCompanyTaxCategoryCurrentResponse;
import com.nector.catalogservice.dto.response.internal.CompanyCompanyTaxCategoryHistoryResponse;
import com.nector.catalogservice.dto.response.internal.CompanyTaxCategoryPageResponse;
import com.nector.catalogservice.dto.response.internal.CompanyTaxCategoryResponseWithTaxMasterAndCompany;
import com.nector.catalogservice.dto.response.internal.CompanyTaxMasterCompanyTaxCategoryHistory;
import com.nector.catalogservice.dto.response.internal.CompanyWithTaxCategoriesResponse;
import com.nector.catalogservice.dto.response.internal.PagedResponse;

import jakarta.validation.Valid;

public interface CompanyTaxCategoryService {

	ApiResponse<CompanyTaxCategoryResponseWithTaxMasterAndCompany> createCompanyTaxCategory(CompanyTaxCategoryCreateRequest request,
			UUID createdBy);

	ApiResponse<CompanyTaxCategoryResponseWithTaxMasterAndCompany> updateCompanyTaxCategory(UUID id,
			@Valid CompanyTaxCategoryUpdateRequest request, UUID updatedBy);

	ApiResponse<List<Object>> deleteCompanyTaxCategory(UUID id, UUID deletedBy);

	ApiResponse<CompanyTaxCategoryResponseWithTaxMasterAndCompany> getCompanyTaxCategoryById(UUID id);

	ApiResponse<CompanyTaxCategoryResponseWithTaxMasterAndCompany> getByCompanyAndTax(UUID companyId, UUID taxMasterId);

	ApiResponse<CompanyTaxMasterCompanyTaxCategoryHistory> getHistoryByCompanyAndTax(UUID companyId, UUID taxMasterId);

	ApiResponse<CompanyCompanyTaxCategoryCurrentResponse> getAllCurrentByCompany(UUID companyId);
	
	ApiResponse<CompanyCompanyTaxCategoryHistoryResponse> getHistoryByCompany(UUID companyId);
	
	ApiResponse<PagedResponse<CompanyWithTaxCategoriesResponse>> getAllTaxCategories(UUID companyId, Boolean active, int page,
			int size, String sort);

}
