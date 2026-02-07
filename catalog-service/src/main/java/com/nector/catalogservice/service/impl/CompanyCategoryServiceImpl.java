package com.nector.catalogservice.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nector.catalogservice.client.OrgServiceClient;
import com.nector.catalogservice.dto.request.internal.BulkCompanyCategoryStatusRequest;
import com.nector.catalogservice.dto.request.internal.CompanyCategoryCreateRequest;
import com.nector.catalogservice.dto.request.internal.CompanyCategoryUpdateRequest;
import com.nector.catalogservice.dto.response.external.CompanyResponseExternalDto;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CategoryResponse;
import com.nector.catalogservice.dto.response.internal.CompanyCategoriesCreationResponse;
import com.nector.catalogservice.dto.response.internal.CompanyCategoriesResponse;
import com.nector.catalogservice.dto.response.internal.CompanyCategoryCategoryResponse;
import com.nector.catalogservice.dto.response.internal.CompanyResponseInternalDto;
import com.nector.catalogservice.dto.response.internal.Company_CategoryResponse;
import com.nector.catalogservice.entity.Category;
import com.nector.catalogservice.entity.CompanyCategory;
import com.nector.catalogservice.exception.DuplicateResourceException;
import com.nector.catalogservice.exception.InactiveResourceException;
import com.nector.catalogservice.exception.OrgServiceException;
import com.nector.catalogservice.exception.ResourceNotFoundException;
import com.nector.catalogservice.repository.CategoryRepository;
import com.nector.catalogservice.repository.CompanyCategoryRepository;
import com.nector.catalogservice.repository.SubCategoryRepository;
import com.nector.catalogservice.service.CompanyCategoryService;

import feign.FeignException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyCategoryServiceImpl implements CompanyCategoryService {

	private final CompanyCategoryRepository companyCategoryRepository;
	private final OrgServiceClient orgServiceClient;
	private final CategoryRepository categoryRepository;
	private final SubCategoryRepository subCategoryRepository;

	@Transactional
	@Override
	public ApiResponse<CompanyCategoriesCreationResponse> createCompanyCategories(CompanyCategoryCreateRequest request,
			UUID createdBy) {

		CompanyCategoriesCreationResponse responseDto = new CompanyCategoriesCreationResponse();

		CompanyResponseExternalDto companyResponse;
		try {
			companyResponse = orgServiceClient.getCompanyBasic(request.getCompanyId()).getBody().getData();
		} catch (FeignException e) {
			HttpStatus status = HttpStatus.resolve(e.status());
			String message = (status == HttpStatus.NOT_FOUND) ? "Company not found!"
					: "Error while communicating with Organization Service";
			throw new OrgServiceException(message, status, e);
		}

		responseDto.setCompanyId(companyResponse.getCompanyId());
		responseDto.setCompanyCode(companyResponse.getCompanyCode());
		responseDto.setCompanyName(companyResponse.getCompanyName());
		responseDto.setActive(companyResponse.getActive());

//		---------------CATEGORY---------------
		List<Category> categories = categoryRepository.findByIdInAndDeletedAtIsNull(request.getCategoryIds());
		if (request.getCategoryIds().size() != categories.size()) {
			throw new ResourceNotFoundException("Some categories are not found");
		}

		Map<UUID, Category> categoryMap = categories.stream().filter(Category::getActive)
				.collect(Collectors.toMap(Category::getId, c -> c));

		if (categoryMap.isEmpty())
			throw new ResourceNotFoundException("No active categories found for given IDs");
//		--------------------------------------

//		---------------COMPANY-CATEGORY---------------
		List<CompanyCategory> existingMappings = companyCategoryRepository
				.findAllByCompanyIdAndCategoryIdInAndDeletedAtIsNull(request.getCompanyId(), request.getCategoryIds());

		Map<UUID, CompanyCategory> existingMap = existingMappings.stream()
				.collect(Collectors.toMap(CompanyCategory::getCategoryId, cc -> cc));
//		--------------------------------------

		List<CompanyCategory> toSave = new ArrayList<>();

		for (UUID catId : request.getCategoryIds()) {
			Category category = categoryMap.get(catId);
			if (category == null)
				continue;

			CompanyCategory cc = existingMap.getOrDefault(catId, new CompanyCategory());

			if (cc.getId() != null) {
				if (Boolean.FALSE.equals(cc.getActive())) {
					cc.setActive(true);
					cc.setDeletedAt(null);
					cc.setDeletedBy(null);
					cc.setUpdatedAt(LocalDateTime.now());
					cc.setUpdatedBy(createdBy);
					toSave.add(cc);
				} else {
					throw new DuplicateResourceException("Active Categories already assigned for this company!");
				}
			} else {
				cc.setCompanyId(request.getCompanyId());
				cc.setCategoryId(catId);
				cc.setCreatedBy(createdBy);
				toSave.add(cc);
			}

			CategoryResponse cr = new CategoryResponse();
			cr.setCategoryId(category.getId());
			cr.setCategoryCode(category.getCategoryCode());
			cr.setCategoryName(category.getCategoryName());
			cr.setDescription(category.getDescription());
			cr.setDisplayOrder(category.getDisplayOrder());
			cr.setActive(category.getActive());
			cr.setCreatedAt(category.getCreatedAt());

			responseDto.getCategories().add(cr);
		}

		// Bulk save
		if (!toSave.isEmpty()) {
			companyCategoryRepository.saveAll(toSave);
		}

		return new ApiResponse<>(true, "Selected categories added to the company successfully", "OK", 200, responseDto);
	}

	@Transactional
	@Override
	public ApiResponse<Company_CategoryResponse> updateCompanyCategory(UUID id, CompanyCategoryUpdateRequest request,
			UUID updatedBy) {
		CompanyCategory cc = companyCategoryRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new ResourceNotFoundException("No category found for the given company"));

		Category category = categoryRepository.findByIdAndDeletedAtIsNullAndActiveTrue(cc.getCategoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Assigned Category not found or inactive"));

		CompanyResponseExternalDto companyResponse;
		try {
			companyResponse = orgServiceClient.getCompanyBasic(cc.getCompanyId()).getBody().getData();
		} catch (FeignException e) {
			HttpStatus status = HttpStatus.resolve(e.status());
			String message = (status == HttpStatus.NOT_FOUND) ? "Assigned Company not found!"
					: "Error while communicating with Organization Service";
			throw new OrgServiceException(message, status, e);
		}

		if (request.getActive() != null) {
			cc.setActive(request.getActive());
			cc.setUpdatedBy(updatedBy);
		}

		CompanyCategory saved = companyCategoryRepository.save(cc);

//		BUILD RESPONSE
		CategoryResponse cr = new CategoryResponse();
		cr.setCategoryId(category.getId());
		cr.setCategoryCode(category.getCategoryCode());
		cr.setCategoryName(category.getCategoryName());
		cr.setDescription(category.getDescription());
		cr.setDisplayOrder(category.getDisplayOrder());
		cr.setActive(category.getActive());
		cr.setCreatedAt(category.getCreatedAt());

		CompanyResponseInternalDto crid = new CompanyResponseInternalDto();
		crid.setCompanyId(companyResponse.getCompanyId());
		crid.setCompanyCode(companyResponse.getCompanyCode());
		crid.setCompanyName(companyResponse.getCompanyName());
		crid.setActive(companyResponse.getActive());

		Company_CategoryResponse ccr = new Company_CategoryResponse();
		ccr.setCompanyCategoryId(cc.getId());
		ccr.setActive(cc.getActive());
		ccr.setCreatedAt(cc.getCreatedAt());

		ccr.setCategory(cr);
		ccr.setCompany(crid);

		return new ApiResponse<>(true, "Assigned category with company, updated successfully", "OK", 200, ccr);

	}

	@Transactional
	@Override
	public ApiResponse<List<Object>> deleteCompanyCategory(UUID id, UUID deletedBy) {

		CompanyCategory cc = companyCategoryRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new ResourceNotFoundException("CompanyCategory not found or already deleted!"));

		cc.setDeletedAt(LocalDateTime.now());
		cc.setDeletedBy(deletedBy);
		cc.setActive(false);
		companyCategoryRepository.save(cc);

		return new ApiResponse<>(true, "Assigned category with company, deleted successfully", "OK", 200,
				Collections.emptyList());
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<Company_CategoryResponse> getCompanyCategoryById(UUID id) {
		CompanyCategory cc = companyCategoryRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new ResourceNotFoundException("No category found for selected company"));

		if (!cc.getActive()) {
			throw new InactiveResourceException("Assigned category for this company is inactive");
		}

		Category category = categoryRepository.findByIdAndDeletedAtIsNull(cc.getCategoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Assigned Category not found"));

		CompanyResponseExternalDto companyResponse;
		try {
			companyResponse = orgServiceClient.getCompanyBasic(cc.getCompanyId()).getBody().getData();
		} catch (FeignException e) {
			HttpStatus status = HttpStatus.resolve(e.status());
			String message = (status == HttpStatus.NOT_FOUND) ? "Assigned Company not found!"
					: "Error while communicating with Organization Service";
			throw new OrgServiceException(message, status, e);
		}

//		BUILD RESPONSE
		CategoryResponse cr = new CategoryResponse();
		cr.setCategoryId(category.getId());
		cr.setCategoryCode(category.getCategoryCode());
		cr.setCategoryName(category.getCategoryName());
		cr.setDescription(category.getDescription());
		cr.setDisplayOrder(category.getDisplayOrder());
		cr.setActive(category.getActive());
		cr.setCreatedAt(category.getCreatedAt());

		CompanyResponseInternalDto crid = new CompanyResponseInternalDto();
		crid.setCompanyId(companyResponse.getCompanyId());
		crid.setCompanyCode(companyResponse.getCompanyCode());
		crid.setCompanyName(companyResponse.getCompanyName());
		crid.setActive(companyResponse.getActive());

		Company_CategoryResponse ccr = new Company_CategoryResponse();
		ccr.setCompanyCategoryId(cc.getId());
		ccr.setActive(cc.getActive());
		ccr.setCreatedAt(cc.getCreatedAt());

		ccr.setCategory(cr);
		ccr.setCompany(crid);

		return new ApiResponse<>(true, "Fetched successfully", "OK", 200, ccr);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<CompanyCategoriesResponse> getAllActiveCompanyCategoriesByCompanyId(UUID companyId) {

		CompanyResponseExternalDto companyResponse;
		try {
			companyResponse = orgServiceClient.getCompanyBasic(companyId).getBody().getData();
		} catch (FeignException e) {
			HttpStatus status = HttpStatus.resolve(e.status());
			String message = (status == HttpStatus.NOT_FOUND) ? "Company not found!"
					: "Error while communicating with Organization Service";
			throw new OrgServiceException(message, status, e);
		}
		CompanyResponseInternalDto crid = new CompanyResponseInternalDto();
		crid.setCompanyId(companyResponse.getCompanyId());
		crid.setCompanyCode(companyResponse.getCompanyCode());
		crid.setCompanyName(companyResponse.getCompanyName());
		crid.setActive(companyResponse.getActive());

		List<CompanyCategory> companyCategories = companyCategoryRepository
				.findByCompanyIdAndDeletedAtIsNullAndActiveTrue(companyId);

		if (companyCategories.isEmpty()) {
			throw new ResourceNotFoundException("No active categories found for this company");
		}

		Map<UUID, CompanyCategory> companyCategoryMap = new HashMap<>();
		List<UUID> categoryIds = new ArrayList<>();
		for (CompanyCategory companyCategory : companyCategories) {
			companyCategoryMap.put(companyCategory.getId(), companyCategory);
			categoryIds.add(companyCategory.getCategoryId());
		}

		List<Category> categories = categoryRepository.findByIdInAndDeletedAtIsNull(categoryIds);
		Map<UUID, Category> categoryMap = new HashMap<>();
		for (Category category2 : categories) {
			categoryMap.put(category2.getId(), category2);
		}

		List<CompanyCategoryCategoryResponse> cCsCategoriesResponseDto2s = new ArrayList<>();
		for (CompanyCategory cc : companyCategories) {

			Category category = categoryMap.get(cc.getCategoryId());

			CategoryResponse cr = new CategoryResponse();
			cr.setCategoryId(category.getId());
			cr.setCategoryCode(category.getCategoryCode());
			cr.setCategoryName(category.getCategoryName());
			cr.setDescription(category.getDescription());
			cr.setDisplayOrder(category.getDisplayOrder());
			cr.setActive(category.getActive());
			cr.setCreatedAt(category.getCreatedAt());

			CompanyCategoryCategoryResponse ccrd = new CompanyCategoryCategoryResponse();
			ccrd.setCompanyCategoryId(cc.getId());
			ccrd.setActive(cc.getActive());
			ccrd.setCreatedAt(cc.getCreatedAt());
			ccrd.setCategory(cr);

			cCsCategoriesResponseDto2s.add(ccrd);
		}

		CompanyCategoriesResponse cccrd = new CompanyCategoriesResponse();
		cccrd.setCompany(crid);
		cccrd.setCategories(cCsCategoriesResponseDto2s);

		return new ApiResponse<>(true, "Fetched successfully", "OK", 200, cccrd);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<CompanyCategoriesResponse> getAllInactiveCompanyCategoriesByCompanyId(UUID companyId) {

		CompanyResponseExternalDto companyResponse;
		try {
			companyResponse = orgServiceClient.getCompanyBasic(companyId).getBody().getData();
		} catch (FeignException e) {
			HttpStatus status = HttpStatus.resolve(e.status());
			String message = (status == HttpStatus.NOT_FOUND) ? "Company not found!"
					: "Error while communicating with Organization Service";
			throw new OrgServiceException(message, status, e);
		}
		CompanyResponseInternalDto crid = new CompanyResponseInternalDto();
		crid.setCompanyId(companyResponse.getCompanyId());
		crid.setCompanyCode(companyResponse.getCompanyCode());
		crid.setCompanyName(companyResponse.getCompanyName());
		crid.setActive(companyResponse.getActive());

		List<CompanyCategory> companyCategories = companyCategoryRepository
				.findByCompanyIdAndDeletedAtIsNullAndActiveFalse(companyId);

		if (companyCategories.isEmpty()) {
			throw new ResourceNotFoundException("No inactive categories found for this company");
		}

		Map<UUID, CompanyCategory> companyCategoryMap = new HashMap<>();
		List<UUID> categoryIds = new ArrayList<>();
		for (CompanyCategory companyCategory : companyCategories) {
			companyCategoryMap.put(companyCategory.getId(), companyCategory);
			categoryIds.add(companyCategory.getCategoryId());
		}

		List<Category> categories = categoryRepository.findByIdInAndDeletedAtIsNull(categoryIds);
		Map<UUID, Category> categoryMap = new HashMap<>();
		for (Category category2 : categories) {
			categoryMap.put(category2.getId(), category2);
		}

		List<CompanyCategoryCategoryResponse> cCsCategoriesResponseDto2s = new ArrayList<>();
		for (CompanyCategory cc : companyCategories) {

			Category category = categoryMap.get(cc.getCategoryId());

			CategoryResponse cr = new CategoryResponse();
			cr.setCategoryId(category.getId());
			cr.setCategoryCode(category.getCategoryCode());
			cr.setCategoryName(category.getCategoryName());
			cr.setDescription(category.getDescription());
			cr.setDisplayOrder(category.getDisplayOrder());
			cr.setActive(category.getActive());
			cr.setCreatedAt(category.getCreatedAt());

			CompanyCategoryCategoryResponse ccrd = new CompanyCategoryCategoryResponse();
			ccrd.setCompanyCategoryId(cc.getId());
			ccrd.setActive(cc.getActive());
			ccrd.setCreatedAt(cc.getCreatedAt());
			ccrd.setCategory(cr);

			cCsCategoriesResponseDto2s.add(ccrd);
		}

		CompanyCategoriesResponse cccrd = new CompanyCategoriesResponse();
		cccrd.setCompany(crid);
		cccrd.setCategories(cCsCategoriesResponseDto2s);

		return new ApiResponse<>(true, "Fetched successfully", "OK", 200, cccrd);
	}

	@Transactional
	@Override
	public ApiResponse<CompanyCategoriesResponse> bulkUpdateCompanyCategoryActiveStatus(
			@Valid BulkCompanyCategoryStatusRequest request, boolean activeStatus, UUID updatedBy) {

		List<CompanyCategory> companyCategories = companyCategoryRepository
				.findByIdInAndDeletedAtIsNull(request.getCompanyCategoryIds());

		if (companyCategories.isEmpty()) {
			throw new ResourceNotFoundException("No company categories found");
		}

		if (companyCategories.size() != request.getCompanyCategoryIds().size()) {
			throw new ResourceNotFoundException("Some company categories not found or already deleted");
		}

		companyCategories.forEach(cc -> {
			if (!cc.getActive().equals(activeStatus)) {
				cc.setActive(activeStatus);
				cc.setUpdatedBy(updatedBy);
			}
		});

		companyCategoryRepository.saveAll(companyCategories);

		UUID companyId = companyCategories.get(0).getCompanyId();
		CompanyResponseExternalDto companyResponse;
		try {
			companyResponse = orgServiceClient.getCompanyBasic(companyId).getBody().getData();
		} catch (FeignException e) {
			throw new OrgServiceException("Error while fetching company details", HttpStatus.resolve(e.status()), e);
		}

		CompanyResponseInternalDto companyDto = new CompanyResponseInternalDto();
		companyDto.setCompanyId(companyResponse.getCompanyId());
		companyDto.setCompanyCode(companyResponse.getCompanyCode());
		companyDto.setCompanyName(companyResponse.getCompanyName());
		companyDto.setActive(companyResponse.getActive());

		List<UUID> categoryIds = companyCategories.stream().map(CompanyCategory::getCategoryId).distinct().toList();

		Map<UUID, Category> categoryMap = categoryRepository.findByIdInAndDeletedAtIsNull(categoryIds).stream()
				.collect(Collectors.toMap(Category::getId, c -> c));

		List<CompanyCategoryCategoryResponse> categoriesDtoList = new ArrayList<>();

		for (CompanyCategory cc : companyCategories) {

			Category category = categoryMap.get(cc.getCategoryId());
			if (category == null)
				continue;

			CategoryResponse cr = new CategoryResponse();
			cr.setCategoryId(category.getId());
			cr.setCategoryCode(category.getCategoryCode());
			cr.setCategoryName(category.getCategoryName());
			cr.setDescription(category.getDescription());
			cr.setDisplayOrder(category.getDisplayOrder());
			cr.setActive(category.getActive());
			cr.setCreatedAt(category.getCreatedAt());

			CompanyCategoryCategoryResponse item = new CompanyCategoryCategoryResponse();
			item.setCompanyCategoryId(cc.getId());
			item.setActive(cc.getActive());
			item.setCreatedAt(cc.getCreatedAt());
			item.setCategory(cr);

			categoriesDtoList.add(item);
		}

		CompanyCategoriesResponse response = new CompanyCategoriesResponse();
		response.setCompany(companyDto);
		response.setCategories(categoriesDtoList);

		String message = activeStatus ? "Categories enabled for company successfully"
				: "Categories disabled for company successfully";

		return new ApiResponse<>(true, message, HttpStatus.OK.name(), HttpStatus.OK.value(), response);
	}

	@Transactional
	@Override
	public ApiResponse<List<Object>> bulkDeleteCompanyCategoriesByCompanyId(UUID companyId,
			BulkCompanyCategoryStatusRequest request, UUID deletedBy) {

	    List<CompanyCategory> companyCategories = companyCategoryRepository
	            .findByIdInAndCompanyIdAndDeletedAtIsNull(request.getCompanyCategoryIds(), companyId);

	    if (companyCategories.isEmpty()) {
	    	throw new ResourceNotFoundException("No company categories found for the given company and IDs");
	    }

	    if (request.getCompanyCategoryIds().size() != companyCategories.size()) {
	    	throw new ResourceNotFoundException("Some company categories not found or already deleted");
		}

	    // Soft delete
	    companyCategories.forEach(cc -> {
	        cc.setDeletedAt(LocalDateTime.now());
	        cc.setDeletedBy(deletedBy);
	        cc.setActive(false);
	    });

	    companyCategoryRepository.saveAll(companyCategories);

	    return new ApiResponse<>(true, "Selected company categories deleted successfully", "OK", 200,
	            Collections.emptyList());
	}



}