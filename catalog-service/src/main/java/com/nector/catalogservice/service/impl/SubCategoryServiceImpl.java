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

import com.nector.catalogservice.dto.request.internal.SubCategoryCreateRequest;
import com.nector.catalogservice.dto.request.internal.SubCategoryUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CategorySubCategoriesResponseDto1;
import com.nector.catalogservice.dto.response.internal.CategorySubCategoriesResponseDto2;
import com.nector.catalogservice.dto.response.internal.SubCategoryCategoryResponseDto1;
import com.nector.catalogservice.dto.response.internal.SubCategoryCategoryResponseDto2;
import com.nector.catalogservice.entity.Category;
import com.nector.catalogservice.entity.SubCategory;
import com.nector.catalogservice.exception.DuplicateResourceException;
import com.nector.catalogservice.exception.InactiveResourceException;
import com.nector.catalogservice.exception.ResourceNotFoundException;
import com.nector.catalogservice.repository.CategoryRepository;
import com.nector.catalogservice.repository.SubCategoryRepository;
import com.nector.catalogservice.service.SubCategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubCategoryServiceImpl implements SubCategoryService {

	private final SubCategoryRepository subCategoryRepository;
	private final CategoryRepository categoryRepository;

	@Transactional
	@Override
	public ApiResponse<SubCategoryCategoryResponseDto1> createSubCategory(@Valid SubCategoryCreateRequest request,
			UUID createdBy) {

		Category category = categoryRepository.findByIdAndDeletedAtIsNullAndActiveTrue(request.getCategoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Category not found or inactive!"));

		Optional<SubCategory> subCategoryOpt = subCategoryRepository
				.findBySubCategoryCode(request.getSubCategoryCode());

		SubCategory subCategory;
		if (subCategoryOpt.isPresent()) {

			SubCategory existing = subCategoryOpt.get();
			if (existing.getDeletedAt() != null) {
				existing.setDeletedAt(null);
				existing.setDeletedBy(null);
				existing.setActive(true);
				existing.setSubCategoryName(request.getSubCategoryName());
				existing.setCategoryId(request.getCategoryId());
				existing.setDescription(request.getDescription());
				existing.setDisplayOrder(request.getDisplayOrder());
				existing.setUpdatedAt(LocalDateTime.now());
				existing.setUpdatedBy(createdBy);

				subCategory = subCategoryRepository.save(existing);
			} else if (!existing.getActive()) {
				throw new InactiveResourceException("Sub-Category already exists but is inactive");
			} else {
				throw new DuplicateResourceException("Sub-Category code already exists!");
			}
		} else {

			subCategory = new SubCategory();
			subCategory.setSubCategoryCode(request.getSubCategoryCode());
			subCategory.setSubCategoryName(request.getSubCategoryName());
			subCategory.setCategoryId(request.getCategoryId());
			subCategory.setDescription(request.getDescription());
			subCategory.setDisplayOrder(request.getDisplayOrder());
			subCategory.setCreatedBy(createdBy);
		}

		SubCategory savedSubCategory = subCategoryRepository.save(subCategory);

//		RESPONSE BUILD
		SubCategoryCategoryResponseDto1 scrd = new SubCategoryCategoryResponseDto1();
		scrd.setSubCategoryId(savedSubCategory.getId());
		scrd.setSubCategoryCode(savedSubCategory.getSubCategoryCode());
		scrd.setSubCategoryName(savedSubCategory.getSubCategoryName());
		scrd.setDescription(savedSubCategory.getDescription());
		scrd.setDisplayOrder(savedSubCategory.getDisplayOrder());
		scrd.setActive(savedSubCategory.getActive());
		scrd.setCreatedAt(savedSubCategory.getCreatedAt());

		SubCategoryCategoryResponseDto2 srcrdt = new SubCategoryCategoryResponseDto2();
		srcrdt.setCategoryId(category.getId());
		srcrdt.setCategoryCode(category.getCategoryCode());
		srcrdt.setCategoryName(category.getCategoryName());
		srcrdt.setDescription(category.getDescription());
		srcrdt.setDisplayOrder(category.getDisplayOrder());
		srcrdt.setActive(category.getActive());

		scrd.setCategory(srcrdt);

		return new ApiResponse<>(true, "Sub-Category created successfully...", HttpStatus.OK.name(),
				HttpStatus.OK.value(), scrd);

	}

	@Transactional
	@Override
	public ApiResponse<SubCategoryCategoryResponseDto1> updateSubCategory(UUID subCategoryId,
			@Valid SubCategoryUpdateRequest request, UUID updatedBy) {

		SubCategory subCategory = subCategoryRepository.findByIdAndDeletedAtIsNull(subCategoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Sub-Category not found"));

		if (request.getSubCategoryName() != null)
			subCategory.setSubCategoryName(request.getSubCategoryName());

		if (request.getDescription() != null)
			subCategory.setDescription(request.getDescription());

		if (request.getDisplayOrder() != null)
			subCategory.setDisplayOrder(request.getDisplayOrder());

		if (request.getActive() != null)
			subCategory.setActive(request.getActive());

		SubCategoryCategoryResponseDto2 categoryDto = new SubCategoryCategoryResponseDto2();

		if (request.getCategoryId() != null && !request.getCategoryId().equals(subCategory.getCategoryId())) {

			Category newCategory = categoryRepository.findByIdAndDeletedAtIsNullAndActiveTrue(request.getCategoryId())
					.orElseThrow(() -> new ResourceNotFoundException("Target Category not found or inactive"));

			subCategory.setCategoryId(newCategory.getId());
			mapCategory(categoryDto, newCategory);

		} else {
			Category category = categoryRepository.findByIdAndDeletedAtIsNull(subCategory.getCategoryId())
					.orElseThrow(() -> new ResourceNotFoundException("Parent Category not found"));

			mapCategory(categoryDto, category);
		}

		subCategory.setUpdatedBy(updatedBy);

		SubCategory updatedSubCategory = subCategoryRepository.save(subCategory);

		SubCategoryCategoryResponseDto1 scrd = new SubCategoryCategoryResponseDto1();
		scrd.setSubCategoryId(updatedSubCategory.getId());
		scrd.setSubCategoryCode(updatedSubCategory.getSubCategoryCode());
		scrd.setSubCategoryName(updatedSubCategory.getSubCategoryName());
		scrd.setDescription(updatedSubCategory.getDescription());
		scrd.setDisplayOrder(updatedSubCategory.getDisplayOrder());
		scrd.setActive(updatedSubCategory.getActive());
		scrd.setCreatedAt(updatedSubCategory.getCreatedAt());
		scrd.setCategory(categoryDto);

		return new ApiResponse<>(true, "Sub-Category updated successfully...", HttpStatus.OK.name(),
				HttpStatus.OK.value(), scrd);
	}

	private void mapCategory(SubCategoryCategoryResponseDto2 dto, Category category) {
		dto.setCategoryId(category.getId());
		dto.setCategoryCode(category.getCategoryCode());
		dto.setCategoryName(category.getCategoryName());
		dto.setDescription(category.getDescription());
		dto.setDisplayOrder(category.getDisplayOrder());
		dto.setActive(category.getActive());
	}

	@Transactional
	@Override
	public ApiResponse<List<Object>> deleteSubCategory(UUID subCategoryId, UUID deletedBy) {

		SubCategory subCategory = subCategoryRepository.findByIdAndDeletedAtIsNull(subCategoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Sub-Category not found or already deleted!"));

		subCategory.setDeletedAt(LocalDateTime.now());
		subCategory.setDeletedBy(deletedBy);
		subCategory.setActive(false);
		subCategoryRepository.save(subCategory);

		return new ApiResponse<>(true, "Sub-Category deleted successfully...", HttpStatus.OK.name(),
				HttpStatus.OK.value(), Collections.emptyList());

	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<SubCategoryCategoryResponseDto1> getSubCategoryBySubCategoryId(UUID subCategoryId) {

		SubCategory subCategory = subCategoryRepository.findByIdAndDeletedAtIsNullAndActiveTrue(subCategoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Active Sub-Category not found or already deleted!"));

		Category category = categoryRepository.findByIdAndDeletedAtIsNullAndActiveTrue(subCategory.getCategoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Parent Category not found or inactive"));

//		RESPONSE BUILD
		SubCategoryCategoryResponseDto1 scrd = new SubCategoryCategoryResponseDto1();
		scrd.setSubCategoryId(subCategory.getId());
		scrd.setSubCategoryCode(subCategory.getSubCategoryCode());
		scrd.setSubCategoryName(subCategory.getSubCategoryName());
		scrd.setDescription(subCategory.getDescription());
		scrd.setDisplayOrder(subCategory.getDisplayOrder());
		scrd.setActive(subCategory.getActive());
		scrd.setCreatedAt(subCategory.getCreatedAt());

		SubCategoryCategoryResponseDto2 srcrdt = new SubCategoryCategoryResponseDto2();
		srcrdt.setCategoryId(category.getId());
		srcrdt.setCategoryCode(category.getCategoryCode());
		srcrdt.setCategoryName(category.getCategoryName());
		srcrdt.setDescription(category.getDescription());
		srcrdt.setDisplayOrder(category.getDisplayOrder());
		srcrdt.setActive(category.getActive());

		scrd.setCategory(srcrdt);

		return new ApiResponse<>(true, "Sub-Category data fetch successfully...", HttpStatus.OK.name(),
				HttpStatus.OK.value(), scrd);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<CategorySubCategoriesResponseDto1> getAllActiveSubCategoriesByCategoryId(UUID categoryId) {
		
		Category category = categoryRepository.findByIdAndDeletedAtIsNull(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
		if (!category.getActive()) {
			throw new InactiveResourceException("Category is inactive");
		}
		
		List<SubCategory> subCatgories = subCategoryRepository.findByCategoryIdAndDeletedAtIsNullAndActiveTrue(category.getId());
		if (subCatgories.isEmpty()) {
			throw new ResourceNotFoundException("No Active Sub-Category found for this Category");
		}
		
		List<CategorySubCategoriesResponseDto2> cscrdtList = new ArrayList<>();
		for (SubCategory sc : subCatgories) {
			CategorySubCategoriesResponseDto2 csrdt = new CategorySubCategoriesResponseDto2();
			csrdt.setSubCategoryId(sc.getId());
			csrdt.setSubCategoryCode(sc.getSubCategoryCode());
			csrdt.setSubCategoryName(sc.getSubCategoryName());
			csrdt.setDescription(sc.getDescription());
			csrdt.setDisplayOrder(sc.getDisplayOrder());
			csrdt.setActive(sc.getActive());
			csrdt.setCreatedAt(sc.getCreatedAt());
			
			cscrdtList.add(csrdt);
		}
		
		CategorySubCategoriesResponseDto1 csrd = new CategorySubCategoriesResponseDto1();
		csrd.setCategoryId(category.getId());
		csrd.setCategoryCode(category.getCategoryCode());
		csrd.setCategoryName(category.getCategoryName());
		csrd.setDescription(category.getDescription());
		csrd.setDisplayOrder(category.getDisplayOrder());
		csrd.setActive(category.getActive());
		
		csrd.setSubCategories(cscrdtList);

		return new ApiResponse<>(true, "Sub-Category data fetch successfully...", HttpStatus.OK.name(),
				HttpStatus.OK.value(), csrd);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<CategorySubCategoriesResponseDto1> getAllInactiveSubCategoriesByCategoryId(UUID categoryId) {
		
		Category category = categoryRepository.findByIdAndDeletedAtIsNull(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
		if (!category.getActive()) {
			throw new InactiveResourceException("Category is inactive");
		}
		
		List<SubCategory> subCatgories = subCategoryRepository.findByCategoryIdAndDeletedAtIsNullAndActiveFalse(category.getId());
		if (subCatgories.isEmpty()) {
			throw new ResourceNotFoundException("No Inactive Sub-Category found for this Category");
		}
		
		List<CategorySubCategoriesResponseDto2> cscrdtList = new ArrayList<>();
		for (SubCategory sc : subCatgories) {
			CategorySubCategoriesResponseDto2 csrdt = new CategorySubCategoriesResponseDto2();
			csrdt.setSubCategoryId(sc.getId());
			csrdt.setSubCategoryCode(sc.getSubCategoryCode());
			csrdt.setSubCategoryName(sc.getSubCategoryName());
			csrdt.setDescription(sc.getDescription());
			csrdt.setDisplayOrder(sc.getDisplayOrder());
			csrdt.setActive(sc.getActive());
			csrdt.setCreatedAt(sc.getCreatedAt());
			
			cscrdtList.add(csrdt);
		}
		
		CategorySubCategoriesResponseDto1 csrd = new CategorySubCategoriesResponseDto1();
		csrd.setCategoryId(category.getId());
		csrd.setCategoryCode(category.getCategoryCode());
		csrd.setCategoryName(category.getCategoryName());
		csrd.setDescription(category.getDescription());
		csrd.setDisplayOrder(category.getDisplayOrder());
		csrd.setActive(category.getActive());
		
		csrd.setSubCategories(cscrdtList);

		return new ApiResponse<>(true, "Sub-Category data fetch successfully...", HttpStatus.OK.name(),
				HttpStatus.OK.value(), csrd);		
		
	}

}
