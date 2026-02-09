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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nector.catalogservice.dto.request.internal.CreateTaxComponentRequest;
import com.nector.catalogservice.dto.request.internal.TaxComponentUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CompanyTaxCategoryWithComponentsResponse;
import com.nector.catalogservice.dto.response.internal.PagedResponse;
import com.nector.catalogservice.dto.response.internal.TaxCalculationResponse;
import com.nector.catalogservice.dto.response.internal.TaxComponentResponseWithCompanyTaxCategory;
import com.nector.catalogservice.service.TaxComponentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tax-components")
@RequiredArgsConstructor
public class TaxComponentController {

	private final TaxComponentService taxComponentService;

	@PostMapping
	public ResponseEntity<ApiResponse<TaxComponentResponseWithCompanyTaxCategory>> createTaxComponent(
			@Valid @RequestBody CreateTaxComponentRequest request, @RequestHeader("X-USER-ID") UUID userId) {

		ApiResponse<TaxComponentResponseWithCompanyTaxCategory> response = taxComponentService
				.createTaxComponent(request, userId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/{taxComponentId}")
	public ResponseEntity<ApiResponse<TaxComponentResponseWithCompanyTaxCategory>> update(
			@PathVariable UUID taxComponentId, @Valid @RequestBody TaxComponentUpdateRequest request,
			@RequestHeader("X-USER-ID") UUID userId) {

		ApiResponse<TaxComponentResponseWithCompanyTaxCategory> response = taxComponentService
				.updateTaxComponent(taxComponentId, request, userId);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@DeleteMapping("/{taxComponentId}")
	public ResponseEntity<ApiResponse<List<Object>>> delete(@PathVariable UUID taxComponentId,
			@RequestHeader("X-USER-ID") UUID userId) {

		ApiResponse<List<Object>> response = taxComponentService.deleteTaxComponent(taxComponentId, userId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/{taxComponentId}")
	public ResponseEntity<ApiResponse<TaxComponentResponseWithCompanyTaxCategory>> getActiveTaxComponentById(
			@PathVariable UUID taxComponentId) {

		ApiResponse<TaxComponentResponseWithCompanyTaxCategory> response = taxComponentService
				.getActiveTaxComponentById(taxComponentId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/company-tax-category/{companyTaxCategoryId}")
	public ResponseEntity<ApiResponse<CompanyTaxCategoryWithComponentsResponse>> getByCompanyTaxCategoryId(
			@PathVariable UUID companyTaxCategoryId) {

		ApiResponse<CompanyTaxCategoryWithComponentsResponse> response = taxComponentService
				.getByCompanyTaxCategoryId(companyTaxCategoryId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/active")
	public ResponseEntity<ApiResponse<List<TaxComponentResponseWithCompanyTaxCategory>>> getAllActiveTaxComponents() {

		ApiResponse<List<TaxComponentResponseWithCompanyTaxCategory>> response = taxComponentService
				.getAllTaxComponentsWithStatus(true);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);

	}

	@GetMapping("/inactive")
	public ResponseEntity<ApiResponse<List<TaxComponentResponseWithCompanyTaxCategory>>> getAllInactiveTaxComponents() {

		ApiResponse<List<TaxComponentResponseWithCompanyTaxCategory>> response = taxComponentService
				.getAllTaxComponentsWithStatus(false);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);

	}

	@GetMapping
	public ResponseEntity<ApiResponse<PagedResponse<TaxComponentResponseWithCompanyTaxCategory>>> getAllTaxComponents(
			@RequestParam(required = false) Boolean active, 
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, 
			@RequestParam(defaultValue = "createdAt") String sort) {

		ApiResponse<PagedResponse<TaxComponentResponseWithCompanyTaxCategory>> response = taxComponentService
				.getAllTaxComponents(page, size, sort, active);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);

	}

	@GetMapping("/calculate")
	public ResponseEntity<ApiResponse<TaxCalculationResponse>> calculateTax(
			@RequestParam("companyTaxCategoryId") String categoryIdStr, 
			@RequestParam("baseAmount") Double baseAmount) {

		UUID companyTaxCategoryId = UUID.fromString(categoryIdStr);
		ApiResponse<TaxCalculationResponse> response = taxComponentService.calculateTax(companyTaxCategoryId,
				baseAmount);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

}
