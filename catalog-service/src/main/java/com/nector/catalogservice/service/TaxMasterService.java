package com.nector.catalogservice.service;

import java.util.List;
import java.util.UUID;

import com.nector.catalogservice.dto.request.internal.TaxMasterCreateRequest;
import com.nector.catalogservice.dto.request.internal.TaxMasterUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.PagedResponse;
import com.nector.catalogservice.dto.response.internal.TaxMasterResponse;

import jakarta.validation.Valid;

public interface TaxMasterService  {

	ApiResponse<TaxMasterResponse> createTaxMaster(@Valid TaxMasterCreateRequest request, UUID createdBy);

	ApiResponse<TaxMasterResponse> updateTaxMaster(UUID id, @Valid TaxMasterUpdateRequest request, UUID updatedBy);

	ApiResponse<List<Object>> deleteTaxMaster(UUID id, UUID deletedBy);

	ApiResponse<TaxMasterResponse> getTaxMasterById(UUID id);

	ApiResponse<TaxMasterResponse> getTaxMasterByTaxCode(String code);

	ApiResponse<List<TaxMasterResponse>> getAllTaxMasterWithStatus(boolean activeStatus);

	ApiResponse<List<TaxMasterResponse>> getAllTaxMasters();

	ApiResponse<PagedResponse<TaxMasterResponse>> getTaxMasters(String taxType, Boolean active, int page, int size,
			String sort);



}
