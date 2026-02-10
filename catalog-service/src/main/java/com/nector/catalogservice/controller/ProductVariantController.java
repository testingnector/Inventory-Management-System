package com.nector.catalogservice.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nector.catalogservice.dto.request.internal.ProductVariantCreateRequest;
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

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> createVariant(
            @Valid @RequestBody ProductVariantCreateRequest request,
            @RequestHeader("X-USER-ID") UUID createdBy) {

        ApiResponse<ProductVariantResponse> response = variantService.createProductVariant(request, createdBy);
        return ResponseEntity.status(response.getHttpStatusCode()).body(response);
    }

//    @PutMapping("/update/{variantId}")
//    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariant(
//            @PathVariable UUID variantId,
//            @Valid @RequestBody ProductVariantUpdateRequest request,
//            @RequestHeader("X-USER-ID") UUID updatedBy) {
//
//        ApiResponse<ProductVariantResponse> response = variantService.updateProductVariant(variantId, request, updatedBy);
//        return ResponseEntity.status(response.getHttpStatusCode()).body(response);
//    }
//
//    @GetMapping("/product/{productId}/company/{companyId}")
//    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> getVariantsByProduct(
//            @PathVariable UUID productId,
//            @PathVariable UUID companyId,
//            @RequestParam(required = false) Boolean active) {
//
//        ApiResponse<List<ProductVariantResponse>> response = variantService.getVariantsByProduct(productId, companyId, active);
//        return ResponseEntity.status(response.getHttpStatusCode()).body(response);
//    }
//
//    @DeleteMapping("/delete/{variantId}")
//    public ResponseEntity<ApiResponse<Void>> deleteVariant(
//            @PathVariable UUID variantId,
//            @RequestHeader("X-USER-ID") UUID deletedBy) {
//
//        ApiResponse<Void> response = variantService.deleteProductVariant(variantId, deletedBy);
//        return ResponseEntity.status(response.getHttpStatusCode()).body(response);
//    }
}
