package com.nector.orgservice.service;

import java.util.List;
import java.util.UUID;

import com.nector.orgservice.dto.request.CompanyCreateRequest;
import com.nector.orgservice.dto.request.CompanyUpdateRequest;
import com.nector.orgservice.dto.request.external.CompanyIdsRequest;
import com.nector.orgservice.dto.response.ApiResponse;
import com.nector.orgservice.dto.response.CompanyResponse;
import com.nector.orgservice.dto.response.external.CompanyBasicResponse;

import jakarta.validation.Valid;

public interface CompanyService {

	ApiResponse<CompanyResponse> createCompany(@Valid CompanyCreateRequest request, UUID createdBy);

	ApiResponse<CompanyResponse> updateCompany(UUID companyId, @Valid CompanyUpdateRequest request, UUID updatedBy);

	ApiResponse<List<Object>> deleteCompany(UUID companyId);

	ApiResponse<List<CompanyResponse>> getAllCompanies();

	ApiResponse<CompanyResponse> getCompanyById(UUID companyId);

	ApiResponse<Boolean> existsCompanyById(UUID companyId);

	ApiResponse<CompanyBasicResponse> getCompanyBasicById(UUID companyId);

	ApiResponse<List<CompanyBasicResponse>> getCompanyBasicByCompanyIds(@Valid CompanyIdsRequest request);

}
