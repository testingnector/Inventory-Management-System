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

import com.nector.catalogservice.dto.request.internal.BulkDeleteVariantsRequest;
import com.nector.catalogservice.dto.request.internal.ProductVariantBulkUpdateRequest;
import com.nector.catalogservice.dto.request.internal.ProductVariantCreateRequest;
import com.nector.catalogservice.dto.request.internal.ProductVariantUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponse;
import com.nector.catalogservice.service.ProductVariantService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/product-variants")
@RequiredArgsConstructor
public class ProductVariantController {

	private final ProductVariantService variantService;

	@PostMapping
	public ResponseEntity<ApiResponse<ProductVariantResponse>> createVariant(
			@Valid @RequestBody ProductVariantCreateRequest request, @RequestHeader("X-USER-ID") UUID createdBy) {

		ApiResponse<ProductVariantResponse> response = variantService.createProductVariant(request, createdBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/{variantId}")
	public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariant(@PathVariable UUID variantId,
			@Valid @RequestBody ProductVariantUpdateRequest request, @RequestHeader("X-USER-ID") UUID updatedBy) {

		ApiResponse<ProductVariantResponse> response = variantService.updateProductVariant(variantId, request,
				updatedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@DeleteMapping("/{variantId}")
	public ResponseEntity<ApiResponse<Void>> deleteVariant(@PathVariable UUID variantId,
			@RequestHeader("X-USER-ID") UUID deletedBy) {

		ApiResponse<Void> response = variantService.deleteProductVariant(variantId, deletedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/{variantId}")
	public ResponseEntity<ApiResponse<ProductVariantResponse>> getVariantByVariantId(@PathVariable UUID variantId) {

		ApiResponse<ProductVariantResponse> response = variantService.getVariantByVariantId(variantId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/product/{productId}")
	public ResponseEntity<?> getVariantsByProductId(@PathVariable UUID productId) {

		ApiResponse<?> response = variantService.getVariantsByProductId(productId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/bulk")
	public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> bulkUpdateVariants(
			@Valid @RequestBody ProductVariantBulkUpdateRequest request, @RequestHeader("X-USER-ID") UUID updatedBy) {

		ApiResponse<List<ProductVariantResponse>> response = variantService.bulkUpdateProductVariants(request,
				updatedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/check-sku")
	public ResponseEntity<ApiResponse<Boolean>> checkSkuAvailability(@RequestParam String skuCode,
			@RequestParam UUID companyId) {

		ApiResponse<Boolean> response = variantService.isSkuAvailable(skuCode, companyId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@DeleteMapping("/bulk-delete")
	public ResponseEntity<ApiResponse<Void>> bulkDeleteVariants(@Valid @RequestBody BulkDeleteVariantsRequest request,
			@RequestHeader("X-USER-ID") UUID deletedBy) {

		ApiResponse<Void> response = variantService.bulkDeleteVariants(request.getVariantIds(), deletedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

}
