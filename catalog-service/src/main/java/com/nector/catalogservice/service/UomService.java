package com.nector.catalogservice.service;

import java.util.List;
import java.util.UUID;

import com.nector.catalogservice.dto.request.internal.UomCreateRequest;
import com.nector.catalogservice.dto.request.internal.UomUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.UomResponse;

import jakarta.validation.Valid;

public interface UomService {

	ApiResponse<UomResponse> createUom(@Valid UomCreateRequest request, UUID createdBy);

	ApiResponse<UomResponse> updateUom(UUID uomId, @Valid UomUpdateRequest request, UUID updatedBy);

	ApiResponse<List<Object>> deleteUom(UUID uomId, UUID deletedBy);

	ApiResponse<UomResponse> getUomById(UUID uomId);

	ApiResponse<UomResponse> getUomByCode(String uomCode);

	ApiResponse<List<UomResponse>> getAllUomWithStatus(boolean activeStatus);

	ApiResponse<List<UomResponse>> getAllUoms();

	ApiResponse<List<UomResponse>> getBaseUoms();

	ApiResponse<List<UomResponse>> getDerivedUomsByBaseUomId(UUID baseUomId);

}
