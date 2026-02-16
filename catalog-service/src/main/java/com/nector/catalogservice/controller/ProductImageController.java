package com.nector.catalogservice.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
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
import org.springframework.web.multipart.MultipartFile;

import com.nector.catalogservice.dto.request.internal.BulkProductImageUpdateRequest;
import com.nector.catalogservice.dto.request.internal.ProductImageCreateRequest;
import com.nector.catalogservice.dto.request.internal.ProductImageUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.ProductImageResponseWithCompanyProductProductVariant;
import com.nector.catalogservice.dto.response.internal.ProductResponseWithCompanyImagesVariants;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponseWithCompanyImagesProduct;
import com.nector.catalogservice.service.ProductImageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/product-images")
@RequiredArgsConstructor
public class ProductImageController {

	private final ProductImageService productImageService;

	@PostMapping("/insert")
	public ResponseEntity<ApiResponse<ProductImageResponseWithCompanyProductProductVariant>> uploadProductImage(
			@RequestParam(value = "productId", required = false) UUID productId,
			@RequestParam(value = "productVariantId", required = false) UUID productVariantId,
			@RequestParam("companyId") UUID companyId, @RequestParam("file") MultipartFile file,
			@RequestParam(value = "imageType", required = false) String imageType,
			@RequestParam(value = "altText", required = false) String altText,
			@RequestParam(value = "primary", required = false, defaultValue = "false") Boolean primary,
			@RequestParam(value = "displayOrder", required = false) Integer displayOrder,
			@RequestHeader("X-USER-ID") UUID createdBy) {

		ProductImageCreateRequest request = new ProductImageCreateRequest();
		request.setProductId(productId);
		request.setProductVariantId(productVariantId);
		request.setCompanyId(companyId);
		request.setFile(file);
		request.setImageType(imageType);
		request.setAltText(altText);
		request.setPrimary(primary);
		request.setDisplayOrder(displayOrder);

		ApiResponse<ProductImageResponseWithCompanyProductProductVariant> response = productImageService
				.saveAndUploadProductImage(request, createdBy);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<ProductImageResponseWithCompanyProductProductVariant>> updateProductImage(
			@PathVariable UUID id, 
			@RequestParam("companyId") UUID companyId,
			@RequestParam(required = false) MultipartFile file, 
			@RequestParam(required = false) String imageType,
			@RequestParam(required = false) String altText, 
			@RequestParam(required = false) Boolean primary,
			@RequestParam(required = false) Integer displayOrder, 
			@RequestParam(required = false) Boolean active,
			@RequestHeader("X-USER-ID") UUID updatedBy) {

		ProductImageUpdateRequest request = new ProductImageUpdateRequest();
		request.setFile(file);
		request.setImageType(imageType);
		request.setAltText(altText);
		request.setPrimary(primary);
		request.setDisplayOrder(displayOrder);
		request.setActive(active);

		ApiResponse<ProductImageResponseWithCompanyProductProductVariant> response = productImageService
				.updateProductImage(id, companyId, request, updatedBy);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> deleteProductImage(@PathVariable UUID id,
			@RequestParam("companyId") UUID companyId, @RequestHeader("X-USER-ID") UUID deletedBy) {

		ApiResponse<Void> response = productImageService.deleteProductImage(id, companyId, deletedBy);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ProductImageResponseWithCompanyProductProductVariant>> getByProductImageId(
			@PathVariable UUID id, @RequestParam("companyId") UUID companyId) {

		ApiResponse<ProductImageResponseWithCompanyProductProductVariant> response = productImageService
				.getByProductImageId(id, companyId);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/by-product-or-variant")
	public ResponseEntity<?> getByProductOrVariant(@RequestParam(value = "productId", required = false) UUID productId,
			@RequestParam(value = "productVariantId", required = false) UUID productVariantId) {

		ApiResponse<?> response = productImageService.getByProductOrVariantId(productId, productVariantId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/company/{companyId}/product/{productId}")
	public ResponseEntity<ApiResponse<ProductResponseWithCompanyImagesVariants>> getByCompanyAndProduct(
			@PathVariable UUID companyId, @PathVariable UUID productId) {

		ApiResponse<ProductResponseWithCompanyImagesVariants> response = productImageService
				.getByCompanyIdAndProductId(companyId, productId);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/company/{companyId}/variant/{variantId}")
	public ResponseEntity<ApiResponse<ProductVariantResponseWithCompanyImagesProduct>> getByCompanyAndVariant(
			@PathVariable UUID companyId, @PathVariable UUID variantId) {

		ApiResponse<ProductVariantResponseWithCompanyImagesProduct> response = productImageService
				.getByCompanyIdAndVariantId(companyId, variantId);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/company/{companyId}/primary")
	public ResponseEntity<ApiResponse<?>> getPrimaryProductOrVariantImage(@PathVariable UUID companyId,
			@RequestParam(value = "productId", required = false) UUID productId,
			@RequestParam(value = "variantId", required = false) UUID variantId) {

		ApiResponse<?> response = productImageService.getPrimaryImageByCompanyAndProductOrVariant(companyId, productId,
				variantId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/search")
	public ResponseEntity<ApiResponse<Page<ProductImageResponseWithCompanyProductProductVariant>>> searchProductImages(
	        @RequestParam(required = false) UUID companyId,
	        @RequestParam(required = false) UUID productId,
	        @RequestParam(required = false) UUID variantId,
	        @RequestParam(required = false) Boolean active,
	        @RequestParam(required = false) Boolean primary,
	        @RequestParam(defaultValue = "false") boolean includeInactiveCompanies,
	        @RequestParam(defaultValue = "false") boolean includeInactiveProducts,
	        @RequestParam(defaultValue = "false") boolean includeInactiveVariants,  // << this one
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size,
	        @RequestParam(defaultValue = "createdAt") String sortBy,
	        @RequestParam(defaultValue = "desc") String sortDir) {

	    ApiResponse<Page<ProductImageResponseWithCompanyProductProductVariant>> response =
	        productImageService.searchProductImages(
	            companyId, productId, variantId, active, primary,
	            includeInactiveCompanies, includeInactiveProducts, includeInactiveVariants,
	            page, size, sortBy, sortDir
	        );

	    return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PostMapping("/bulk-upload")
	public ResponseEntity<ApiResponse<List<ProductImageResponseWithCompanyProductProductVariant>>> bulkUpload(
			@RequestParam("companyId") UUID companyId,
			@RequestParam(value = "productId", required = false) UUID productId,
			@RequestParam(value = "variantId", required = false) UUID variantId,
			@RequestParam("files") List<MultipartFile> files,
			@RequestParam(value = "imageType", required = false) String imageType,
			@RequestParam(value = "altText", required = false) String altText,
			@RequestParam(value = "primary", required = false) Boolean primary,
			@RequestParam(value = "displayOrder", required = false) Integer displayOrder,
			@RequestHeader("X-USER-ID") UUID createdBy) {

		List<ProductImageCreateRequest> requests = files.stream().map(file -> {
			ProductImageCreateRequest req = new ProductImageCreateRequest();
			req.setCompanyId(companyId);
			req.setProductId(productId);
			req.setProductVariantId(variantId);
			req.setFile(file);
			req.setImageType(imageType);
			req.setAltText(altText);
			req.setPrimary(primary);
			req.setDisplayOrder(displayOrder);
			return req;
		}).toList();

		ApiResponse<List<ProductImageResponseWithCompanyProductProductVariant>> response = productImageService
				.bulkUploadProductImages(requests, createdBy);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}
	
	@PutMapping("/bulk-update")
	public ResponseEntity<ApiResponse<List<ProductImageResponseWithCompanyProductProductVariant>>> bulkUpdateProductImages(
	        @RequestParam("companyId") UUID companyId,  
	        @RequestBody List<BulkProductImageUpdateRequest> requests,
	        @RequestHeader("X-USER-ID") UUID updatedBy) {

	    ApiResponse<List<ProductImageResponseWithCompanyProductProductVariant>> response =
	            productImageService.bulkUpdateProductImages(companyId, requests, updatedBy);

	    return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}


}
