package com.nector.orgservice.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nector.orgservice.dto.request.internal.BranchCreateRequestDto;
import com.nector.orgservice.dto.request.internal.BranchUpdateRequestDto;
import com.nector.orgservice.dto.response.internal.ApiResponse;
import com.nector.orgservice.dto.response.internal.BranchCompanyResponse;
import com.nector.orgservice.dto.response.internal.CompanyBranchResponse;
import com.nector.orgservice.dto.response.internal.CompanyBranchesResponse;
import com.nector.orgservice.service.BranchService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/branches")
@RequiredArgsConstructor
@Validated
public class BranchController {

	private final BranchService branchService;

	@PostMapping("/insert")
	public ResponseEntity<ApiResponse<CompanyBranchResponse>> createBranch(
			@Valid @RequestBody BranchCreateRequestDto dto, @RequestHeader("X-USER-ID") UUID createdBy,
			@RequestHeader("X-USER-ROLE") String role) {

		if (!"SUPER_ADMIN".equals(role)) {
			throw new RuntimeException("Only SUPER_ADMIN can create company");
		}

		ApiResponse<CompanyBranchResponse> response = branchService.createBranch(dto, createdBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<BranchCompanyResponse>> getBranch(@PathVariable UUID id) {
		ApiResponse<BranchCompanyResponse> response = branchService.getBranchById(id);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/company/{companyId}")
	public ResponseEntity<ApiResponse<CompanyBranchesResponse>> getBranchesByCompany(@PathVariable UUID companyId) {
		ApiResponse<CompanyBranchesResponse> response = branchService.getBranchesByCompany(companyId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<BranchCompanyResponse>> updateBranch(@PathVariable UUID id,
			@Valid @RequestBody BranchUpdateRequestDto dto, @RequestHeader("X-USER-ID") UUID updatedBy,
			@RequestHeader("X-USER-ROLE") String role) {

		if (!"SUPER_ADMIN".equals(role)) {
			throw new RuntimeException("Only SUPER_ADMIN can update branch");
		}

		ApiResponse<BranchCompanyResponse> response = branchService.updateBranch(id, dto, updatedBy);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> deleteBranchByBranchId(@PathVariable UUID id,
			@RequestHeader("X-USER-ID") UUID deletedBy, @RequestHeader("X-USER-ROLE") String role) {

		if (!"SUPER_ADMIN".equals(role)) {
			throw new RuntimeException("Only SUPER_ADMIN can delete branch");
		}

		ApiResponse<Void> response = branchService.deleteBranch(id, deletedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/code/{branchCode}")
	public ResponseEntity<ApiResponse<BranchCompanyResponse>> getBranchByCode(@PathVariable String branchCode) {

		ApiResponse<BranchCompanyResponse> response = branchService.getBranchByCode(branchCode);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/company/{companyId}/head-office")
	public ResponseEntity<ApiResponse<BranchCompanyResponse>> getHeadOfficeByCompany(@PathVariable UUID companyId) {

		ApiResponse<BranchCompanyResponse> response = branchService.getHeadOfficeByCompanyId(companyId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/{branchId}/make-head-office")
	public ResponseEntity<ApiResponse<Void>> changeHeadOfficeByBranchId(@PathVariable UUID branchId,
			@RequestHeader("X-USER-ID") UUID userId, @RequestHeader("X-USER-ROLE") String role) {

		if (!"SUPER_ADMIN".equals(role)) {
			throw new RuntimeException("Only SUPER_ADMIN can change head office");
		}

		ApiResponse<Void> response = branchService.changeHeadOffice(branchId, userId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

}
