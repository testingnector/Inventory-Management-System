package com.nector.catalogservice.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import com.nector.catalogservice.dto.response.internal.CompanyCompanyTaxCategoryCurrentResponse;
import com.nector.catalogservice.dto.response.internal.CompanyCompanyTaxCategoryHistoryResponse;
import com.nector.catalogservice.dto.response.internal.CompanyResponseInternalDto;
import com.nector.catalogservice.dto.response.internal.CompanyTaxCategoryResponse;
import com.nector.catalogservice.dto.response.internal.CompanyTaxCategoryResponseWithTaxMaster;
import com.nector.catalogservice.dto.response.internal.CompanyTaxCategoryResponseWithTaxMasterAndCompany;
import com.nector.catalogservice.dto.response.internal.CompanyTaxMasterCompanyTaxCategoryHistory;
import com.nector.catalogservice.dto.response.internal.CompanyWithTaxCategoriesResponse;
import com.nector.catalogservice.dto.response.internal.PagedResponse;
import com.nector.catalogservice.dto.response.internal.TaxMasterResponse;
import com.nector.catalogservice.dto.response.internal.TaxMasterWithCompanyTaxCategoryCurrentResponse;
import com.nector.catalogservice.dto.response.internal.TaxMasterWithCompanyTaxCategoryHistoryResponse;
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
	public ApiResponse<CompanyTaxCategoryResponseWithTaxMasterAndCompany> createCompanyTaxCategory(
			CompanyTaxCategoryCreateRequest request, UUID createdBy) {

		boolean currentExists = companyTaxCategoryRepository.existsCurrentTax(request.getCompanyId(),
				request.getTaxMasterId(), request.getEffectiveFrom());

		if (currentExists) {
			throw new DuplicateResourceException(
					"Current tax already exists for this company and tax master. Close existing tax before adding a new one.");
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

		CompanyTaxCategoryResponseWithTaxMasterAndCompany response = toResponse(saved, taxMaster, companyResponse);

		return new ApiResponse<>(true, "Company tax category created successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), response);
	}

	@Transactional
	@Override
	public ApiResponse<CompanyTaxCategoryResponseWithTaxMasterAndCompany> updateCompanyTaxCategory(UUID id,
			CompanyTaxCategoryUpdateRequest request, UUID updatedBy) {

		CompanyTaxCategory entity = companyTaxCategoryRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new ResourceNotFoundException("Company tax category not found or deleted"));

		if (Boolean.TRUE.equals(request.getActive())) {

			boolean conflict = companyTaxCategoryRepository.existsAnotherCurrentActive(entity.getCompanyId(),
					entity.getTaxMasterId(), entity.getId(), LocalDate.now());

			if (conflict) {
				throw new DuplicateResourceException(
						"Another current active tax already exists for this company and tax master");
			}
		}

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

		CompanyTaxCategoryResponseWithTaxMasterAndCompany response = toResponse(saved, taxMaster, companyResponse);

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
	public ApiResponse<CompanyTaxCategoryResponseWithTaxMasterAndCompany> getCompanyTaxCategoryById(UUID id) {

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

		CompanyTaxCategoryResponseWithTaxMasterAndCompany response = toResponse(entity, taxMaster, companyResponse);

		return new ApiResponse<>(true, "Company tax category fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), response);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<CompanyTaxCategoryResponseWithTaxMasterAndCompany> getByCompanyAndTax(UUID companyId,
			UUID taxMasterId) {

		CompanyResponseExternalDto company;
		try {
			company = orgServiceClient.getCompanyBasic(companyId).getBody().getData();
		} catch (FeignException e) {
			throw new OrgServiceException("Company not found", HttpStatus.NOT_FOUND, e);
		}

		TaxMaster taxMaster = taxMasterRepository.findByIdAndDeletedAtIsNull(taxMasterId)
				.orElseThrow(() -> new ResourceNotFoundException("Tax master not found"));
		LocalDate today = LocalDate.now();

		CompanyTaxCategory entity = companyTaxCategoryRepository.findCurrentTaxCategory(companyId, taxMasterId, today)
				.orElseThrow(() -> new ResourceNotFoundException("No active current tax category found"));

		CompanyTaxCategoryResponseWithTaxMasterAndCompany response = new CompanyTaxCategoryResponseWithTaxMasterAndCompany();
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
	public ApiResponse<CompanyTaxMasterCompanyTaxCategoryHistory> getHistoryByCompanyAndTax(UUID companyId,
			UUID taxMasterId) {

		CompanyResponseExternalDto company;
		try {
			company = orgServiceClient.getCompanyBasic(companyId).getBody().getData();
		} catch (FeignException e) {
			throw new OrgServiceException("Company not found", HttpStatus.NOT_FOUND, e);
		}

		TaxMaster taxMaster = taxMasterRepository.findByIdAndDeletedAtIsNull(taxMasterId)
				.orElseThrow(() -> new ResourceNotFoundException("Tax master not found"));

		LocalDate today = LocalDate.now();

		List<CompanyTaxCategory> historyList = companyTaxCategoryRepository.findHistoryByCompanyAndTax(companyId,
				taxMasterId, today);

		CompanyTaxMasterCompanyTaxCategoryHistory response = new CompanyTaxMasterCompanyTaxCategoryHistory();

		CompanyResponseInternalDto crid = new CompanyResponseInternalDto();
		crid.setCompanyId(company.getCompanyId());
		crid.setCompanyCode(company.getCompanyCode());
		crid.setCompanyName(company.getCompanyName());
		crid.setActive(company.getActive());
		response.setCompany(crid);

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

		List<CompanyTaxCategoryResponse> ctrList = new ArrayList<>();
		for (CompanyTaxCategory entity : historyList) {
			CompanyTaxCategoryResponse ctr = new CompanyTaxCategoryResponse();
			ctr.setCompanyTaxCategoryId(entity.getId());
			ctr.setTaxRate(entity.getTaxRate());
			ctr.setHsnCode(entity.getHsnCode());
			ctr.setEffectiveFrom(entity.getEffectiveFrom());
			ctr.setEffectiveTo(entity.getEffectiveTo());
			ctr.setActive(entity.getActive());
			ctr.setCreatedAt(entity.getCreatedAt());
			ctr.setUpdatedAt(entity.getUpdatedAt());

			ctrList.add(ctr);
		}

		response.setCompanyTaxCategoryHistory(ctrList);

		String message = historyList.isEmpty() ? "No historical tax categories"
				: "Historical tax categories fetched successfully";

		return new ApiResponse<>(true, message, HttpStatus.OK.name(), HttpStatus.OK.value(), response);

	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<CompanyCompanyTaxCategoryCurrentResponse> getAllCurrentByCompany(UUID companyId) {

		CompanyResponseExternalDto company;
		try {
			company = orgServiceClient.getCompanyBasic(companyId).getBody().getData();
		} catch (FeignException e) {
			throw new OrgServiceException("Company not found", HttpStatus.NOT_FOUND, e);
		}

		LocalDate today = LocalDate.now();

		List<CompanyTaxCategory> categories = companyTaxCategoryRepository.findAllCurrentByCompanyId(companyId, today);

		Set<UUID> taxMasterIds = categories.stream().map(CompanyTaxCategory::getTaxMasterId)
				.collect(Collectors.toSet());

		List<TaxMaster> taxMasters = taxMasterRepository.findByIdInAndDeletedAtIsNull(taxMasterIds);

		Map<UUID, TaxMaster> taxMasterMap = taxMasters.stream().collect(Collectors.toMap(TaxMaster::getId, t -> t));

		List<TaxMasterWithCompanyTaxCategoryCurrentResponse> responseList = new ArrayList<>();
		for (CompanyTaxCategory companyTaxCategory : categories) {

			TaxMaster taxMaster = taxMasterMap.get(companyTaxCategory.getTaxMasterId());

			TaxMasterWithCompanyTaxCategoryCurrentResponse twccr = new TaxMasterWithCompanyTaxCategoryCurrentResponse();
			twccr.setTaxMasterId(taxMaster.getId());
			twccr.setTaxCode(taxMaster.getTaxCode());
			twccr.setTaxName(taxMaster.getTaxName());
			twccr.setTaxType(taxMaster.getTaxType());
			twccr.setCompoundTax(taxMaster.getCompoundTax());
			twccr.setDescription(taxMaster.getDescription());
			twccr.setActive(taxMaster.getActive());
			twccr.setCreatedAt(taxMaster.getCreatedAt());
			twccr.setUpdatedAt(taxMaster.getUpdatedAt());

			CompanyTaxCategoryResponse ctr = new CompanyTaxCategoryResponse();
			ctr.setCompanyTaxCategoryId(companyTaxCategory.getId());
			ctr.setTaxRate(companyTaxCategory.getTaxRate());
			ctr.setHsnCode(companyTaxCategory.getHsnCode());
			ctr.setEffectiveFrom(companyTaxCategory.getEffectiveFrom());
			ctr.setEffectiveTo(companyTaxCategory.getEffectiveTo());
			ctr.setActive(companyTaxCategory.getActive());
			ctr.setCreatedAt(companyTaxCategory.getCreatedAt());
			ctr.setUpdatedAt(companyTaxCategory.getUpdatedAt());

			twccr.setCompanyTaxCategory(ctr);

			responseList.add(twccr);
		}

		CompanyResponseInternalDto crid = new CompanyResponseInternalDto();
		crid.setCompanyId(company.getCompanyId());
		crid.setCompanyCode(company.getCompanyCode());
		crid.setCompanyName(company.getCompanyName());
		crid.setActive(company.getActive());

		CompanyCompanyTaxCategoryCurrentResponse cccr = new CompanyCompanyTaxCategoryCurrentResponse();
		cccr.setCompany(crid);
		cccr.setTaxMaster(responseList);

		String message = categories.isEmpty() ? "No current tax categories"
				: "Current tax categories fetched successfully";

		return new ApiResponse<>(true, message, HttpStatus.OK.name(), HttpStatus.OK.value(), cccr);

	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<CompanyCompanyTaxCategoryHistoryResponse> getHistoryByCompany(UUID companyId) {

		CompanyResponseExternalDto company;
		try {
			company = orgServiceClient.getCompanyBasic(companyId).getBody().getData();
		} catch (FeignException e) {
			throw new OrgServiceException("Company not found", HttpStatus.NOT_FOUND, e);
		}

		LocalDate today = LocalDate.now();

		List<CompanyTaxCategory> categories = companyTaxCategoryRepository.findHistoryByCompanyId(companyId, today);

		Set<UUID> taxMasterIds = categories.stream().map(CompanyTaxCategory::getTaxMasterId)
				.collect(Collectors.toSet());

		List<TaxMaster> taxMasters = taxMasterRepository.findByIdInAndDeletedAtIsNull(taxMasterIds);

		Map<UUID, TaxMaster> taxMasterMap = taxMasters.stream().collect(Collectors.toMap(TaxMaster::getId, t -> t));

		Map<UUID, List<CompanyTaxCategory>> grouped = categories.stream()
				.collect(Collectors.groupingBy(CompanyTaxCategory::getTaxMasterId));

		List<TaxMasterWithCompanyTaxCategoryHistoryResponse> responseList = new ArrayList<>();
		for (UUID taxMasterId : grouped.keySet()) {
			TaxMaster taxMaster = taxMasterMap.get(taxMasterId);

			TaxMasterWithCompanyTaxCategoryHistoryResponse tmh = new TaxMasterWithCompanyTaxCategoryHistoryResponse();
			tmh.setTaxMasterId(taxMaster.getId());
			tmh.setTaxCode(taxMaster.getTaxCode());
			tmh.setTaxName(taxMaster.getTaxName());
			tmh.setTaxType(taxMaster.getTaxType());
			tmh.setCompoundTax(taxMaster.getCompoundTax());
			tmh.setDescription(taxMaster.getDescription());
			tmh.setCreatedAt(taxMaster.getCreatedAt());
			tmh.setUpdatedAt(taxMaster.getUpdatedAt());

			tmh.setCompanyTaxCategories(
					grouped.get(taxMasterId).stream().map(this::mapToResponse).collect(Collectors.toList()));

			responseList.add(tmh);
		}

		CompanyResponseInternalDto crid = new CompanyResponseInternalDto();
		crid.setCompanyId(company.getCompanyId());
		crid.setCompanyCode(company.getCompanyCode());
		crid.setCompanyName(company.getCompanyName());
		crid.setActive(company.getActive());

		CompanyCompanyTaxCategoryHistoryResponse historyResponse = new CompanyCompanyTaxCategoryHistoryResponse();
		historyResponse.setCompany(crid);
		historyResponse.setTaxMaster(responseList);

		String message = categories.isEmpty() ? "No historical tax categories"
				: "Historical tax categories fetched successfully";

		return new ApiResponse<>(true, message, HttpStatus.OK.name(), HttpStatus.OK.value(), historyResponse);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<PagedResponse<CompanyWithTaxCategoriesResponse>> getAllTaxCategories(UUID companyId,
			Boolean active, int page, int size, String sort) {

		String[] sortParams = sort.split(",");
		Pageable pageable = PageRequest.of(page, size,
				Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]));

		Page<CompanyTaxCategory> pageData;
		if (companyId != null) {
			pageData = (active == null)
					? companyTaxCategoryRepository.findByCompanyIdAndDeletedAtIsNull(companyId, pageable)
					: companyTaxCategoryRepository.findByCompanyIdAndActiveAndDeletedAtIsNull(companyId, active,
							pageable);
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

		List<CompanyTaxCategoryResponseWithTaxMaster> dtoList = categories.stream().map(c -> {
			TaxMaster taxMaster = taxMasterMap.get(c.getTaxMasterId());
			if (taxMaster == null)
				throw new ResourceNotFoundException("Tax master not found: " + c.getTaxMasterId());
			return toResponse(c, taxMaster);
		}).toList();

		Map<UUID, List<CompanyTaxCategoryResponseWithTaxMaster>> groupedByCompany = dtoList.stream()
				.collect(Collectors.groupingBy(c -> {
					return categories.stream().filter(cat -> cat.getId().equals(c.getCompanyTaxCategoryId()))
							.findFirst().get().getCompanyId();
				}));

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

	private CompanyTaxCategoryResponseWithTaxMaster toResponse(CompanyTaxCategory saved, TaxMaster taxMaster) {

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

		CompanyTaxCategoryResponseWithTaxMaster ctcr = new CompanyTaxCategoryResponseWithTaxMaster();
		ctcr.setCompanyTaxCategoryId(saved.getId());
		ctcr.setTaxRate(saved.getTaxRate());
		ctcr.setHsnCode(saved.getHsnCode());
		ctcr.setEffectiveFrom(saved.getEffectiveFrom());
		ctcr.setEffectiveTo(saved.getEffectiveTo());
		ctcr.setActive(saved.getActive());
		ctcr.setCreatedAt(saved.getCreatedAt());
		ctcr.setUpdatedAt(saved.getUpdatedAt());

		ctcr.setTaxMaster(tmr);

		return ctcr;
	}

	// ---------------------------------------------
	private CompanyTaxCategoryResponseWithTaxMasterAndCompany toResponse(CompanyTaxCategory saved, TaxMaster taxMaster,
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

		CompanyTaxCategoryResponseWithTaxMasterAndCompany ctcr = new CompanyTaxCategoryResponseWithTaxMasterAndCompany();
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

	private CompanyTaxCategoryResponse mapToResponse(CompanyTaxCategory entity) {
		CompanyTaxCategoryResponse response = new CompanyTaxCategoryResponse();
		response.setCompanyTaxCategoryId(entity.getId());
		response.setTaxRate(entity.getTaxRate());
		response.setHsnCode(entity.getHsnCode());
		response.setEffectiveFrom(entity.getEffectiveFrom());
		response.setEffectiveTo(entity.getEffectiveTo());
		response.setActive(entity.getActive());
		response.setCreatedAt(entity.getCreatedAt());
		response.setUpdatedAt(entity.getUpdatedAt());
		return response;
	}

}
