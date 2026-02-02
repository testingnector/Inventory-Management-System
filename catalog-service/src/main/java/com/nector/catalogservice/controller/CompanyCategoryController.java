package com.nector.catalogservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nector.catalogservice.dto.request.internal.BulkCompanyCategoryStatusRequest;
import com.nector.catalogservice.dto.request.internal.CompanyCategoryCreateRequest;
import com.nector.catalogservice.dto.request.internal.CompanyCategoryUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CompanyCCsCategoriesResponseDto1;
import com.nector.catalogservice.dto.response.internal.CompanyCategoriesResponseDto1;
import com.nector.catalogservice.dto.response.internal.Company_CategoryResponse;
import com.nector.catalogservice.service.CompanyCategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/company-categories")
@RequiredArgsConstructor
public class CompanyCategoryController {

	private final CompanyCategoryService companyCategoryService;

	@PostMapping("/add")
	public ResponseEntity<ApiResponse<CompanyCategoriesResponseDto1>> createCompanyCategory(
			@Valid @RequestBody CompanyCategoryCreateRequest request, @RequestHeader("X-USER-ID") UUID createdBy) {

		ApiResponse<CompanyCategoriesResponseDto1> response = companyCategoryService.createCompanyCategories(request,
				createdBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<Company_CategoryResponse>> updateCompanyCategoryById(@PathVariable UUID id,
			@Valid @RequestBody CompanyCategoryUpdateRequest request, @RequestHeader("X-USER-ID") UUID updatedBy) {

		ApiResponse<Company_CategoryResponse> response = companyCategoryService.updateCompanyCategory(id, request,
				updatedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<List<Object>>> deleteCompanyCategory(@PathVariable UUID id,
			@RequestHeader("X-USER-ID") UUID deletedBy) {

		ApiResponse<List<Object>> response = companyCategoryService.deleteCompanyCategory(id, deletedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<Company_CategoryResponse>> getCompanyCategoryById(@PathVariable UUID id) {

		ApiResponse<Company_CategoryResponse> response = companyCategoryService.getCompanyCategoryById(id);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/company/{companyId}/active")
	public ResponseEntity<ApiResponse<CompanyCCsCategoriesResponseDto1>> getAllActiveCompanyCategoriesByCompanyId(
			@PathVariable UUID companyId) {

		ApiResponse<CompanyCCsCategoriesResponseDto1> response = companyCategoryService
				.getAllActiveCompanyCategoriesByCompanyId(companyId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/company/{companyId}/inactive")
	public ResponseEntity<ApiResponse<CompanyCCsCategoriesResponseDto1>> getAllInactiveCompanyCategoriesByCompanyId(
			@PathVariable UUID companyId) {

		ApiResponse<CompanyCCsCategoriesResponseDto1> response = companyCategoryService
				.getAllInactiveCompanyCategoriesByCompanyId(companyId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/company/{companyId}/bulk-activate")
	public ResponseEntity<ApiResponse<CompanyCCsCategoriesResponseDto1>> bulkActivate(
			@Valid @RequestBody BulkCompanyCategoryStatusRequest request, @RequestHeader("X-USER-ID") UUID updatedBy) {

		ApiResponse<CompanyCCsCategoriesResponseDto1> response = companyCategoryService.bulkUpdateCompanyCategoryActiveStatus(request, true, updatedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/company/{companyId}/bulk-deactivate")
	public ResponseEntity<ApiResponse<CompanyCCsCategoriesResponseDto1>> bulkDeactivate(
			@Valid @RequestBody BulkCompanyCategoryStatusRequest request, @RequestHeader("X-USER-ID") UUID updatedBy) {

		ApiResponse<CompanyCCsCategoriesResponseDto1> response = companyCategoryService.bulkUpdateCompanyCategoryActiveStatus(request, false,
				updatedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}
}
