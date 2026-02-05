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

import com.nector.catalogservice.dto.request.internal.BulkProductStatusRequest;
import com.nector.catalogservice.dto.request.internal.ProductCreateRequest;
import com.nector.catalogservice.dto.request.internal.ProductUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CompanyProductsDetailsResponseDto1;
import com.nector.catalogservice.dto.response.internal.ProductAggregateResponse;
import com.nector.catalogservice.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@PostMapping("/insert")
	public ResponseEntity<ApiResponse<ProductAggregateResponse>> createProduct(
			@Valid @RequestBody ProductCreateRequest request, @RequestHeader("X-USER-ID") UUID createdBy,
			@RequestHeader("X-USER-ROLE") String role) {

		ApiResponse<ProductAggregateResponse> response = productService.createProduct(request, createdBy);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/{productId}")
	public ResponseEntity<ApiResponse<ProductAggregateResponse>> updateProduct(@PathVariable UUID productId,
			@Valid @RequestBody ProductUpdateRequest request, @RequestHeader("X-USER-ID") UUID updatedBy) {

		ApiResponse<ProductAggregateResponse> response = productService.updateProduct(productId, request, updatedBy);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@DeleteMapping("/{productId}")
	public ResponseEntity<ApiResponse<List<Object>>> deleteProduct(@PathVariable UUID productId,
			@RequestHeader("X-USER-ID") UUID deletedBy) {

		ApiResponse<List<Object>> response = productService.deleteProduct(productId, deletedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/{productId}")
	public ResponseEntity<ApiResponse<ProductAggregateResponse>> getProductByProductId(@PathVariable UUID productId) {

		ApiResponse<ProductAggregateResponse> response = productService.getProductByProductId(productId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/company/{companyId}/active")
	public ResponseEntity<ApiResponse<CompanyProductsDetailsResponseDto1>> getAllActiveProductByCompanyId(
			@PathVariable UUID companyId) {

		ApiResponse<CompanyProductsDetailsResponseDto1> response = productService
				.getAllActiveProductsByCompanyId(companyId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/company/{companyId}/inactive")
	public ResponseEntity<ApiResponse<CompanyProductsDetailsResponseDto1>> getAllInactiveProductByCompanyId(
			@PathVariable UUID companyId) {

		ApiResponse<CompanyProductsDetailsResponseDto1> response = productService
				.getAllInactiveProductsByCompanyId(companyId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/company/{companyId}/bulk-activate")
	public ResponseEntity<ApiResponse<CompanyProductsDetailsResponseDto1>> bulkActivateProductsByCompanyId(@PathVariable UUID companyId,
			@Valid @RequestBody BulkProductStatusRequest request, @RequestHeader("X-USER-ID") UUID updatedBy) {

		ApiResponse<CompanyProductsDetailsResponseDto1> response = productService.bulkUpdateProductStatusByCompany(companyId, request, true, updatedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}
	
	@GetMapping("/company/{companyId}/bulk-deactivate")
	public ResponseEntity<ApiResponse<CompanyProductsDetailsResponseDto1>> bulkDeactivateProductsByCompanyId(@PathVariable UUID companyId,
			@Valid @RequestBody BulkProductStatusRequest request, @RequestHeader("X-USER-ID") UUID updatedBy) {
		
		ApiResponse<CompanyProductsDetailsResponseDto1> response = productService.bulkUpdateProductStatusByCompany(companyId, request, false, updatedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}
	
	@GetMapping("/code/{productCode}")
	public ResponseEntity<ApiResponse<ProductAggregateResponse>> getProductByProductCode(@PathVariable String  productCode) {
		
		ApiResponse<ProductAggregateResponse> response = productService.getProductByProductCode(productCode);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}
	
	@DeleteMapping("/company/{companyId}/bulk-delete")
	public ResponseEntity<ApiResponse<List<Object>>> bulkDeletionOfProductsByCompanyId(@PathVariable UUID  companyId, @Valid @RequestBody BulkProductStatusRequest request, @RequestHeader("X-USER-ID") UUID deletedBy) {
		
		ApiResponse<List<Object>> response = productService.bulkDeletionOfProductsByCompanyId(companyId, request, deletedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}
	
	

}
