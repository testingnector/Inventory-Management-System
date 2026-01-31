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

import com.nector.catalogservice.dto.request.internal.SubCategoryCreateRequest;
import com.nector.catalogservice.dto.request.internal.SubCategoryUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CategorySubCategoriesResponseDto1;
import com.nector.catalogservice.dto.response.internal.SubCategoryCategoryResponseDto1;
import com.nector.catalogservice.service.SubCategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("sub-categories")
@RequiredArgsConstructor
public class SubCategoryController {

	private final SubCategoryService subCategoryService;

//	ADMIN / SUPER_ADMIN
	@PostMapping("/insert")
	public ResponseEntity<ApiResponse<SubCategoryCategoryResponseDto1>> createSubCategory(
			@Valid @RequestBody SubCategoryCreateRequest subCategoryCreateRequest,
			@RequestHeader("X-USER-ID") UUID createdBy, @RequestHeader("X-USER-ROLE") String role) {

		ApiResponse<SubCategoryCategoryResponseDto1> response = subCategoryService
				.createSubCategory(subCategoryCreateRequest, createdBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

//	ADMIN / SUPER_ADMIN
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<SubCategoryCategoryResponseDto1>> updateSubCategory(
			@PathVariable("id") UUID subCategoryId,
			@Valid @RequestBody SubCategoryUpdateRequest subCategoryUpdateRequest,
			@RequestHeader("X-USER-ID") UUID updatedBy, @RequestHeader("X-USER-ROLE") String role) {

		ApiResponse<SubCategoryCategoryResponseDto1> response = subCategoryService.updateSubCategory(subCategoryId,
				subCategoryUpdateRequest, updatedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

//	ADMIN / SUPER_ADMIN
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<List<Object>>> deleteSubCategory(@PathVariable("id") UUID subCategoryId,
			@RequestHeader("X-USER-ID") UUID deletedBy, @RequestHeader("X-USER-ROLE") String role) {

		ApiResponse<List<Object>> response = subCategoryService.deleteSubCategory(subCategoryId, deletedBy);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

//	ADMIN / SUPER_ADMIN
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<SubCategoryCategoryResponseDto1>> getSubCategoryBySubCategoryId(
			@PathVariable("id") UUID subCategoryId) {

		ApiResponse<SubCategoryCategoryResponseDto1> response = subCategoryService
				.getSubCategoryBySubCategoryId(subCategoryId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/category/{categoryId}/active")
	public ResponseEntity<ApiResponse<CategorySubCategoriesResponseDto1>> getAllActiveSubCategoriesByCategoryId(
			@PathVariable("categoryId") UUID categoryId) {

		ApiResponse<CategorySubCategoriesResponseDto1> response = subCategoryService
				.getAllActiveSubCategoriesByCategoryId(categoryId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@GetMapping("/category/{categoryId}/inactive")
	public ResponseEntity<ApiResponse<CategorySubCategoriesResponseDto1>> getAllInactiveSubCategoriesByCategoryId(
			@PathVariable("categoryId") UUID categoryId) {

		ApiResponse<CategorySubCategoriesResponseDto1> response = subCategoryService
				.getAllInactiveSubCategoriesByCategoryId(categoryId);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

}
