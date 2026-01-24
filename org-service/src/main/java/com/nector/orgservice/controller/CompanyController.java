package com.nector.orgservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
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

import com.nector.orgservice.dto.request.CompanyCreateRequest;
import com.nector.orgservice.dto.request.CompanyUpdateRequest;
import com.nector.orgservice.dto.request.external.CompanyIdsRequestDto;
import com.nector.orgservice.dto.response.ApiResponse;
import com.nector.orgservice.dto.response.CompanyResponse;
import com.nector.orgservice.dto.response.external.CompanyResponseExternalDto;
import com.nector.orgservice.service.CompanyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
@Validated
public class CompanyController {

	private final CompanyService companyService;

	// Create
	@PostMapping("/insert")
	public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(@Valid @RequestBody CompanyCreateRequest request,
			@RequestHeader("X-USER-ID") UUID createdBy, @RequestHeader("X-USER-ROLE") String role) {

		if (!"SUPER_ADMIN".equals(role)) {
		    throw new RuntimeException("Only SUPER_ADMIN can create company");
		}
		
		ApiResponse<CompanyResponse> response = companyService.createCompany(request, createdBy);
		return ResponseEntity.status(HttpStatus.resolve(response.getHttpStatusCode())).body(response);
	}

	// Update
	@PutMapping("/update/{id}")
	public ResponseEntity<ApiResponse<CompanyResponse>> updateCompany(@PathVariable("id") UUID companyId,
			@Valid @RequestBody CompanyUpdateRequest request, @RequestHeader("X-USER-ID") UUID updatedBy) {

		ApiResponse<CompanyResponse> response = companyService.updateCompany(companyId, request, updatedBy);
		return ResponseEntity.status(HttpStatus.resolve(response.getHttpStatusCode())).body(response);
		
	}

	// Delete (soft delete)
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<ApiResponse<List<Object>>> deleteCompany(@PathVariable("id") UUID companyId) {
		ApiResponse<List<Object>> response = companyService.deleteCompany(companyId);
		return ResponseEntity.status(HttpStatus.resolve(response.getHttpStatusCode())).body(response);
	}

	// Get all
	@GetMapping
	public ResponseEntity<ApiResponse<List<CompanyResponse>>> getAllCompanies() {
		ApiResponse<List<CompanyResponse>> response = companyService.getAllCompanies();
		return ResponseEntity.status(HttpStatus.resolve(response.getHttpStatusCode())).body(response);
	}

	// Get by ID
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<CompanyResponse>> getCompanyById(@PathVariable("id") UUID companyId) {
		ApiResponse<CompanyResponse> response = companyService.getCompanyById(companyId);
		return ResponseEntity.status(HttpStatus.resolve(response.getHttpStatusCode())).body(response);
	}
	
	@GetMapping("{companyId}/users")
	public ResponseEntity<?> getAllUsersByCompanyId(@PathVariable("companyId") UUID companyId) {
		ApiResponse<?> response = companyService.getAllUsersByCompanyId(companyId);
		return ResponseEntity.status(HttpStatus.resolve(response.getHttpStatusCode())).body(response);
	}
	
	
//	---------------------FOR EXTERNAL SERVICE CALLING-------------------

	// Exists by ID
	@GetMapping("/exists/{id}")
	public ResponseEntity<ApiResponse<Boolean>> existsByCompanyId(@PathVariable("id") UUID companyId) {
		ApiResponse<Boolean> response = companyService.existsCompanyById(companyId);
		return ResponseEntity.status(HttpStatus.resolve(response.getHttpStatusCode())).body(response);
	}
	
	@GetMapping("/detail/{id}")
	public ResponseEntity<ApiResponse<CompanyResponseExternalDto>> getCompanyBasic(@PathVariable("id") UUID companyId) {
	    ApiResponse<CompanyResponseExternalDto> response = companyService.getCompanyBasicById(companyId);
	    System.out.println(response.toString());
	    return ResponseEntity.status(HttpStatus.resolve(response.getHttpStatusCode())).body(response);
	}

	@PostMapping("/details")
	public ResponseEntity<ApiResponse<List<CompanyResponseExternalDto>>> getCompaniesDetailsByCompanyIds(@Valid @RequestBody CompanyIdsRequestDto request) {
		ApiResponse<List<CompanyResponseExternalDto>> response = companyService.getCompaniesDetailsByCompanyIds(request);
		System.out.println(response.toString());
		return ResponseEntity.status(HttpStatus.resolve(response.getHttpStatusCode())).body(response);
	}

	
	
	
}
