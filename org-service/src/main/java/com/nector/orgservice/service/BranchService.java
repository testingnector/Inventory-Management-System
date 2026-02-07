package com.nector.orgservice.service;

import java.util.UUID;

import com.nector.orgservice.dto.request.internal.BranchCreateRequestDto;
import com.nector.orgservice.dto.request.internal.BranchUpdateRequestDto;
import com.nector.orgservice.dto.response.internal.ApiResponse;
import com.nector.orgservice.dto.response.internal.BranchCompanyResponse;
import com.nector.orgservice.dto.response.internal.CompanyBranchResponse;
import com.nector.orgservice.dto.response.internal.CompanyBranchesResponse;

public interface BranchService {

	ApiResponse<CompanyBranchResponse> createBranch(BranchCreateRequestDto dto, UUID createdBy);

	ApiResponse<BranchCompanyResponse> getBranchById(UUID id);

	ApiResponse<CompanyBranchesResponse> getBranchesByCompany(UUID companyId);

	ApiResponse<BranchCompanyResponse> updateBranch(UUID branchId, BranchUpdateRequestDto dto, UUID updatedBy);

	ApiResponse<Void> deleteBranch(UUID id, UUID deletedBy);

	ApiResponse<BranchCompanyResponse> getBranchByCode(String branchCode);

	ApiResponse<BranchCompanyResponse> getHeadOfficeByCompanyId(UUID companyId);

	ApiResponse<Void> changeHeadOffice(UUID branchId, UUID updatedBy);


}
