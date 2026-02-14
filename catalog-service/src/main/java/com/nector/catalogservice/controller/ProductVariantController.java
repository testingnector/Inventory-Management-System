package com.nector.catalogservice.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nector.catalogservice.dto.request.internal.BulkDeleteVariantsRequest;
import com.nector.catalogservice.dto.request.internal.BulkVariantsStatusRequest;
import com.nector.catalogservice.dto.request.internal.ProductVariantBulkUpdateRequest;
import com.nector.catalogservice.dto.request.internal.ProductVariantCreateRequest;
import com.nector.catalogservice.dto.request.internal.ProductVariantPriceUpdateRequest;
import com.nector.catalogservice.dto.request.internal.ProductVariantUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CompanyProductVariantsResponse;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponseWithProductCompanyUom;
import com.nector.catalogservice.service.ProductVariantService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/product-variants")
@RequiredArgsConstructor
public class ProductVariantController {

	private final ProductVariantService variantService;

	@PostMapping
	public ResponseEntity<ApiResponse<ProductVariantResponseWithProductCompanyUom>> createVariant(
			@Valid @RequestBody ProductVariantCreateRequest request, @RequestHeader("X-USER-ID") UUID createdBy) {

		ApiResponse<ProductVariantResponseWithProductCompanyUom> response = variantService.createProductVariant(request, createdBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/{variantId}")
	public ResponseEntity<ApiResponse<ProductVariantResponseWithProductCompanyUom>> updateVariant(@PathVariable UUID variantId,
			@Valid @RequestBody ProductVariantUpdateRequest request, @RequestHeader("X-USER-ID") UUID updatedBy) {

		ApiResponse<ProductVariantResponseWithProductCompanyUom> response = variantService.updateProductVariant(variantId, request,
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
	public ResponseEntity<ApiResponse<ProductVariantResponseWithProductCompanyUom>> getVariantByVariantId(@PathVariable UUID variantId) {

		ApiResponse<ProductVariantResponseWithProductCompanyUom> response = variantService.getVariantByVariantId(variantId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/product/{productId}")
	public ResponseEntity<?> getVariantsByProductId(@PathVariable UUID productId) {

		ApiResponse<?> response = variantService.getVariantsByProductId(productId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/check-sku")
	public ResponseEntity<ApiResponse<Boolean>> checkSkuAvailability(@RequestParam String skuCode,
			@RequestParam UUID companyId) {

		ApiResponse<Boolean> response = variantService.isSkuAvailable(skuCode, companyId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping
	public ResponseEntity<ApiResponse<List<ProductVariantResponseWithProductCompanyUom>>> bulkUpdateVariants(
			@Valid @RequestBody ProductVariantBulkUpdateRequest request, @RequestHeader("X-USER-ID") UUID updatedBy) {

		ApiResponse<List<ProductVariantResponseWithProductCompanyUom>> response = variantService.bulkUpdateProductVariants(request,
				updatedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@DeleteMapping
	public ResponseEntity<ApiResponse<Void>> bulkDeleteVariants(@Valid @RequestBody BulkDeleteVariantsRequest request,
			@RequestHeader("X-USER-ID") UUID deletedBy) {

		ApiResponse<Void> response = variantService.bulkDeleteVariants(request.getVariantIds(), deletedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/sku/{skuCode}")
	public ResponseEntity<ApiResponse<ProductVariantResponseWithProductCompanyUom>> getVariantBySku(@PathVariable String skuCode,
			@RequestParam UUID companyId) {

		ApiResponse<ProductVariantResponseWithProductCompanyUom> response = variantService.getVariantBySkuAndCompanyId(skuCode, companyId);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/company/{companyId}")
	public ResponseEntity<?> getVariantsByCompany(@PathVariable UUID companyId) {
		ApiResponse<?> response = variantService.getVariantsByCompanyId(companyId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PatchMapping("/{variantId}/price")
	public ResponseEntity<ApiResponse<ProductVariantResponseWithProductCompanyUom>> updateVariantPrice(@PathVariable UUID variantId,
			@Valid @RequestBody ProductVariantPriceUpdateRequest request, @RequestHeader("X-USER-ID") UUID updatedBy) {

		ApiResponse<ProductVariantResponseWithProductCompanyUom> response = variantService.updateVariantPrice(variantId, request, updatedBy);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/count")
	public ResponseEntity<ApiResponse<Long>> countVariants(@RequestParam(required = false) UUID companyId,
			@RequestParam(required = false) UUID productId, @RequestParam(required = false) Boolean active) {

		ApiResponse<Long> response = variantService.countVariants(companyId, productId, active);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping
	public ResponseEntity<ApiResponse<Page<ProductVariantResponseWithProductCompanyUom>>> getAllVariants(
			@RequestParam(required = false) UUID companyId, @RequestParam(required = false) UUID productId,
			@RequestParam(required = false) Boolean active, @RequestParam(required = false) Boolean serialized,
			@RequestParam(required = false) Boolean batchTracked, @RequestParam(required = false) String search,
			@RequestParam(required = false) BigDecimal minPrice, @RequestParam(required = false) BigDecimal maxPrice,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String sortDir,
			@RequestParam(defaultValue = "false") boolean includeInactiveCompanies,
			@RequestParam(defaultValue = "false") boolean includeInactiveProducts,
			@RequestParam(defaultValue = "false") boolean includeInactiveUoms) {

		ApiResponse<Page<ProductVariantResponseWithProductCompanyUom>> response = variantService.getAllVariants(companyId, productId, active,
				serialized, batchTracked, search, minPrice, maxPrice, page, size, sortBy, sortDir,
				includeInactiveCompanies, includeInactiveProducts, includeInactiveUoms);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PatchMapping("/company/{companyId}/bulk-activate")
	public ResponseEntity<ApiResponse<?>> bulkActivateVariantsByCompanyId(@PathVariable UUID companyId,
			@Valid @RequestBody BulkVariantsStatusRequest request, @RequestHeader("X-USER-ID") UUID updatedBy) {

		ApiResponse<?> response = variantService.bulkUpdateProductStatusByCompany(companyId, request, true, updatedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PatchMapping("/company/{companyId}/bulk-deactivate")
	public ResponseEntity<ApiResponse<?>> bulkDeactivateVariantsByCompanyId(@PathVariable UUID companyId,
			@Valid @RequestBody BulkVariantsStatusRequest request, @RequestHeader("X-USER-ID") UUID updatedBy) {

		ApiResponse<?> response = variantService.bulkUpdateProductStatusByCompany(companyId, request, false, updatedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/company/{companyId}/active")
	public ResponseEntity<ApiResponse<CompanyProductVariantsResponse>> getAllActiveVariantsByCompanyId(
			@PathVariable UUID companyId) {

		ApiResponse<CompanyProductVariantsResponse> response = variantService.getVariantsByCompanyAndStatus(companyId,
				true);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/company/{companyId}/inactive")
	public ResponseEntity<ApiResponse<CompanyProductVariantsResponse>> getAllInactiveVariantsByCompanyId(
			@PathVariable UUID companyId) {

		ApiResponse<CompanyProductVariantsResponse> response = variantService.getVariantsByCompanyAndStatus(companyId,
				false);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

}
