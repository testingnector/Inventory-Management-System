package com.nector.orgservice.service;

import java.util.UUID;

import com.nector.orgservice.dto.request.internal.BranchCreateRequestDto;
import com.nector.orgservice.dto.request.internal.BranchUpdateRequestDto;
import com.nector.orgservice.dto.response.internal.ApiResponse;
import com.nector.orgservice.dto.response.internal.BranchCompanyResponseDto1;
import com.nector.orgservice.dto.response.internal.CompanyBranchResponseDto1;
import com.nector.orgservice.dto.response.internal.CompanyBranchesResponseDto1;

public interface BranchService {

	ApiResponse<CompanyBranchResponseDto1> createBranch(BranchCreateRequestDto dto, UUID createdBy);

	ApiResponse<BranchCompanyResponseDto1> getBranchById(UUID id);

	ApiResponse<CompanyBranchesResponseDto1> getBranchesByCompany(UUID companyId);

	ApiResponse<BranchCompanyResponseDto1> updateBranch(UUID branchId, BranchUpdateRequestDto dto, UUID updatedBy);

	ApiResponse<Void> deleteBranch(UUID id, UUID deletedBy);

	ApiResponse<BranchCompanyResponseDto1> getBranchByCode(String branchCode);

	ApiResponse<BranchCompanyResponseDto1> getHeadOfficeByCompanyId(UUID companyId);

	ApiResponse<Void> changeHeadOffice(UUID branchId, UUID updatedBy);


}
