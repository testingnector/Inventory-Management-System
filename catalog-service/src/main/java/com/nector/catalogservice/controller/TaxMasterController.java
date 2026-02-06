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

import com.nector.catalogservice.dto.request.internal.TaxMasterCreateRequest;
import com.nector.catalogservice.dto.request.internal.TaxMasterUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.PagedResponse;
import com.nector.catalogservice.dto.response.internal.TaxMasterResponse;
import com.nector.catalogservice.service.TaxMasterService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tax-masters")
@RequiredArgsConstructor
@Validated
public class TaxMasterController {

	private final TaxMasterService taxMasterService;

	@PostMapping("/insert")
	public ResponseEntity<ApiResponse<TaxMasterResponse>> createTaxMaster(
			@Valid @RequestBody TaxMasterCreateRequest request, @RequestHeader("X-User-Id") UUID createdBy) {

		ApiResponse<TaxMasterResponse> response = taxMasterService.createTaxMaster(request, createdBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<TaxMasterResponse>> updateTaxMaster(@PathVariable UUID id,
			@Valid @RequestBody TaxMasterUpdateRequest request, @RequestHeader("X-User-Id") UUID updatedBy) {

		ApiResponse<TaxMasterResponse> response = taxMasterService.updateTaxMaster(id, request, updatedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<List<Object>>> deleteTaxMaster(@PathVariable UUID id,
			@RequestHeader("X-User-Id") UUID deletedBy) {

		ApiResponse<List<Object>> response = taxMasterService.deleteTaxMaster(id, deletedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<TaxMasterResponse>> getTaxMasterById(@PathVariable UUID id) {
		ApiResponse<TaxMasterResponse> response = taxMasterService.getTaxMasterById(id);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/code/{code}")
	public ResponseEntity<ApiResponse<TaxMasterResponse>> getTaxMasterByTaxCode(@PathVariable String code) {
		ApiResponse<TaxMasterResponse> response = taxMasterService.getTaxMasterByTaxCode(code);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/active")
	public ResponseEntity<ApiResponse<List<TaxMasterResponse>>> getAllActiveTaxMaster() {
		ApiResponse<List<TaxMasterResponse>> response = taxMasterService.getAllTaxMasterWithStatus(true);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/inactive")
	public ResponseEntity<ApiResponse<List<TaxMasterResponse>>> getAllInactiveTaxMaster() {
		ApiResponse<List<TaxMasterResponse>> response = taxMasterService.getAllTaxMasterWithStatus(false);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/")
	public ResponseEntity<ApiResponse<List<TaxMasterResponse>>> getAllTaxMasters() {
		ApiResponse<List<TaxMasterResponse>> response = taxMasterService.getAllTaxMasters();
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/search")
	public ResponseEntity<ApiResponse<PagedResponse<TaxMasterResponse>>> getTaxMasters(
			@RequestParam(value = "taxType", required = false) String taxType,
			@RequestParam(value = "active", required = false) Boolean active,
			@RequestParam(value = "page", defaultValue = "0") int page,
			@RequestParam(value = "size", defaultValue = "20") int size,
			@RequestParam(value = "sort", defaultValue = "taxName,asc") String sort) {

		ApiResponse<PagedResponse<TaxMasterResponse>> response = taxMasterService.getTaxMasters(taxType, active, page,
				size, sort);
		return ResponseEntity.ok(response);
	}

}
