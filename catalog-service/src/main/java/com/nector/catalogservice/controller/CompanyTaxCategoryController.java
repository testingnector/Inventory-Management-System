package com.nector.catalogservice.controller;

import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nector.catalogservice.dto.request.internal.CompanyTaxCategoryCreateRequest;
import com.nector.catalogservice.dto.request.internal.CompanyTaxCategoryUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CompanyCompanyTaxCategoryCurrentResponse;
import com.nector.catalogservice.dto.response.internal.CompanyCompanyTaxCategoryHistoryResponse;
import com.nector.catalogservice.dto.response.internal.CompanyTaxCategoryPageResponse;
import com.nector.catalogservice.dto.response.internal.CompanyTaxCategoryResponseWithTaxMasterAndCompany;
import com.nector.catalogservice.dto.response.internal.CompanyTaxMasterCompanyTaxCategoryHistory;
import com.nector.catalogservice.dto.response.internal.CompanyWithTaxCategoriesResponse;
import com.nector.catalogservice.dto.response.internal.PagedResponse;
import com.nector.catalogservice.service.CompanyTaxCategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/company-tax-categories")
@RequiredArgsConstructor
@Validated
public class CompanyTaxCategoryController {

	private final CompanyTaxCategoryService companyTaxCategoryService;

	@PostMapping("/insert")
	public ResponseEntity<ApiResponse<CompanyTaxCategoryResponseWithTaxMasterAndCompany>> createCompanyTaxCategory(
			@Valid @RequestBody CompanyTaxCategoryCreateRequest request, @RequestHeader("X-User-Id") UUID createdBy) {

		ApiResponse<CompanyTaxCategoryResponseWithTaxMasterAndCompany> response = companyTaxCategoryService
				.createCompanyTaxCategory(request, createdBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<CompanyTaxCategoryResponseWithTaxMasterAndCompany>> updateCompanyTaxCategory(
			@PathVariable UUID id, @Valid @RequestBody CompanyTaxCategoryUpdateRequest request,
			@RequestHeader("X-User-Id") UUID updatedBy) {

		ApiResponse<CompanyTaxCategoryResponseWithTaxMasterAndCompany> response = companyTaxCategoryService
				.updateCompanyTaxCategory(id, request, updatedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<List<Object>>> deleteCompanyTaxCategory(@PathVariable UUID id,
			@RequestHeader("X-User-Id") UUID deletedBy) {

		ApiResponse<List<Object>> response = companyTaxCategoryService.deleteCompanyTaxCategory(id, deletedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<CompanyTaxCategoryResponseWithTaxMasterAndCompany>> getById(
			@PathVariable UUID id) {

		ApiResponse<CompanyTaxCategoryResponseWithTaxMasterAndCompany> response = companyTaxCategoryService
				.getCompanyTaxCategoryById(id);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/company/{companyId}/tax/{taxMasterId}")
	public ResponseEntity<ApiResponse<CompanyTaxCategoryResponseWithTaxMasterAndCompany>> getByCompanyAndTax(
			@PathVariable UUID companyId, @PathVariable UUID taxMasterId) {

		ApiResponse<CompanyTaxCategoryResponseWithTaxMasterAndCompany> response = companyTaxCategoryService
				.getByCompanyAndTax(companyId, taxMasterId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/company/{companyId}/tax/{taxMasterId}/history")
	public ResponseEntity<ApiResponse<CompanyTaxMasterCompanyTaxCategoryHistory>> getHistoryByCompanyAndTax(
			@PathVariable UUID companyId, @PathVariable UUID taxMasterId) {

		ApiResponse<CompanyTaxMasterCompanyTaxCategoryHistory> response = companyTaxCategoryService
				.getHistoryByCompanyAndTax(companyId, taxMasterId);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/company/{companyId}")
	public ResponseEntity<ApiResponse<CompanyCompanyTaxCategoryCurrentResponse>> getAllCurrentByCompany(
			@PathVariable UUID companyId) {

		ApiResponse<CompanyCompanyTaxCategoryCurrentResponse> response = companyTaxCategoryService
				.getAllCurrentByCompany(companyId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/company/{companyId}/history")
	public ResponseEntity<ApiResponse<CompanyCompanyTaxCategoryHistoryResponse>> getHistoryByCompany(
			@PathVariable UUID companyId) {

		ApiResponse<CompanyCompanyTaxCategoryHistoryResponse> response = companyTaxCategoryService
				.getHistoryByCompany(companyId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping
	public ResponseEntity<ApiResponse<PagedResponse<CompanyWithTaxCategoriesResponse>>> getAllTaxCategories(
			@RequestParam(value = "companyId", required = false) UUID companyId,
			@RequestParam(value = "active", required = false) Boolean active,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "20") int size,
			@RequestParam(value = "sort", defaultValue = "effectiveFrom,desc") String sort) {

		ApiResponse<PagedResponse<CompanyWithTaxCategoriesResponse>> response = companyTaxCategoryService
				.getAllTaxCategories(companyId, active, page, size, sort);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

}
