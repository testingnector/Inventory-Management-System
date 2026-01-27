package com.nector.orgservice.service;

import java.util.UUID;

import com.nector.orgservice.dto.request.internal.BranchCreateRequestDto;
import com.nector.orgservice.dto.response.internal.ApiResponse;
import com.nector.orgservice.dto.response.internal.BranchCompanyResponseDto1;
import com.nector.orgservice.dto.response.internal.CompanyBranchResponseDto1;
import com.nector.orgservice.dto.response.internal.CompanyBranchesResponseDto1;

public interface BranchService {

	ApiResponse<CompanyBranchResponseDto1> createBranch(BranchCreateRequestDto dto, UUID createdBy);

	ApiResponse<BranchCompanyResponseDto1> getBranchById(UUID id);

	ApiResponse<CompanyBranchesResponseDto1> getBranchesByCompany(UUID companyId);
}

