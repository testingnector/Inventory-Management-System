package com.nector.productcatalogservice.controller;

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

import com.nector.productcatalogservice.dto.request.internal.BulkCategoryStatusRequest;
import com.nector.productcatalogservice.dto.request.internal.CategoryCreateRequest;
import com.nector.productcatalogservice.dto.request.internal.CategoryUpdateRequest;
import com.nector.productcatalogservice.dto.response.internal.ApiResponse;
import com.nector.productcatalogservice.dto.response.internal.CategoryCompanyResponseDto1;
import com.nector.productcatalogservice.dto.response.internal.CompanyCategoriesResponseDto1;
import com.nector.productcatalogservice.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	// Create
	@PostMapping("/insert")
	public ResponseEntity<ApiResponse<CategoryCompanyResponseDto1>> createCategory(
			@Valid @RequestBody CategoryCreateRequest categoryCreateRequest, @RequestHeader("X-USER-ID") UUID createdBy,
			@RequestHeader("X-USER-ROLE") String role) {

		ApiResponse<CategoryCompanyResponseDto1> response = categoryService.createCategory(categoryCreateRequest,
				createdBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<CategoryCompanyResponseDto1>> updateCategory(@PathVariable("id") UUID categoryId,
			@Valid @RequestBody CategoryUpdateRequest categoryUpdateRequest, @RequestHeader("X-USER-ID") UUID updatedBy,
			@RequestHeader("X-USER-ROLE") String role) {

		ApiResponse<CategoryCompanyResponseDto1> response = categoryService.updateCategory(categoryId,
				categoryUpdateRequest, updatedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<List<Object>>> deleteCategory(@PathVariable("id") UUID categoryId,
			@RequestHeader("X-USER-ID") UUID deletedBy, @RequestHeader("X-USER-ROLE") String role) {

		ApiResponse<List<Object>> response = categoryService.deleteCategory(categoryId, deletedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<CategoryCompanyResponseDto1>> getCategoryByCategoryId(
			@PathVariable("id") UUID categoryId) {

		ApiResponse<CategoryCompanyResponseDto1> response = categoryService.getCategoryByCategoryId(categoryId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/code/{categoryCode}")
	public ResponseEntity<ApiResponse<CategoryCompanyResponseDto1>> getCategoryByCategoryCode(
			@PathVariable("categoryCode") String categoryCode) {

		ApiResponse<CategoryCompanyResponseDto1> response = categoryService.getCategoryByCategoryCode(categoryCode);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/company/{companyId}/active")
	public ResponseEntity<ApiResponse<CompanyCategoriesResponseDto1>> getAllActiveCategoriesByCompanyId(
			@PathVariable UUID companyId) {

		ApiResponse<CompanyCategoriesResponseDto1> response = categoryService.getActiveCategoriesByCompanyId(companyId);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/company/{companyId}/inactive")
	public ResponseEntity<ApiResponse<CompanyCategoriesResponseDto1>> getAllInactiveCategoriesByCompanyId(
			@PathVariable UUID companyId) {

		ApiResponse<CompanyCategoriesResponseDto1> response = categoryService
				.getInactiveCategoriesByCompanyId(companyId);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/bulk-activate")
	public ResponseEntity<ApiResponse<CompanyCategoriesResponseDto1>> bulkActivate(
			@Valid @RequestBody BulkCategoryStatusRequest request, @RequestHeader("X-USER-ID") UUID updatedBy) {

		ApiResponse<CompanyCategoriesResponseDto1> response = categoryService.bulkUpdateActiveStatus(request, true,
				updatedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@PutMapping("/bulk-deactivate")
	public ResponseEntity<ApiResponse<CompanyCategoriesResponseDto1>> bulkDeactivate(
			@Valid @RequestBody BulkCategoryStatusRequest request, @RequestHeader("X-USER-ID") UUID updatedBy) {

		ApiResponse<CompanyCategoriesResponseDto1> response = categoryService.bulkUpdateActiveStatus(request, false,
				updatedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@DeleteMapping("/bulk-delete")
	public ResponseEntity<ApiResponse<CompanyCategoriesResponseDto1>> bulkDelete(
			@Valid @RequestBody BulkCategoryStatusRequest request, @RequestHeader("X-USER-ID") UUID deletedBy) {

		ApiResponse<CompanyCategoriesResponseDto1> response = categoryService.bulkDeleteCategories(request, deletedBy);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

}