package com.nector.catalogservice.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nector.catalogservice.client.OrgServiceClient;
import com.nector.catalogservice.dto.request.internal.BulkCategoryStatusRequest;
import com.nector.catalogservice.dto.request.internal.CategoryCreateRequest;
import com.nector.catalogservice.dto.request.internal.CategoryUpdateRequest;
import com.nector.catalogservice.dto.response.external.CompanyResponseExternalDto;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CategoryResponse;
import com.nector.catalogservice.dto.response.internal.CompanyResponseInternalDto;
import com.nector.catalogservice.entity.Category;
import com.nector.catalogservice.exception.ActiveResourceException;
import com.nector.catalogservice.exception.DuplicateResourceException;
import com.nector.catalogservice.exception.InactiveResourceException;
import com.nector.catalogservice.exception.OrgServiceException;
import com.nector.catalogservice.exception.ResourceNotFoundException;
import com.nector.catalogservice.repository.CategoryRepository;
import com.nector.catalogservice.service.CategoryService;

import feign.FeignException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

	private final CategoryRepository categoryRepository;
	private final OrgServiceClient orgServiceClient;

	@Transactional
	@Override
	public ApiResponse<CategoryResponse> createCategory(CategoryCreateRequest request, UUID createdBy) {

		Optional<Category> existingCategoryOpt = categoryRepository
				.findByCategoryCode(request.getCategoryCode());

		if (existingCategoryOpt.isPresent()) {
			Category existing = existingCategoryOpt.get();
			if (!existing.getActive() && existing.getDeletedAt() == null) {
				throw new InactiveResourceException("Category already exists but is inactive");
			} 
			else {
				throw new DuplicateResourceException("Category code already exists!");
			}
		}

		Category category = new Category();
		category.setCategoryCode(request.getCategoryCode());
		category.setCategoryName(request.getCategoryName());
		category.setDescription(request.getDescription());
		category.setDisplayOrder(request.getDisplayOrder());
		category.setCreatedBy(createdBy);

		Category savedCategory = categoryRepository.save(category);

//	    BUID RESPONSE
		CategoryResponse cr = new CategoryResponse();
		cr.setCategoryId(savedCategory.getId());
		cr.setCategoryCode(savedCategory.getCategoryCode());
		cr.setCategoryName(savedCategory.getCategoryName());
		cr.setDescription(savedCategory.getDescription());
		cr.setDisplayOrder(savedCategory.getDisplayOrder());
		cr.setActive(savedCategory.getActive());
		cr.setCreatedAt(savedCategory.getCreatedAt());

		return new ApiResponse<>(true, "Category created successfully...",
				HttpStatus.OK.name(), HttpStatus.OK.value(), cr);

	}

	@Transactional
	@Override
	public ApiResponse<CategoryResponse> updateCategory(UUID categoryId,
			@Valid CategoryUpdateRequest request, UUID updatedBy) {

		Category category = categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found"));

		if (category.getActive() == false && request.getActive() == false) {
			throw new InactiveResourceException("Category is already inactive!");
		}
		if (category.getActive() == true && request.getActive() == true) {
			throw new ActiveResourceException("Category is already active!");
		}

		if (request.getCategoryName() != null)
			category.setCategoryName(request.getCategoryName());
		if (request.getDescription() != null)
			category.setDescription(request.getDescription());
		if (request.getDisplayOrder() != null)
			category.setDisplayOrder(request.getDisplayOrder());
		if (request.getActive() != null)
			category.setActive(request.getActive());

		category.setUpdatedBy(updatedBy);
		Category updatedCategory = categoryRepository.save(category);



//	    BUID RESPONSE
		CategoryResponse cr = new CategoryResponse();
		cr.setCategoryId(updatedCategory.getId());
		cr.setCategoryCode(updatedCategory.getCategoryCode());
		cr.setCategoryName(updatedCategory.getCategoryName());
		cr.setDescription(updatedCategory.getDescription());
		cr.setDisplayOrder(updatedCategory.getDisplayOrder());
		cr.setActive(updatedCategory.getActive());
		cr.setCreatedAt(updatedCategory.getCreatedAt());

		return new ApiResponse<>(true, "Category updated successfully...",
				HttpStatus.OK.name(), HttpStatus.OK.value(), cr);

	}

	@Transactional
	@Override
	public ApiResponse<List<Object>> deleteCategory(UUID categoryId, UUID deletedBy) {

		Category category = categoryRepository.findByIdAndDeletedAtIsNull(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found or already deleted!"));

		category.setDeletedAt(LocalDateTime.now());
		category.setDeletedBy(deletedBy);
		category.setActive(false);
		categoryRepository.save(category);

		return new ApiResponse<>(true, "Category deleted successfully...", HttpStatus.OK.name(), HttpStatus.OK.value(),
				Collections.emptyList());
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<CategoryResponse> getCategoryByCategoryId(UUID categoryId) {

		Category category = categoryRepository.findByIdAndDeletedAtIsNullAndActiveTrue(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found!"));


//	    BUID RESPONSE
		CategoryResponse cr = new CategoryResponse();
		cr.setCategoryId(category.getId());
		cr.setCategoryCode(category.getCategoryCode());
		cr.setCategoryName(category.getCategoryName());
		cr.setDescription(category.getDescription());
		cr.setDisplayOrder(category.getDisplayOrder());
		cr.setActive(category.getActive());
		cr.setCreatedAt(category.getCreatedAt());

		return new ApiResponse<>(true, "Category details fetch successfully...",
				HttpStatus.OK.name(), HttpStatus.OK.value(), cr);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<CategoryResponse> getCategoryByCategoryCode(String categoryCode) {

		Category category = categoryRepository.findByCategoryCodeAndDeletedAtIsNullAndActiveTrue(categoryCode)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found!"));

//	    BUID RESPONSE
		CategoryResponse cr = new CategoryResponse();
		cr.setCategoryId(category.getId());
		cr.setCategoryCode(category.getCategoryCode());
		cr.setCategoryName(category.getCategoryName());
		cr.setDescription(category.getDescription());
		cr.setDisplayOrder(category.getDisplayOrder());
		cr.setActive(category.getActive());
		cr.setCreatedAt(category.getCreatedAt());

		return new ApiResponse<>(true, "Category details fetch successfully...",
				HttpStatus.OK.name(), HttpStatus.OK.value(), cr);

	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<List<CategoryResponse>> getAllActiveCategories() {

		List<Category> categories = categoryRepository.findByDeletedAtIsNullAndActiveTrue();
		if (categories.isEmpty()) {
			throw new ResourceNotFoundException("No Active Categories found for this company");
		}

		List<CategoryResponse> categoriesResponseDto2s = new ArrayList<>();
		for (Category category : categories) {
			CategoryResponse cr = new CategoryResponse();
			cr.setCategoryId(category.getId());
			cr.setCategoryCode(category.getCategoryCode());
			cr.setCategoryName(category.getCategoryName());
			cr.setDescription(category.getDescription());
			cr.setDisplayOrder(category.getDisplayOrder());
			cr.setActive(category.getActive());
			cr.setCreatedAt(category.getCreatedAt());

			categoriesResponseDto2s.add(cr);
		}

		return new ApiResponse<>(true, "Active Categories details fetch successfully...", HttpStatus.OK.name(),
				HttpStatus.OK.value(), categoriesResponseDto2s);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<List<CategoryResponse>> getAllInactiveCategories() {

		List<Category> categories = categoryRepository.findByDeletedAtIsNullAndActiveFalse();
		if (categories.isEmpty()) {
			throw new ResourceNotFoundException("No Inactive Categories found for this company");
		}

		List<CategoryResponse> categoriesResponseDto2s = new ArrayList<>();
		for (Category category : categories) {
			CategoryResponse cr = new CategoryResponse();
			cr.setCategoryId(category.getId());
			cr.setCategoryCode(category.getCategoryCode());
			cr.setCategoryName(category.getCategoryName());
			cr.setDescription(category.getDescription());
			cr.setDisplayOrder(category.getDisplayOrder());
			cr.setActive(category.getActive());
			cr.setCreatedAt(category.getCreatedAt());

			categoriesResponseDto2s.add(cr);
		}

		return new ApiResponse<>(true, "Inactive Categories details fetch successfully...", HttpStatus.OK.name(),
				HttpStatus.OK.value(), categoriesResponseDto2s);
	}

	@Transactional
	@Override
	public ApiResponse<List<CategoryResponse>> bulkUpdateActiveStatus(@Valid BulkCategoryStatusRequest request,
			boolean activeStatus, UUID updatedBy) {

		List<Category> categories = categoryRepository.findByIdInAndDeletedAtIsNull(request.getCategoryIds());

		if (categories.isEmpty()) {
			throw new ResourceNotFoundException("No categories found");
		}

		if (categories.size() != request.getCategoryIds().size()) {
			throw new ResourceNotFoundException("Some categories not found or already deleted");
		}

		categories.forEach(category -> {
			if (category.getActive() == activeStatus) {
				return;
			}

			category.setActive(activeStatus);
			category.setUpdatedBy(updatedBy);
		});

		categoryRepository.saveAll(categories);

		String message = activeStatus ? "Categories activated successfully" : "Categories deactivated successfully";

		List<CategoryResponse> categoriesResponseDto2s = new ArrayList<>();
		for (Category category : categories) {
			CategoryResponse cr = new CategoryResponse();
			cr.setCategoryId(category.getId());
			cr.setCategoryCode(category.getCategoryCode());
			cr.setCategoryName(category.getCategoryName());
			cr.setDescription(category.getDescription());
			cr.setDisplayOrder(category.getDisplayOrder());
			cr.setActive(category.getActive());
			cr.setCreatedAt(category.getCreatedAt());

			categoriesResponseDto2s.add(cr);
		}
		
		return new ApiResponse<>(true, message, HttpStatus.OK.name(), HttpStatus.OK.value(), categoriesResponseDto2s);
	}

	@Transactional
	@Override
	public ApiResponse<List<Object>> bulkDeleteCategories(@Valid BulkCategoryStatusRequest request,
			UUID deletedBy) {

		List<Category> categories = categoryRepository.findByIdInAndDeletedAtIsNull(request.getCategoryIds());

		if (categories.isEmpty()) {
			throw new ResourceNotFoundException("No categories found");
		}

		if (categories.size() != request.getCategoryIds().size()) {
			throw new ResourceNotFoundException("Some categories not found or already deleted");
		}

		LocalDateTime now = LocalDateTime.now();

		categories.forEach(category -> {
			category.setDeletedAt(now);
			category.setDeletedBy(deletedBy);
			category.setActive(false);
		});

		categoryRepository.saveAll(categories);

		return new ApiResponse<>(true, "Categories deleted successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				Collections.emptyList());
	}

	

	
	
	
	
//	==============HELPER METHOD===============
	private CompanyResponseInternalDto fetchCompany(UUID companyId) {

		try {
			CompanyResponseExternalDto external = orgServiceClient.getCompanyBasic(companyId).getBody().getData();

			CompanyResponseInternalDto company = new CompanyResponseInternalDto();
			company.setCompanyId(external.getCompanyId());
			company.setCompanyCode(external.getCompanyCode());
			company.setCompanyName(external.getCompanyName());
			company.setActive(external.getActive());

			return company;

		} catch (FeignException e) {
			HttpStatus status = HttpStatus.resolve(e.status());
			String message = (status == HttpStatus.NOT_FOUND) ? "Company not found!"
					: "Error while communicating with Organization Service";

			throw new OrgServiceException(message, status, e);
		}
	}
}
