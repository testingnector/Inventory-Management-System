package com.nector.catalogservice.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nector.catalogservice.dto.request.internal.ProductImageCreateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.entity.ProductImage;
import com.nector.catalogservice.service.ProductImageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/product-images")
@RequiredArgsConstructor
public class ProductImageController {

	private final ProductImageService productImageService;

	@PostMapping
	public ResponseEntity<ApiResponse<ProductImage>> createProductImage(
			@Valid @RequestBody ProductImageCreateRequest request, @RequestHeader("X-USER-ID") UUID createdBy) {
		ApiResponse<ProductImage> response = productImageService.createProductImage(request, createdBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}
}
