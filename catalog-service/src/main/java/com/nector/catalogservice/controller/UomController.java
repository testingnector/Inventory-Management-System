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

import com.nector.catalogservice.dto.request.internal.UomCreateRequest;
import com.nector.catalogservice.dto.request.internal.UomUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.UomResponse;
import com.nector.catalogservice.service.UomService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/uoms")
@RequiredArgsConstructor
public class UomController {

	private final UomService uomService;

	@PostMapping("/insert")
	public ResponseEntity<ApiResponse<UomResponse>> createUom(@Valid @RequestBody UomCreateRequest request,
			@RequestHeader("X-USER-ID") UUID createdBy, @RequestHeader("X-USER-ROLE") String role) {

		ApiResponse<UomResponse> response = uomService.createUom(request, createdBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/{uomId}")
	public ResponseEntity<ApiResponse<UomResponse>> updateUom(@PathVariable UUID uomId,
			@Valid @RequestBody UomUpdateRequest request, @RequestHeader("X-USER-ID") UUID updatedBy) {

		ApiResponse<UomResponse> response = uomService.updateUom(uomId, request, updatedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@DeleteMapping("/{uomId}")
	public ResponseEntity<ApiResponse<List<Object>>> deleteUom(@PathVariable UUID uomId,
			@RequestHeader("X-USER-ID") UUID deletedBy) {

		ApiResponse<List<Object>> response = uomService.deleteUom(uomId, deletedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/{uomId}")
	public ResponseEntity<ApiResponse<UomResponse>> getUomById(@PathVariable UUID uomId) {

		ApiResponse<UomResponse> response = uomService.getUomById(uomId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/code/{uomCode}")
	public ResponseEntity<ApiResponse<UomResponse>> getUomByCode(@PathVariable String uomCode) {

		ApiResponse<UomResponse> response = uomService.getUomByCode(uomCode);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/active")
	public ResponseEntity<ApiResponse<List<UomResponse>>> getAllActiveUoms() {

		ApiResponse<List<UomResponse>> response = uomService.getAllUomWithStatus(true);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/inactive")
	public ResponseEntity<ApiResponse<List<UomResponse>>> getAllInactiveUoms() {

		ApiResponse<List<UomResponse>> response = uomService.getAllUomWithStatus(false);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<UomResponse>>> getAllUoms() {

		ApiResponse<List<UomResponse>> response = uomService.getAllUoms();
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/base")
	public ResponseEntity<ApiResponse<List<UomResponse>>> getAllBaseUoms() {

		ApiResponse<List<UomResponse>> response = uomService.getBaseUoms();
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/derived/{baseUomId}")
	public ResponseEntity<ApiResponse<List<UomResponse>>> getAllDerivedUomsByBaseUomId(@PathVariable UUID baseUomId) {

		ApiResponse<List<UomResponse>> response = uomService.getDerivedUomsByBaseUomId(baseUomId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

}
