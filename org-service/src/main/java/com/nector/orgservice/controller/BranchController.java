package com.nector.orgservice.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nector.orgservice.dto.request.internal.BranchCreateRequestDto;
import com.nector.orgservice.dto.response.internal.ApiResponse;
import com.nector.orgservice.dto.response.internal.BranchCompanyResponseDto1;
import com.nector.orgservice.dto.response.internal.CompanyBranchResponseDto1;
import com.nector.orgservice.dto.response.internal.CompanyBranchesResponseDto1;
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
	public ResponseEntity<ApiResponse<CompanyBranchResponseDto1>> createBranch(
			@Valid @RequestBody BranchCreateRequestDto dto, @RequestHeader("X-USER-ID") UUID createdBy,
			@RequestHeader("X-USER-ROLE") String role) {

		if (!"SUPER_ADMIN".equals(role)) {
		    throw new RuntimeException("Only SUPER_ADMIN can create company");
		}
		
		ApiResponse<CompanyBranchResponseDto1> response = branchService.createBranch(dto, createdBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<BranchCompanyResponseDto1>> getBranch(@PathVariable UUID id) {
		ApiResponse<BranchCompanyResponseDto1> response = branchService.getBranchById(id);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/company/{companyId}")
	public ResponseEntity<ApiResponse<CompanyBranchesResponseDto1>> getBranchesByCompany(@PathVariable UUID companyId) {
		ApiResponse<CompanyBranchesResponseDto1> response = branchService.getBranchesByCompany(companyId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

//	@DeleteMapping("/{id}")
//	public ResponseEntity<ApiResponse<Void>> deactivateBranch(@PathVariable UUID id) {
//		return ResponseEntity.ok(branchService.deactivateBranch(id));
//	}
}
