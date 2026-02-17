package com.nector.catalogservice.controller;

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

import com.nector.catalogservice.dto.request.internal.ProductTaxMappingCreateRequest;
import com.nector.catalogservice.dto.request.internal.ProductTaxMappingUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.ProductResponseWithCompanyImagesVariants;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponseWithCompanyImagesProduct;
import com.nector.catalogservice.service.ProductTaxMappingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/product-tax-mappings")
@RequiredArgsConstructor
public class ProductTaxMappingController {

	private final ProductTaxMappingService productTaxMappingService;

	@PostMapping
	public ResponseEntity<?> createProductTaxMapping(@Valid @RequestBody ProductTaxMappingCreateRequest request,
			@RequestHeader("X-USER-ID") UUID userId) {

		ApiResponse<?> response = productTaxMappingService.createProductTaxMapping(request, userId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateProductTaxMapping(@PathVariable UUID id, @RequestParam UUID companyId,
			@Valid @RequestBody ProductTaxMappingUpdateRequest request, @RequestHeader("X-USER-ID") UUID userId) {

		ApiResponse<?> response = productTaxMappingService.updateProductTaxMapping(companyId, id, request, userId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteProductTaxMapping(@PathVariable UUID id, @RequestParam UUID companyId,
			@RequestHeader("X-USER-ID") UUID userId) {

		ApiResponse<?> response = productTaxMappingService.deleteProductTaxMapping(id, companyId, userId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getByCompanyAndId(@PathVariable UUID id, @RequestParam UUID companyId) {

		ApiResponse<?> response = productTaxMappingService.getByCompanyAndId(id, companyId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/company/{companyId}/product/{productId}")
	public ResponseEntity<?> getByCompanyAndProduct(@PathVariable UUID companyId, @PathVariable UUID productId) {

		ApiResponse<?> response = productTaxMappingService.getByCompanyIdAndProductId(companyId, productId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/company/{companyId}/variant/{variantId}")
	public ResponseEntity<?> getByCompanyAndVariant(@PathVariable UUID companyId, @PathVariable UUID variantId) {

		ApiResponse<?> response = productTaxMappingService.getByCompanyIdAndVariantId(companyId, variantId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

}
