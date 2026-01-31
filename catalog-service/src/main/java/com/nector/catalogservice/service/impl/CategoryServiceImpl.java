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
import com.nector.catalogservice.dto.response.internal.CategoryCompanyResponseDto1;
import com.nector.catalogservice.dto.response.internal.CompanyCategoriesResponseDto1;
import com.nector.catalogservice.dto.response.internal.CompanyCategoriesResponseDto2;
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
	public ApiResponse<CategoryCompanyResponseDto1> createCategory(CategoryCreateRequest request, UUID createdBy) {

		Optional<Category> existingCategoryOpt = categoryRepository
				.findByCategoryCodeAndDeletedAtIsNull(request.getCategoryCode());

		if (existingCategoryOpt.isPresent()) {
			Category existing = existingCategoryOpt.get();
			if (!existing.getActive()) {
				throw new InactiveResourceException("Category already exists but is inactive");
			} else {
				throw new DuplicateResourceException("Category code already exists!");
			}
		}

		CompanyResponseInternalDto crid = fetchCompany(request.getCompanyId());

		Category category = new Category();
		category.setCategoryCode(request.getCategoryCode());
		category.setCategoryName(request.getCategoryName());
		category.setCompanyId(request.getCompanyId());
		category.setDescription(request.getDescription());
		category.setDisplayOrder(request.getDisplayOrder());
		category.setCreatedBy(createdBy);

		Category savedCategory = categoryRepository.save(category);

//	    BUID RESPONSE
		CategoryCompanyResponseDto1 ccrd = new CategoryCompanyResponseDto1();
		ccrd.setCategoryId(savedCategory.getId());
		ccrd.setCategoryCode(savedCategory.getCategoryCode());
		ccrd.setCategoryName(savedCategory.getCategoryName());
		ccrd.setDescription(savedCategory.getDescription());
		ccrd.setDisplayOrder(savedCategory.getDisplayOrder());
		ccrd.setActive(savedCategory.getActive());
		ccrd.setCreatedAt(savedCategory.getCreatedAt());
		ccrd.setCompany(crid);

		return new ApiResponse<CategoryCompanyResponseDto1>(true, "Category created successfully...",
				HttpStatus.OK.name(), HttpStatus.OK.value(), ccrd);

	}

	@Transactional
	@Override
	public ApiResponse<CategoryCompanyResponseDto1> updateCategory(UUID categoryId,
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

		CompanyResponseInternalDto crid = fetchCompany(category.getCompanyId());

		CategoryCompanyResponseDto1 ccrd = new CategoryCompanyResponseDto1();
		ccrd.setCategoryId(updatedCategory.getId());
		ccrd.setCategoryCode(updatedCategory.getCategoryCode());
		ccrd.setCategoryName(updatedCategory.getCategoryName());
		ccrd.setDescription(updatedCategory.getDescription());
		ccrd.setDisplayOrder(updatedCategory.getDisplayOrder());
		ccrd.setActive(updatedCategory.getActive());
		ccrd.setCreatedAt(updatedCategory.getCreatedAt());
		ccrd.setCompany(crid);

		return new ApiResponse<CategoryCompanyResponseDto1>(true, "Category updated successfully...",
				HttpStatus.OK.name(), HttpStatus.OK.value(), ccrd);

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
	public ApiResponse<CategoryCompanyResponseDto1> getCategoryByCategoryId(UUID categoryId) {

		Category category = categoryRepository.findByIdAndDeletedAtIsNullAndActiveTrue(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found!"));

		CompanyResponseInternalDto crid = fetchCompany(category.getCompanyId());

		CategoryCompanyResponseDto1 ccrd = new CategoryCompanyResponseDto1();
		ccrd.setCategoryId(category.getId());
		ccrd.setCategoryCode(category.getCategoryCode());
		ccrd.setCategoryName(category.getCategoryName());
		ccrd.setDescription(category.getDescription());
		ccrd.setDisplayOrder(category.getDisplayOrder());
		ccrd.setActive(category.getActive());
		ccrd.setCreatedAt(category.getCreatedAt());
		ccrd.setCompany(crid);

		return new ApiResponse<CategoryCompanyResponseDto1>(true, "Category details fetch successfully...",
				HttpStatus.OK.name(), HttpStatus.OK.value(), ccrd);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<CategoryCompanyResponseDto1> getCategoryByCategoryCode(String categoryCode) {

		Category category = categoryRepository.findByCategoryCodeAndDeletedAtIsNullAndActiveTrue(categoryCode)
				.orElseThrow(() -> new ResourceNotFoundException("Category not found!"));

		CompanyResponseInternalDto crid = fetchCompany(category.getCompanyId());

		CategoryCompanyResponseDto1 ccrd = new CategoryCompanyResponseDto1();
		ccrd.setCategoryId(category.getId());
		ccrd.setCategoryCode(category.getCategoryCode());
		ccrd.setCategoryName(category.getCategoryName());
		ccrd.setDescription(category.getDescription());
		ccrd.setDisplayOrder(category.getDisplayOrder());
		ccrd.setActive(category.getActive());
		ccrd.setCreatedAt(category.getCreatedAt());
		ccrd.setCompany(crid);

		return new ApiResponse<CategoryCompanyResponseDto1>(true, "Category details fetch successfully...",
				HttpStatus.OK.name(), HttpStatus.OK.value(), ccrd);

	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<CompanyCategoriesResponseDto1> getActiveCategoriesByCompanyId(UUID companyId) {

		CompanyResponseInternalDto crid = fetchCompany(companyId);

		List<Category> categories = categoryRepository.findByCompanyIdAndDeletedAtIsNullAndActiveTrue(companyId);
		if (categories.isEmpty()) {
			throw new ResourceNotFoundException("No Active Categories found for this company");
		}

		CompanyCategoriesResponseDto1 ccrd = new CompanyCategoriesResponseDto1();
		ccrd.setCompanyId(crid.getCompanyId());
		ccrd.setCompanyName(crid.getCompanyName());
		ccrd.setCompanyCode(crid.getCompanyCode());
		ccrd.setActive(crid.getActive());

		List<CompanyCategoriesResponseDto2> categoriesResponseDto2s = new ArrayList<>();
		for (Category category : categories) {
			CompanyCategoriesResponseDto2 ccrdt = new CompanyCategoriesResponseDto2();
			ccrdt.setCategoryId(category.getId());
			ccrdt.setCategoryCode(category.getCategoryCode());
			ccrdt.setCategoryName(category.getCategoryName());
			ccrdt.setDescription(category.getDescription());
			ccrdt.setDisplayOrder(category.getDisplayOrder());
			ccrdt.setActive(category.getActive());
			ccrdt.setCreatedAt(category.getCreatedAt());

			categoriesResponseDto2s.add(ccrdt);
		}

		ccrd.setCategories(categoriesResponseDto2s);

		return new ApiResponse<>(true, "Active Categories details fetch successfully...", HttpStatus.OK.name(),
				HttpStatus.OK.value(), ccrd);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<CompanyCategoriesResponseDto1> getInactiveCategoriesByCompanyId(UUID companyId) {

		CompanyResponseInternalDto crid = fetchCompany(companyId);

		List<Category> categories = categoryRepository.findByCompanyIdAndDeletedAtIsNullAndActiveFalse(companyId);
		if (categories.isEmpty()) {
			throw new ResourceNotFoundException("No Inactive Categories found for this company");
		}

		CompanyCategoriesResponseDto1 ccrd = new CompanyCategoriesResponseDto1();
		ccrd.setCompanyId(crid.getCompanyId());
		ccrd.setCompanyName(crid.getCompanyName());
		ccrd.setCompanyCode(crid.getCompanyCode());
		ccrd.setActive(crid.getActive());

		List<CompanyCategoriesResponseDto2> categoriesResponseDto2s = new ArrayList<>();
		for (Category category : categories) {
			CompanyCategoriesResponseDto2 ccrdt = new CompanyCategoriesResponseDto2();
			ccrdt.setCategoryId(category.getId());
			ccrdt.setCategoryCode(category.getCategoryCode());
			ccrdt.setCategoryName(category.getCategoryName());
			ccrdt.setDescription(category.getDescription());
			ccrdt.setDisplayOrder(category.getDisplayOrder());
			ccrdt.setActive(category.getActive());
			ccrdt.setCreatedAt(category.getCreatedAt());

			categoriesResponseDto2s.add(ccrdt);
		}

		ccrd.setCategories(categoriesResponseDto2s);

		return new ApiResponse<>(true, "Inactive Categories details fetch successfully...", HttpStatus.OK.name(),
				HttpStatus.OK.value(), ccrd);
	}

	@Transactional
	@Override
	public ApiResponse<CompanyCategoriesResponseDto1> bulkUpdateActiveStatus(@Valid BulkCategoryStatusRequest request,
			boolean activeStatus, UUID updatedBy) {

		List<Category> categories = categoryRepository.findByIdInAndDeletedAtIsNull(request.getCategoryIds());

		if (categories.isEmpty()) {
			throw new ResourceNotFoundException("No categories found");
		}

		if (categories.size() != request.getCategoryIds().size()) {
			throw new ResourceNotFoundException("Some categories not found or already deleted");
		}

		UUID companyId = categories.get(0).getCompanyId();

		boolean multipleCompanies = categories.stream().anyMatch(c -> !c.getCompanyId().equals(companyId));
		if (multipleCompanies) {
			throw new IllegalArgumentException("Bulk operation allowed for single company only");
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

		CompanyResponseInternalDto crid = fetchCompany(companyId);

		CompanyCategoriesResponseDto1 ccrd = new CompanyCategoriesResponseDto1();
		ccrd.setCompanyId(crid.getCompanyId());
		ccrd.setCompanyName(crid.getCompanyName());
		ccrd.setCompanyCode(crid.getCompanyCode());
		ccrd.setActive(crid.getActive());

		List<CompanyCategoriesResponseDto2> categoriesResponseDto2s = new ArrayList<>();
		for (Category category : categories) {
			CompanyCategoriesResponseDto2 ccrdt = new CompanyCategoriesResponseDto2();
			ccrdt.setCategoryId(category.getId());
			ccrdt.setCategoryCode(category.getCategoryCode());
			ccrdt.setCategoryName(category.getCategoryName());
			ccrdt.setDescription(category.getDescription());
			ccrdt.setDisplayOrder(category.getDisplayOrder());
			ccrdt.setActive(category.getActive());
			ccrdt.setCreatedAt(category.getCreatedAt());

			categoriesResponseDto2s.add(ccrdt);
		}
		
		ccrd.setCategories(categoriesResponseDto2s);

		return new ApiResponse<>(true, message, HttpStatus.OK.name(), HttpStatus.OK.value(), ccrd);
	}

	@Transactional
	@Override
	public ApiResponse<CompanyCategoriesResponseDto1> bulkDeleteCategories(@Valid BulkCategoryStatusRequest request,
			UUID deletedBy) {

		List<Category> categories = categoryRepository.findByIdInAndDeletedAtIsNull(request.getCategoryIds());

		if (categories.isEmpty()) {
			throw new ResourceNotFoundException("No categories found");
		}

		if (categories.size() != request.getCategoryIds().size()) {
			throw new ResourceNotFoundException("Some categories not found or already deleted");
		}

		UUID companyId = categories.get(0).getCompanyId();

		boolean multipleCompanies = categories.stream().anyMatch(c -> !c.getCompanyId().equals(companyId));

		if (multipleCompanies) {
			throw new IllegalArgumentException("Bulk delete allowed for single company only");
		}

		LocalDateTime now = LocalDateTime.now();

		categories.forEach(category -> {
			category.setDeletedAt(now);
			category.setDeletedBy(deletedBy);
			category.setActive(false);
		});

		categoryRepository.saveAll(categories);

		CompanyResponseInternalDto crid = fetchCompany(companyId);

		CompanyCategoriesResponseDto1 ccrd = new CompanyCategoriesResponseDto1();
		ccrd.setCompanyId(crid.getCompanyId());
		ccrd.setCompanyName(crid.getCompanyName());
		ccrd.setCompanyCode(crid.getCompanyCode());
		ccrd.setActive(crid.getActive());

		List<CompanyCategoriesResponseDto2> categoryDtos = new ArrayList<>();
		for (Category category : categories) {
			CompanyCategoriesResponseDto2 dto = new CompanyCategoriesResponseDto2();
			dto.setCategoryId(category.getId());
			dto.setCategoryCode(category.getCategoryCode());
			dto.setCategoryName(category.getCategoryName());
			dto.setDescription(category.getDescription());
			dto.setDisplayOrder(category.getDisplayOrder());
			dto.setActive(category.getActive());
			dto.setCreatedAt(category.getCreatedAt());
			categoryDtos.add(dto);
		}

		ccrd.setCategories(categoryDtos);

		return new ApiResponse<>(true, "Categories deleted successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				ccrd);
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
