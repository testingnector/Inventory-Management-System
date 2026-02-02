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

import com.nector.catalogservice.dto.request.internal.BulkCategoryStatusRequest;
import com.nector.catalogservice.dto.request.internal.CategoryCreateRequest;
import com.nector.catalogservice.dto.request.internal.CategoryUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CategoryResponse;
import com.nector.catalogservice.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	// Create
	@PostMapping("/insert")
	public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
			@Valid @RequestBody CategoryCreateRequest categoryCreateRequest, @RequestHeader("X-USER-ID") UUID createdBy,
			@RequestHeader("X-USER-ROLE") String role) {

		ApiResponse<CategoryResponse> response = categoryService.createCategory(categoryCreateRequest, createdBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(@PathVariable("id") UUID categoryId,
			@Valid @RequestBody CategoryUpdateRequest categoryUpdateRequest, @RequestHeader("X-USER-ID") UUID updatedBy,
			@RequestHeader("X-USER-ROLE") String role) {

		ApiResponse<CategoryResponse> response = categoryService.updateCategory(categoryId, categoryUpdateRequest,
				updatedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<List<Object>>> deleteCategory(@PathVariable("id") UUID categoryId,
			@RequestHeader("X-USER-ID") UUID deletedBy, @RequestHeader("X-USER-ROLE") String role) {

		ApiResponse<List<Object>> response = categoryService.deleteCategory(categoryId, deletedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryByCategoryId(@PathVariable("id") UUID categoryId) {

		ApiResponse<CategoryResponse> response = categoryService.getCategoryByCategoryId(categoryId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/code/{categoryCode}")
	public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryByCategoryCode(
			@PathVariable("categoryCode") String categoryCode) {

		ApiResponse<CategoryResponse> response = categoryService.getCategoryByCategoryCode(categoryCode);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/active")
	public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllActiveCategories() {

		ApiResponse<List<CategoryResponse>> response = categoryService.getAllActiveCategories();
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/inactive")
	public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllInactiveCategories() {

		ApiResponse<List<CategoryResponse>> response = categoryService.getAllInactiveCategories();
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/bulk-activate")
	public ResponseEntity<ApiResponse<List<CategoryResponse>>> bulkActivate(
			@Valid @RequestBody BulkCategoryStatusRequest request, @RequestHeader("X-USER-ID") UUID updatedBy) {

		ApiResponse<List<CategoryResponse>> response = categoryService.bulkUpdateActiveStatus(request, true,
				updatedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/bulk-deactivate")
	public ResponseEntity<ApiResponse<List<CategoryResponse>>> bulkDeactivate(
			@Valid @RequestBody BulkCategoryStatusRequest request, @RequestHeader("X-USER-ID") UUID updatedBy) {

		ApiResponse<List<CategoryResponse>> response = categoryService.bulkUpdateActiveStatus(request, false,
				updatedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@DeleteMapping("/bulk-delete")
	public ResponseEntity<ApiResponse<List<Object>>> bulkDelete(
			@Valid @RequestBody BulkCategoryStatusRequest request, @RequestHeader("X-USER-ID") UUID deletedBy) {

		ApiResponse<List<Object>> response = categoryService.bulkDeleteCategories(request, deletedBy);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

}