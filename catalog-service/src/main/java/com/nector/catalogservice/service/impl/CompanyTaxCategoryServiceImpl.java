package com.nector.catalogservice.service.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nector.catalogservice.client.OrgServiceClient;
import com.nector.catalogservice.dto.request.internal.CompanyTaxCategoryCreateRequest;
import com.nector.catalogservice.dto.request.internal.CompanyTaxCategoryUpdateRequest;
import com.nector.catalogservice.dto.response.external.CompanyResponseExternalDto;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CompanyResponseInternalDto;
import com.nector.catalogservice.dto.response.internal.CompanyTaxCategoryPageResponse;
import com.nector.catalogservice.dto.response.internal.CompanyTaxCategoryResponse;
import com.nector.catalogservice.dto.response.internal.CompanyTaxCategoryResponseByCompany;
import com.nector.catalogservice.dto.response.internal.CompanyWithTaxCategoriesResponse;
import com.nector.catalogservice.dto.response.internal.PageMeta;
import com.nector.catalogservice.dto.response.internal.PagedResponse;
import com.nector.catalogservice.dto.response.internal.TaxMasterResponse;
import com.nector.catalogservice.entity.CompanyTaxCategory;
import com.nector.catalogservice.entity.TaxMaster;
import com.nector.catalogservice.exception.DuplicateResourceException;
import com.nector.catalogservice.exception.InactiveResourceException;
import com.nector.catalogservice.exception.OrgServiceException;
import com.nector.catalogservice.exception.ResourceNotFoundException;
import com.nector.catalogservice.repository.CompanyTaxCategoryRepository;
import com.nector.catalogservice.repository.TaxMasterRepository;
import com.nector.catalogservice.service.CompanyTaxCategoryService;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyTaxCategoryServiceImpl implements CompanyTaxCategoryService {

	private final CompanyTaxCategoryRepository companyTaxCategoryRepository;
	private final OrgServiceClient orgServiceClient;
	private final TaxMasterRepository taxMasterRepository;

	@Transactional
	@Override
	public ApiResponse<CompanyTaxCategoryResponse> createCompanyTaxCategory(CompanyTaxCategoryCreateRequest request,
			UUID createdBy) {

		Optional<CompanyTaxCategory> existing = companyTaxCategoryRepository
				.findByCompanyIdAndTaxMasterIdAndEffectiveFromAndDeletedAtIsNull(request.getCompanyId(),
						request.getTaxMasterId(), request.getEffectiveFrom());

		if (existing.isPresent()) {
			throw new DuplicateResourceException(
					"CompanyTaxCategory already exists for this company, tax, and effectiveFrom date");
		}

		CompanyResponseExternalDto companyResponse;
		try {
			companyResponse = orgServiceClient.getCompanyBasic(request.getCompanyId()).getBody().getData();
		} catch (FeignException e) {
			HttpStatus status = HttpStatus.resolve(e.status());
			String message = (status == HttpStatus.NOT_FOUND) ? "Company not found!"
					: "Error while communicating with Organization Service";
			throw new OrgServiceException(message, status, e);
		}

		TaxMaster taxMaster = taxMasterRepository.findByIdAndDeletedAtIsNull(request.getTaxMasterId())
				.orElseThrow(() -> new ResourceNotFoundException("Tax master data not found or deleted"));
		if (!taxMaster.getActive()) {
			throw new InactiveResourceException("Tax master data is inactive");
		}

		CompanyTaxCategory entity = new CompanyTaxCategory();
		entity.setCompanyId(request.getCompanyId());
		entity.setTaxMasterId(request.getTaxMasterId());
		entity.setTaxRate(request.getTaxRate());
		entity.setHsnCode(request.getHsnCode());
		entity.setEffectiveFrom(request.getEffectiveFrom());
		entity.setEffectiveTo(request.getEffectiveTo());
		entity.setCreatedBy(createdBy);

		CompanyTaxCategory saved = companyTaxCategoryRepository.save(entity);

		CompanyTaxCategoryResponse response = toResponse(saved, taxMaster, companyResponse);

		return new ApiResponse<>(true, "Company tax category created successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), response);
	}

	@Transactional
	@Override
	public ApiResponse<CompanyTaxCategoryResponse> updateCompanyTaxCategory(UUID id,
			CompanyTaxCategoryUpdateRequest request, UUID updatedBy) {

		CompanyTaxCategory entity = companyTaxCategoryRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new ResourceNotFoundException("Company tax category not found or deleted"));

		if (request.getTaxRate() != null)
			entity.setTaxRate(request.getTaxRate());
		if (request.getHsnCode() != null)
			entity.setHsnCode(request.getHsnCode());
		if (request.getEffectiveTo() != null)
			entity.setEffectiveTo(request.getEffectiveTo());
		if (request.getActive() != null)
			entity.setActive(request.getActive());

		entity.setUpdatedAt(LocalDateTime.now());
		entity.setUpdatedBy(updatedBy);

		CompanyTaxCategory saved = companyTaxCategoryRepository.save(entity);

		TaxMaster taxMaster = taxMasterRepository.findByIdAndDeletedAtIsNull(entity.getTaxMasterId())
				.orElseThrow(() -> new ResourceNotFoundException("Tax master data not found or deleted"));

		CompanyResponseExternalDto companyResponse;
		try {
			companyResponse = orgServiceClient.getCompanyBasic(entity.getCompanyId()).getBody().getData();
		} catch (FeignException e) {
			throw new OrgServiceException("Error while communicating with Organization Service", null, e);
		}

		CompanyTaxCategoryResponse response = toResponse(saved, taxMaster, companyResponse);

		return new ApiResponse<>(true, "Company tax category updated successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), response);
	}

	@Transactional
	@Override
	public ApiResponse<List<Object>> deleteCompanyTaxCategory(UUID id, UUID deletedBy) {

		CompanyTaxCategory entity = companyTaxCategoryRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new ResourceNotFoundException("Company tax category not found or already deleted"));

		entity.setDeletedAt(java.time.LocalDateTime.now());
		entity.setDeletedBy(deletedBy);
		entity.setActive(false);

		companyTaxCategoryRepository.save(entity);

		return new ApiResponse<>(true, "Company tax category deleted successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), Collections.emptyList());
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<CompanyTaxCategoryResponse> getCompanyTaxCategoryById(UUID id) {

		CompanyTaxCategory entity = companyTaxCategoryRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new ResourceNotFoundException("Company tax category not found or deleted"));

		TaxMaster taxMaster = taxMasterRepository.findByIdAndDeletedAtIsNull(entity.getTaxMasterId())
				.orElseThrow(() -> new ResourceNotFoundException("Tax master not found or deleted"));

		CompanyResponseExternalDto companyResponse;
		try {
			companyResponse = orgServiceClient.getCompanyBasic(entity.getCompanyId()).getBody().getData();
		} catch (FeignException e) {
			throw new OrgServiceException("Error while fetching company details", HttpStatus.resolve(e.status()), e);
		}

		CompanyTaxCategoryResponse response = toResponse(entity, taxMaster, companyResponse);

		return new ApiResponse<>(true, "Company tax category fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), response);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<CompanyTaxCategoryPageResponse> getCompanyTaxCategoryByCompany(UUID companyId, Boolean active,
			int page, int size, String sort) {
		CompanyResponseExternalDto company;
		try {
			company = orgServiceClient.getCompanyBasic(companyId).getBody().getData();
		} catch (FeignException e) {
			throw new OrgServiceException("Company not found", HttpStatus.NOT_FOUND, e);
		}

		String[] sortParams = sort.split(",");
		Pageable pageable = PageRequest.of(page, size,
				Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]));

		Page<CompanyTaxCategory> pageData = (active == null)
				? companyTaxCategoryRepository.findByCompanyIdAndDeletedAtIsNull(companyId, pageable)
				: companyTaxCategoryRepository.findByCompanyIdAndActiveAndDeletedAtIsNull(companyId, active, pageable);

		if (pageData.isEmpty()) {
			throw new ResourceNotFoundException("No company tax categories found");
		}

		Set<UUID> taxMasterIds = pageData.getContent().stream().map(CompanyTaxCategory::getTaxMasterId)
				.collect(Collectors.toSet());

		Map<UUID, TaxMaster> taxMasterMap = taxMasterRepository.findByIdInAndDeletedAtIsNull(taxMasterIds).stream()
				.collect(Collectors.toMap(TaxMaster::getId, t -> t));

		List<CompanyTaxCategoryResponseByCompany> taxCategoryItems = pageData.getContent().stream().map(entity -> {

			TaxMaster taxMaster = taxMasterMap.get(entity.getTaxMasterId());
			if (taxMaster == null) {
				throw new ResourceNotFoundException("Tax master not found");
			}

			CompanyTaxCategoryResponseByCompany item = new CompanyTaxCategoryResponseByCompany();
			item.setCompanyTaxCategoryId(entity.getId());
			item.setTaxRate(entity.getTaxRate());
			item.setHsnCode(entity.getHsnCode());
			item.setEffectiveFrom(entity.getEffectiveFrom());
			item.setEffectiveTo(entity.getEffectiveTo());
			item.setActive(entity.getActive());

			TaxMasterResponse tmr = new TaxMasterResponse();
			tmr.setTaxMasterId(taxMaster.getId());
			tmr.setTaxCode(taxMaster.getTaxCode());
			tmr.setTaxName(taxMaster.getTaxName());
			tmr.setTaxType(taxMaster.getTaxType());
			tmr.setCompoundTax(taxMaster.getCompoundTax());
			tmr.setDescription(taxMaster.getDescription());
			tmr.setActive(taxMaster.getActive());
			tmr.setCreatedAt(taxMaster.getCreatedAt());
			tmr.setUpdatedAt(taxMaster.getUpdatedAt());
			item.setTaxMaster(tmr);

			return item;
		}).toList();

		CompanyResponseInternalDto companyDto = new CompanyResponseInternalDto();
		companyDto.setCompanyId(company.getCompanyId());
		companyDto.setCompanyCode(company.getCompanyCode());
		companyDto.setCompanyName(company.getCompanyName());
		companyDto.setActive(company.getActive());

		PageMeta pageMeta = new PageMeta();
		pageMeta.setPageNumber(pageData.getNumber());
		pageMeta.setPageSize(pageData.getSize());
		pageMeta.setTotalElements(pageData.getTotalElements());
		pageMeta.setTotalPages(pageData.getTotalPages());
		pageMeta.setLast(pageData.isLast());

		CompanyTaxCategoryPageResponse response = new CompanyTaxCategoryPageResponse();
		response.setCompany(companyDto);
		response.setTaxCategories(taxCategoryItems);
		response.setPage(pageMeta);

		return new ApiResponse<>(true, "Company tax categories fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), response);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<CompanyTaxCategoryResponse> getByCompanyAndTax(UUID companyId, UUID taxMasterId) {

		CompanyResponseExternalDto company;
		try {
			company = orgServiceClient.getCompanyBasic(companyId).getBody().getData();
		} catch (FeignException e) {
			throw new OrgServiceException("Company not found", HttpStatus.NOT_FOUND, e);
		}

		CompanyTaxCategory entity = companyTaxCategoryRepository
				.findByCompanyIdAndTaxMasterIdAndDeletedAtIsNull(companyId, taxMasterId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Company tax category not found for given company and tax master"));

		if (!entity.getActive()) {
			throw new InactiveResourceException("Company tax category is inactive");
		}

		TaxMaster taxMaster = taxMasterRepository.findByIdAndDeletedAtIsNull(taxMasterId)
				.orElseThrow(() -> new ResourceNotFoundException("Tax master not found"));

		CompanyTaxCategoryResponse response = new CompanyTaxCategoryResponse();
		response.setCompanyTaxCategoryId(entity.getId());
		response.setTaxRate(entity.getTaxRate());
		response.setHsnCode(entity.getHsnCode());
		response.setEffectiveFrom(entity.getEffectiveFrom());
		response.setEffectiveTo(entity.getEffectiveTo());
		response.setActive(entity.getActive());
		response.setCreatedAt(entity.getCreatedAt());
		response.setUpdatedAt(entity.getUpdatedAt());

		TaxMasterResponse tmr = new TaxMasterResponse();
		tmr.setTaxMasterId(taxMaster.getId());
		tmr.setTaxCode(taxMaster.getTaxCode());
		tmr.setTaxName(taxMaster.getTaxName());
		tmr.setTaxType(taxMaster.getTaxType());
		tmr.setCompoundTax(taxMaster.getCompoundTax());
		tmr.setDescription(taxMaster.getDescription());
		tmr.setActive(taxMaster.getActive());
		tmr.setCreatedAt(taxMaster.getCreatedAt());
		tmr.setUpdatedAt(taxMaster.getUpdatedAt());
		response.setTaxMaster(tmr);

		CompanyResponseInternalDto crid = new CompanyResponseInternalDto();
		crid.setCompanyId(company.getCompanyId());
		crid.setCompanyCode(company.getCompanyCode());
		crid.setCompanyName(company.getCompanyName());
		crid.setActive(company.getActive());
		response.setCompany(crid);

		return new ApiResponse<>(true, "Company tax category fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), response);

	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<PagedResponse<CompanyWithTaxCategoriesResponse>> getAllTaxCategories(UUID companyId, Boolean active,
			int page, int size, String sort) {
		String[] sortParams = sort.split(",");
		Pageable pageable = PageRequest.of(page, size,
				Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]));

		Page<CompanyTaxCategory> pageData;
		if (companyId != null) {
			pageData = (active == null) ? companyTaxCategoryRepository.findByCompanyIdAndDeletedAtIsNull(companyId, pageable)
					: companyTaxCategoryRepository.findByCompanyIdAndActiveAndDeletedAtIsNull(companyId, active, pageable);
		} else {
			pageData = (active == null) ? companyTaxCategoryRepository.findByDeletedAtIsNull(pageable)
					: companyTaxCategoryRepository.findByActiveAndDeletedAtIsNull(active, pageable);
		}

		if (pageData.isEmpty()) {
			throw new ResourceNotFoundException("No company tax categories found");
		}

		List<CompanyTaxCategory> categories = pageData.getContent();

		Set<UUID> companyIds = categories.stream().map(CompanyTaxCategory::getCompanyId).collect(Collectors.toSet());

		Set<UUID> taxMasterIds = categories.stream().map(CompanyTaxCategory::getTaxMasterId)
				.collect(Collectors.toSet());

		Map<UUID, TaxMaster> taxMasterMap = taxMasterRepository.findByIdInAndDeletedAtIsNull(taxMasterIds).stream()
				.collect(Collectors.toMap(TaxMaster::getId, t -> t));

		Map<UUID, CompanyResponseExternalDto> companyMap = companyIds.stream()
				.collect(Collectors.toMap(id -> id, id -> {
					try {
						return orgServiceClient.getCompanyBasic(id).getBody().getData();
					} catch (FeignException e) {
						throw new OrgServiceException("Error fetching company: " + id, HttpStatus.NOT_FOUND, e);
					}
				}));

		Map<UUID, List<CompanyTaxCategoryResponse>> groupedByCompany = categories.stream().map(c -> {
			TaxMaster taxMaster = taxMasterMap.get(c.getTaxMasterId());
			if (taxMaster == null)
				throw new ResourceNotFoundException("Tax master not found: " + c.getTaxMasterId());

			CompanyResponseExternalDto company = companyMap.get(c.getCompanyId());
			return toResponse(c, taxMaster, company);
		}).collect(Collectors.groupingBy(r -> r.getCompany().getCompanyId()));

		List<CompanyWithTaxCategoriesResponse> finalList = groupedByCompany.entrySet().stream().map(entry -> {
			CompanyResponseExternalDto company = companyMap.get(entry.getKey());
			CompanyWithTaxCategoriesResponse dto = new CompanyWithTaxCategoriesResponse();
			dto.setCompanyId(company.getCompanyId());
			dto.setCompanyCode(company.getCompanyCode());
			dto.setCompanyName(company.getCompanyName());
			dto.setActive(company.getActive());
			dto.setTaxCategories(entry.getValue());
			return dto;
		}).toList();

		PagedResponse<CompanyWithTaxCategoriesResponse> pagedResponse = new PagedResponse<>();
		pagedResponse.setContent(finalList);
		pagedResponse.setPageNumber(pageData.getNumber());
		pagedResponse.setPageSize(pageData.getSize());
		pagedResponse.setTotalElements(pageData.getTotalElements());
		pagedResponse.setTotalPages(pageData.getTotalPages());
		pagedResponse.setLast(pageData.isLast());

		return new ApiResponse<>(true, "Company tax categories fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), pagedResponse);
	}

//	---------------------------------------------
	private CompanyTaxCategoryResponse toResponse(CompanyTaxCategory saved, TaxMaster taxMaster,
			CompanyResponseExternalDto companyResponse) {

		CompanyResponseInternalDto crid = new CompanyResponseInternalDto();
		crid.setCompanyId(companyResponse.getCompanyId());
		crid.setCompanyCode(companyResponse.getCompanyCode());
		crid.setCompanyName(companyResponse.getCompanyName());
		crid.setActive(companyResponse.getActive());

		TaxMasterResponse tmr = new TaxMasterResponse();
		tmr.setTaxMasterId(taxMaster.getId());
		tmr.setTaxCode(taxMaster.getTaxCode());
		tmr.setTaxName(taxMaster.getTaxName());
		tmr.setTaxType(taxMaster.getTaxType());
		tmr.setCompoundTax(taxMaster.getCompoundTax());
		tmr.setDescription(taxMaster.getDescription());
		tmr.setActive(taxMaster.getActive());
		tmr.setCreatedAt(taxMaster.getCreatedAt());
		tmr.setUpdatedAt(taxMaster.getUpdatedAt());

		CompanyTaxCategoryResponse ctcr = new CompanyTaxCategoryResponse();
		ctcr.setCompanyTaxCategoryId(saved.getId());
		ctcr.setTaxRate(saved.getTaxRate());
		ctcr.setHsnCode(saved.getHsnCode());
		ctcr.setEffectiveFrom(saved.getEffectiveFrom());
		ctcr.setEffectiveTo(saved.getEffectiveTo());
		ctcr.setActive(saved.getActive());
		ctcr.setCreatedAt(saved.getCreatedAt());
		ctcr.setUpdatedAt(saved.getUpdatedAt());

		ctcr.setTaxMaster(tmr);
		ctcr.setCompany(crid);

		return ctcr;
	}

	

}
