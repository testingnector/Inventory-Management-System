package com.nector.orgservice.service;

import java.util.List;
import java.util.UUID;

import com.nector.orgservice.dto.request.external.CompanyIdsRequestDto;
import com.nector.orgservice.dto.request.internal.CompanyCreateRequest;
import com.nector.orgservice.dto.request.internal.CompanyUpdateRequest;
import com.nector.orgservice.dto.response.external.CompanyResponseExternalDto;
import com.nector.orgservice.dto.response.internal.ApiResponse;
import com.nector.orgservice.dto.response.internal.CompanyResponse;

import jakarta.validation.Valid;

public interface CompanyService {

	ApiResponse<CompanyResponse> createCompany(@Valid CompanyCreateRequest request, UUID createdBy);

	ApiResponse<CompanyResponse> updateCompany(UUID companyId, @Valid CompanyUpdateRequest request, UUID updatedBy);

	ApiResponse<List<Object>> deleteCompany(UUID companyId);

	ApiResponse<List<CompanyResponse>> getAllCompanies();

	ApiResponse<CompanyResponse> getCompanyById(UUID companyId);

	ApiResponse<Boolean> existsCompanyById(UUID companyId);

	ApiResponse<CompanyResponseExternalDto> getCompanyBasicById(UUID companyId);

	ApiResponse<List<CompanyResponseExternalDto>> getCompaniesDetailsByCompanyIds(@Valid CompanyIdsRequestDto request);

	ApiResponse<?> getAllUsersByCompanyId(UUID companyId);

}
