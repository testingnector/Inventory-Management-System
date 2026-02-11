package com.nector.catalogservice.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nector.catalogservice.client.OrgServiceClient;
import com.nector.catalogservice.dto.request.internal.CreateTaxComponentRequest;
import com.nector.catalogservice.dto.request.internal.TaxComponentUpdateRequest;
import com.nector.catalogservice.dto.response.external.CompanyResponseExternalDto;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CompanyResponseInternalDto;
import com.nector.catalogservice.dto.response.internal.CompanyTaxCategoryResponse;
import com.nector.catalogservice.dto.response.internal.CompanyTaxCategoryWithComponentsResponse;
import com.nector.catalogservice.dto.response.internal.PagedResponse;
import com.nector.catalogservice.dto.response.internal.TaxCalculationItem;
import com.nector.catalogservice.dto.response.internal.TaxCalculationResponse;
import com.nector.catalogservice.dto.response.internal.TaxComponentResponse;
import com.nector.catalogservice.dto.response.internal.TaxComponentResponseWithCompanyTaxCategory;
import com.nector.catalogservice.dto.response.internal.TaxMasterResponse;
import com.nector.catalogservice.entity.CompanyTaxCategory;
import com.nector.catalogservice.entity.TaxComponent;
import com.nector.catalogservice.entity.TaxMaster;
import com.nector.catalogservice.exception.DuplicateResourceException;
import com.nector.catalogservice.exception.ExternalServiceException;
import com.nector.catalogservice.exception.InactiveResourceException;
import com.nector.catalogservice.exception.ResourceNotFoundException;
import com.nector.catalogservice.repository.CompanyTaxCategoryRepository;
import com.nector.catalogservice.repository.TaxComponentRepository;
import com.nector.catalogservice.repository.TaxMasterRepository;
import com.nector.catalogservice.service.TaxComponentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaxComponentServiceImpl implements TaxComponentService {

	private final TaxComponentRepository taxComponentRepository;
	private final CompanyTaxCategoryRepository companyTaxCategoryRepository;
	private final TaxMasterRepository taxMasterRepository;
	private final OrgServiceClient orgServiceClient;

	@Transactional
	@Override
	public ApiResponse<TaxComponentResponseWithCompanyTaxCategory> createTaxComponent(CreateTaxComponentRequest request,
			UUID userId) {

		Optional<TaxComponent> taxComponentOpt = taxComponentRepository.findByCompanyTaxCategoryIdAndComponentType(
				request.getCompanyTaxCategoryId(), request.getComponentType());

		if (taxComponentOpt.isPresent()) {
			TaxComponent existing = taxComponentOpt.get();
			if (existing.getActive()) {
				throw new DuplicateResourceException("Tax component already exists for this category");
			} else {
				throw new DuplicateResourceException("Tax component already exists but inactive");
			}
		}

		CompanyTaxCategory companyTaxCategory = companyTaxCategoryRepository
				.findByIdAndDeletedAtIsNull(request.getCompanyTaxCategoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Tax category is not found for company"));

		if (!companyTaxCategory.getActive()) {
			throw new InactiveResourceException("For company tax category is inactive");
		}

		TaxComponent taxComponent = new TaxComponent();
		taxComponent.setCompanyTaxCategoryId(request.getCompanyTaxCategoryId());
		taxComponent.setComponentType(request.getComponentType());
		taxComponent.setComponentRate(request.getComponentRate());
		taxComponent.setCreatedBy(userId);

		TaxComponent savedTaxComponent = taxComponentRepository.save(taxComponent);

		TaxComponentResponseWithCompanyTaxCategory tcr = toResponse(savedTaxComponent, companyTaxCategory);

		return new ApiResponse<>(true, "Tax Component created successfully...", HttpStatus.CREATED.name(),
				HttpStatus.CREATED.value(), tcr);

	}

	private TaxComponentResponseWithCompanyTaxCategory toResponse(TaxComponent savedTaxComponent,
			CompanyTaxCategory companyTaxCategory) {

		TaxComponentResponseWithCompanyTaxCategory tcr = new TaxComponentResponseWithCompanyTaxCategory();
		tcr.setTaxComponentId(savedTaxComponent.getId());
		tcr.setComponentType(savedTaxComponent.getComponentType());
		tcr.setComponentRate(savedTaxComponent.getComponentRate());
		tcr.setActive(savedTaxComponent.getActive());
		tcr.setCreatedAt(savedTaxComponent.getCreatedAt());
		tcr.setUpdatedAt(savedTaxComponent.getUpdatedAt());

		CompanyTaxCategoryResponse cr = new CompanyTaxCategoryResponse();
		cr.setCompanyTaxCategoryId(companyTaxCategory.getId());
		cr.setTaxRate(companyTaxCategory.getTaxRate());
		cr.setHsnCode(companyTaxCategory.getHsnCode());
		cr.setEffectiveFrom(companyTaxCategory.getEffectiveFrom());
		cr.setEffectiveTo(companyTaxCategory.getEffectiveTo());
		cr.setActive(companyTaxCategory.getActive());
		cr.setCreatedAt(companyTaxCategory.getCreatedAt());
		cr.setUpdatedAt(companyTaxCategory.getUpdatedAt());

		tcr.setCompanyTaxCategory(cr);

		return tcr;
	}

	@Override
	@Transactional
	public ApiResponse<TaxComponentResponseWithCompanyTaxCategory> updateTaxComponent(UUID taxComponentId,
			TaxComponentUpdateRequest request, UUID userId) {

		TaxComponent component = taxComponentRepository.findByIdAndDeletedAtIsNull(taxComponentId)
				.orElseThrow(() -> new ResourceNotFoundException("Tax component not found"));

		if (request.getComponentRate() != null)
			component.setComponentRate(request.getComponentRate());
		if (request.getActive() != null)
			component.setActive(request.getActive());

		component.setUpdatedBy(userId);

		TaxComponent updated = taxComponentRepository.save(component);

		CompanyTaxCategory companyTaxCategory = companyTaxCategoryRepository
				.findByIdAndDeletedAtIsNull(updated.getCompanyTaxCategoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Tax category is not found for company"));

		TaxComponentResponseWithCompanyTaxCategory response = toResponse(updated, companyTaxCategory);

		return new ApiResponse<>(true, "Tax Component updated successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), response);
	}

	@Override
	@Transactional
	public ApiResponse<List<Object>> deleteTaxComponent(UUID taxComponentId, UUID userId) {

		TaxComponent component = taxComponentRepository.findByIdAndDeletedAtIsNull(taxComponentId)
				.orElseThrow(() -> new ResourceNotFoundException("Tax component not found or already deleted"));

		component.setDeletedAt(LocalDateTime.now());
		component.setDeletedBy(userId);
		component.setActive(false);

		taxComponentRepository.save(component);

		return new ApiResponse<>(true, "Tax Component deleted successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), Collections.emptyList());

	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse<TaxComponentResponseWithCompanyTaxCategory> getActiveTaxComponentById(UUID taxComponentId) {

		TaxComponent component = taxComponentRepository.findByIdAndDeletedAtIsNull(taxComponentId)
				.orElseThrow(() -> new ResourceNotFoundException("Tax component not found"));

		CompanyTaxCategory companyTaxCategory = companyTaxCategoryRepository
				.findByIdAndDeletedAtIsNull(component.getCompanyTaxCategoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Tax category is not found for company"));

		TaxComponentResponseWithCompanyTaxCategory response = toResponse(component, companyTaxCategory);

		return new ApiResponse<>(true, "Tax Component fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), response);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<CompanyTaxCategoryWithComponentsResponse> getByCompanyTaxCategoryId(UUID companyTaxCategoryId) {

		CompanyTaxCategory category = companyTaxCategoryRepository.findByIdAndDeletedAtIsNull(companyTaxCategoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Company tax category not found"));

		List<TaxComponent> components = taxComponentRepository
				.findAllByCompanyTaxCategoryIdAndDeletedAtIsNull(companyTaxCategoryId);

		CompanyTaxCategoryWithComponentsResponse response = toWithComponentsResponse(category, components);

		return new ApiResponse<>(true, "Company tax category with tax components fetched successfully",
				HttpStatus.OK.name(), HttpStatus.OK.value(), response);

	}

	private CompanyTaxCategoryWithComponentsResponse toWithComponentsResponse(CompanyTaxCategory category,
			List<TaxComponent> components) {

		CompanyTaxCategoryWithComponentsResponse resp = new CompanyTaxCategoryWithComponentsResponse();
		resp.setCompanyTaxCategoryId(category.getId());
		resp.setTaxRate(category.getTaxRate());
		resp.setHsnCode(category.getHsnCode());
		resp.setEffectiveFrom(category.getEffectiveFrom());
		resp.setEffectiveTo(category.getEffectiveTo());
		resp.setActive(category.getActive());

		TaxMaster taxMaster = taxMasterRepository.findByIdAndDeletedAtIsNull(category.getTaxMasterId())
				.orElseThrow(() -> new ResourceNotFoundException("Tax master data not found or deleted"));

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

		var cResponse = orgServiceClient.getCompanyBasic(category.getCompanyId());

		if (cResponse == null || cResponse.getBody() == null || cResponse.getBody().getData() == null) {
			throw new ExternalServiceException("Invalid response from Organization Service",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		CompanyResponseExternalDto companyResponse = cResponse.getBody().getData();

		CompanyResponseInternalDto crid = new CompanyResponseInternalDto();
		crid.setCompanyId(companyResponse.getCompanyId());
		crid.setCompanyCode(companyResponse.getCompanyCode());
		crid.setCompanyName(companyResponse.getCompanyName());
		crid.setActive(companyResponse.getActive());

		List<TaxComponentResponse> taxComponentResponses = components.stream().map(c -> {
			TaxComponentResponse tcr = new TaxComponentResponse();
			tcr.setTaxComponentId(c.getId());
			tcr.setComponentType(c.getComponentType());
			tcr.setComponentRate(c.getComponentRate());
			tcr.setActive(c.getActive());
			return tcr;

		}).toList();

		resp.setTaxMaster(tmr);
		resp.setCompany(crid);
		resp.setTaxComponents(taxComponentResponses);

		return resp;
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse<List<TaxComponentResponseWithCompanyTaxCategory>> getAllTaxComponentsWithStatus(
			boolean activeStatus) {

		List<TaxComponent> taxComponents = taxComponentRepository.findByActiveAndDeletedAtIsNull(activeStatus);

		if (taxComponents.isEmpty()) {
			if (activeStatus) {
				throw new ResourceNotFoundException("No active tax components found");
			} else {
				throw new ResourceNotFoundException("No inactive tax components found");
			}
		}

		Set<UUID> companyTaxCategoryIds = taxComponents.stream().map(TaxComponent::getCompanyTaxCategoryId)
				.collect(Collectors.toSet());

		List<CompanyTaxCategory> companyTaxCategories = companyTaxCategoryRepository
				.findByIdInAndDeletedAtIsNull(companyTaxCategoryIds);

		Map<UUID, CompanyTaxCategory> companyTaxCategoryMap = companyTaxCategories.stream()
				.collect(Collectors.toMap(CompanyTaxCategory::getId, c -> c));

		List<TaxComponentResponseWithCompanyTaxCategory> finalList = new ArrayList<>();
		for (TaxComponent taxComponent : taxComponents) {

			CompanyTaxCategory companyTaxCategory = companyTaxCategoryMap.get(taxComponent.getCompanyTaxCategoryId());
			CompanyTaxCategoryResponse cr = new CompanyTaxCategoryResponse();
			cr.setCompanyTaxCategoryId(companyTaxCategory.getId());
			cr.setTaxRate(companyTaxCategory.getTaxRate());
			cr.setHsnCode(companyTaxCategory.getHsnCode());
			cr.setEffectiveFrom(companyTaxCategory.getEffectiveFrom());
			cr.setEffectiveTo(companyTaxCategory.getEffectiveTo());
			cr.setActive(companyTaxCategory.getActive());
			cr.setCreatedAt(companyTaxCategory.getCreatedAt());
			cr.setUpdatedAt(companyTaxCategory.getUpdatedAt());

			TaxComponentResponseWithCompanyTaxCategory trwc = new TaxComponentResponseWithCompanyTaxCategory();
			trwc.setTaxComponentId(taxComponent.getId());
			trwc.setComponentType(taxComponent.getComponentType());
			trwc.setComponentRate(taxComponent.getComponentRate());
			trwc.setActive(taxComponent.getActive());
			trwc.setCreatedAt(taxComponent.getCreatedAt());
			trwc.setUpdatedAt(taxComponent.getUpdatedAt());

			trwc.setCompanyTaxCategory(cr);

			finalList.add(trwc);

		}

		String message = activeStatus ? "All active tax components data fetched successfully"
				: "All inactive tax components data fetched successfully";

		return new ApiResponse<>(true, message, HttpStatus.OK.name(), HttpStatus.OK.value(), finalList);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<PagedResponse<TaxComponentResponseWithCompanyTaxCategory>> getAllTaxComponents(int page,
			int size, String sort, Boolean active) {

		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sort).descending());

		Page<TaxComponent> pageResult;

		if (active == null) {
			pageResult = taxComponentRepository.findAllByDeletedAtIsNull(pageRequest);
		} else {
			pageResult = taxComponentRepository.findAllByActiveAndDeletedAtIsNull(active, pageRequest);
		}

		List<TaxComponent> taxComponents = pageResult.getContent();

		Set<UUID> companyTaxCategoryIds = taxComponents.stream().map(TaxComponent::getCompanyTaxCategoryId)
				.collect(Collectors.toSet());

		List<CompanyTaxCategory> companyTaxCategories = companyTaxCategoryRepository
				.findByIdInAndDeletedAtIsNull(companyTaxCategoryIds);

		Map<UUID, CompanyTaxCategory> companyTaxCategoryMap = companyTaxCategories.stream()
				.collect(Collectors.toMap(CompanyTaxCategory::getId, c -> c));

		List<TaxComponentResponseWithCompanyTaxCategory> finalList = new ArrayList<>();
		for (TaxComponent taxComponent : taxComponents) {

			CompanyTaxCategory companyTaxCategory = companyTaxCategoryMap.get(taxComponent.getCompanyTaxCategoryId());
			CompanyTaxCategoryResponse cr = new CompanyTaxCategoryResponse();
			cr.setCompanyTaxCategoryId(companyTaxCategory.getId());
			cr.setTaxRate(companyTaxCategory.getTaxRate());
			cr.setHsnCode(companyTaxCategory.getHsnCode());
			cr.setEffectiveFrom(companyTaxCategory.getEffectiveFrom());
			cr.setEffectiveTo(companyTaxCategory.getEffectiveTo());
			cr.setActive(companyTaxCategory.getActive());
			cr.setCreatedAt(companyTaxCategory.getCreatedAt());
			cr.setUpdatedAt(companyTaxCategory.getUpdatedAt());

			TaxComponentResponseWithCompanyTaxCategory trwc = new TaxComponentResponseWithCompanyTaxCategory();
			trwc.setTaxComponentId(taxComponent.getId());
			trwc.setComponentType(taxComponent.getComponentType());
			trwc.setComponentRate(taxComponent.getComponentRate());
			trwc.setActive(taxComponent.getActive());
			trwc.setCreatedAt(taxComponent.getCreatedAt());
			trwc.setUpdatedAt(taxComponent.getUpdatedAt());

			trwc.setCompanyTaxCategory(cr);

			finalList.add(trwc);

		}

		PagedResponse<TaxComponentResponseWithCompanyTaxCategory> pagedResponse = new PagedResponse<>();
		pagedResponse.setContent(finalList);
		pagedResponse.setPageNumber(pageResult.getNumber());
		pagedResponse.setPageSize(pageResult.getSize());
		pagedResponse.setTotalElements(pageResult.getTotalElements());
		pagedResponse.setTotalPages(pageResult.getTotalPages());
		pagedResponse.setLast(pageResult.isLast());

		return new ApiResponse<>(true, "All tax components data fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), pagedResponse);

	}

	@Transactional
	@Override
	public ApiResponse<TaxCalculationResponse> calculateTax(UUID companyTaxCategoryId, Double baseAmount) {

		CompanyTaxCategory companyTaxCategory = companyTaxCategoryRepository
				.findByIdAndDeletedAtIsNull(companyTaxCategoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Company tax category not found"));

		if (!companyTaxCategory.getActive()) {
			throw new InactiveResourceException("Company tax category is inactive");
		}

		List<TaxComponent> components = taxComponentRepository
				.findAllByCompanyTaxCategoryIdAndActiveTrueAndDeletedAtIsNull(companyTaxCategory.getId());

		if (components.isEmpty()) {
			TaxCalculationResponse taxCalculationResponse = new TaxCalculationResponse(baseAmount, 0.0, baseAmount,
					List.of());
			return new ApiResponse<>(true, "Tax component not found", HttpStatus.NOT_FOUND.name(),
					HttpStatus.NOT_FOUND.value(), taxCalculationResponse);
		}

		double totalTax = 0.0;
		List<TaxCalculationItem> calculationItems = new ArrayList<>();
		for (TaxComponent t : components) {
			Double taxAmount = round((baseAmount * t.getComponentRate()) / 100.0);
			totalTax += taxAmount;
			calculationItems.add(new TaxCalculationItem(t.getComponentType().name(), t.getComponentRate(), taxAmount));
		}

		Double totalAmount = round(baseAmount + totalTax);

		TaxCalculationResponse taxCalculationResponse = new TaxCalculationResponse(baseAmount, totalTax, totalAmount,
				calculationItems);

		return new ApiResponse<>(true, "Tax calculated successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				taxCalculationResponse);

	}

	private Double round(Double value) {
		return Math.round(value * 100.0) / 100.0;
	}

}
