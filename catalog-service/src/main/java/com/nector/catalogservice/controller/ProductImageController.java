package com.nector.catalogservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nector.catalogservice.dto.request.internal.ProductImageCreateRequest;
import com.nector.catalogservice.dto.request.internal.ProductImageUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.ProductImageResponseWithCompanyProductProductVariant;
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
			@PathVariable UUID id, @RequestParam(required = false) MultipartFile file,
			@RequestParam(required = false) String imageType, @RequestParam(required = false) String altText,
			@RequestParam(required = false) Boolean primary, @RequestParam(required = false) Integer displayOrder,
			@RequestParam(required = false) Boolean active, @RequestHeader("X-USER-ID") UUID updatedBy) {

		ProductImageUpdateRequest request = new ProductImageUpdateRequest();
		request.setFile(file);
		request.setImageType(imageType);
		request.setAltText(altText);
		request.setPrimary(primary);
		request.setDisplayOrder(displayOrder);
		request.setActive(active);

		ApiResponse<ProductImageResponseWithCompanyProductProductVariant> response = productImageService
				.updateProductImage(id, request, updatedBy);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> deleteProductImage(@PathVariable UUID id,
			@RequestHeader("X-USER-ID") UUID deletedBy) {

		ApiResponse<Void> response = productImageService.deleteProductImage(id, deletedBy);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ProductImageResponseWithCompanyProductProductVariant>> getByProductImageId(
			@PathVariable UUID id) {

		ApiResponse<ProductImageResponseWithCompanyProductProductVariant> response = productImageService
				.getByProductImageId(id);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/by-product-or-variant")
	public ResponseEntity<ApiResponse<List<ProductImageResponseWithCompanyProductProductVariant>>> getByProductOrVariant(
			@RequestParam(value = "productId", required = false) UUID productId,
			@RequestParam(value = "productVariantId", required = false) UUID productVariantId) {

		ApiResponse<List<ProductImageResponseWithCompanyProductProductVariant>> response = productImageService
				.getByProductOrVariantId(productId, productVariantId);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

}
