package com.nector.catalogservice.service;

import java.util.List;
import java.util.UUID;

import com.nector.catalogservice.dto.request.internal.CreateTaxComponentRequest;
import com.nector.catalogservice.dto.request.internal.TaxComponentUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CompanyTaxCategoryWithComponentsResponse;
import com.nector.catalogservice.dto.response.internal.PagedResponse;
import com.nector.catalogservice.dto.response.internal.TaxCalculationResponse;
import com.nector.catalogservice.dto.response.internal.TaxComponentResponseWithCompanyTaxCategory;

import jakarta.validation.Valid;

public interface TaxComponentService {

	ApiResponse<TaxComponentResponseWithCompanyTaxCategory> createTaxComponent(@Valid CreateTaxComponentRequest request, UUID userId);

	ApiResponse<TaxComponentResponseWithCompanyTaxCategory> updateTaxComponent(UUID taxComponentId, @Valid TaxComponentUpdateRequest request,
			UUID userId);

	ApiResponse<List<Object>> deleteTaxComponent(UUID taxComponentId, UUID userId);

	ApiResponse<TaxComponentResponseWithCompanyTaxCategory> getActiveTaxComponentById(UUID taxComponentId);

	ApiResponse<CompanyTaxCategoryWithComponentsResponse> getByCompanyTaxCategoryId(UUID companyTaxCategoryId);

	ApiResponse<List<TaxComponentResponseWithCompanyTaxCategory>> getAllTaxComponentsWithStatus(boolean activeStatus);

	ApiResponse<PagedResponse<TaxComponentResponseWithCompanyTaxCategory>> getAllTaxComponents(int page, int size, String sort, Boolean active);

	ApiResponse<TaxCalculationResponse> calculateTax(UUID companyTaxCategoryId, Double baseAmount);


}
