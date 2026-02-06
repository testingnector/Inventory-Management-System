package com.nector.catalogservice.service.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nector.catalogservice.dto.request.internal.TaxMasterCreateRequest;
import com.nector.catalogservice.dto.request.internal.TaxMasterUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.PagedResponse;
import com.nector.catalogservice.dto.response.internal.TaxMasterResponse;
import com.nector.catalogservice.entity.TaxMaster;
import com.nector.catalogservice.exception.DuplicateResourceException;
import com.nector.catalogservice.exception.InactiveResourceException;
import com.nector.catalogservice.exception.ResourceNotFoundException;
import com.nector.catalogservice.repository.TaxMasterRepository;
import com.nector.catalogservice.service.TaxMasterService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaxMasterServiceImpl implements TaxMasterService {

	private final TaxMasterRepository taxMasterRepository;

	@Transactional
	@Override
	public ApiResponse<TaxMasterResponse> createTaxMaster(@Valid TaxMasterCreateRequest request, UUID createdBy) {
		if (taxMasterRepository.existsByTaxCode(request.getTaxCode())) {
			throw new DuplicateResourceException("Tax code already exists: " + request.getTaxCode());
		}

		TaxMaster taxMaster = new TaxMaster();
		taxMaster.setTaxCode(request.getTaxCode());
		taxMaster.setTaxName(request.getTaxName());
		taxMaster.setTaxType(request.getTaxType());
		taxMaster.setCompoundTax(request.getCompoundTax() != null ? request.getCompoundTax() : false);
		taxMaster.setDescription(request.getDescription());
		taxMaster.setCreatedBy(createdBy);

		TaxMaster savedTaxMaster = taxMasterRepository.save(taxMaster);

		return new ApiResponse<>(true, "Tax Master data created successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), toResponse(savedTaxMaster));
	}

	@Transactional
	@Override
	public ApiResponse<TaxMasterResponse> updateTaxMaster(UUID id, @Valid TaxMasterUpdateRequest request,
			UUID updatedBy) {
		TaxMaster taxMaster = taxMasterRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new ResourceNotFoundException("Tax not found or deleted"));

		if (request.getTaxName() != null)
			taxMaster.setTaxName(request.getTaxName());
		if (request.getTaxType() != null)
			taxMaster.setTaxType(request.getTaxType());
		if (request.getDescription() != null)
			taxMaster.setDescription(request.getDescription());
		taxMaster.setCompoundTax(request.getCompoundTax() != null ? request.getCompoundTax() : false);

		if (request.getActive() != null)
			taxMaster.setActive(request.getActive());

		taxMaster.setUpdatedAt(LocalDateTime.now());
		taxMaster.setUpdatedBy(updatedBy);

		TaxMaster updated = taxMasterRepository.save(taxMaster);
		return new ApiResponse<>(true, "Tax updated successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				toResponse(updated));
	}

	@Transactional
	@Override
	public ApiResponse<List<Object>> deleteTaxMaster(UUID id, UUID deletedBy) {

		TaxMaster taxMaster = taxMasterRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new ResourceNotFoundException("Tax not found or already deleted"));

		taxMaster.setDeletedAt(LocalDateTime.now());
		taxMaster.setDeletedBy(deletedBy);
		taxMaster.setActive(false);

		taxMasterRepository.save(taxMaster);

		return new ApiResponse<>(true, "Tax deleted successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				Collections.emptyList());

	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<TaxMasterResponse> getTaxMasterById(UUID id) {
		TaxMaster taxMaster = taxMasterRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new ResourceNotFoundException("Tax not found or deleted"));

		if (!taxMaster.getActive()) {
			throw new InactiveResourceException("Tax is inactive");
		}

		return new ApiResponse<>(true, "Tax data fetch successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				toResponse(taxMaster));
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<TaxMasterResponse> getTaxMasterByTaxCode(String code) {
		TaxMaster taxMaster = taxMasterRepository.findByTaxCodeAndDeletedAtIsNull(code)
				.orElseThrow(() -> new ResourceNotFoundException("Tax not found or deleted"));

		if (!taxMaster.getActive()) {
			throw new InactiveResourceException("Tax is inactive");
		}

		return new ApiResponse<>(true, "Tax data fetch successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				toResponse(taxMaster));
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<List<TaxMasterResponse>> getAllTaxMasterWithStatus(boolean activeStatus) {

		List<TaxMaster> taxMasters = taxMasterRepository.findByActiveAndDeletedAtIsNull(activeStatus);

		if (taxMasters.isEmpty()) {
			if (activeStatus) {
				throw new ResourceNotFoundException("Active Tax Master data is not found");
			} else {
				throw new ResourceNotFoundException("Inactive Tax Master data is not found");
			}
		}

		List<TaxMasterResponse> list = taxMasters.stream().map(this::toResponse).collect(Collectors.toList());

		String message = activeStatus ? "Active tax master data found successfully..."
				: "Inactive tax master data found successfully...";

		return new ApiResponse<>(true, message, HttpStatus.OK.name(), HttpStatus.OK.value(), list);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<List<TaxMasterResponse>> getAllTaxMasters() {

		List<TaxMasterResponse> list = taxMasterRepository.findByDeletedAtIsNull().stream().map(this::toResponse)
				.collect(Collectors.toList());
		return new ApiResponse<>(true, "Taxes data fetch successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				list);
	}


	@Transactional(readOnly = true)
	@Override
	public ApiResponse<PagedResponse<TaxMasterResponse>> getTaxMasters(String taxType, Boolean active, int page,
			int size, String sort) {

		String[] sortParams = sort.split(",");
		Pageable pageable = PageRequest.of(page, size,
				Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]));

		Page<TaxMaster> taxPage;

		if (taxType != null && active != null) {
			taxPage = taxMasterRepository.findByTaxTypeAndActiveAndDeletedAtIsNull(taxType, active, pageable);
		} else if (taxType != null) {
			taxPage = taxMasterRepository.findByTaxTypeAndDeletedAtIsNull(taxType, pageable);
		} else if (active != null) {
			taxPage = taxMasterRepository.findByActiveAndDeletedAtIsNull(active, pageable);
		} else {
			taxPage = taxMasterRepository.findByDeletedAtIsNull(pageable);
		}

		List<TaxMasterResponse> content = taxPage.getContent().stream().map(this::toResponse)
				.collect(Collectors.toList());

		PagedResponse<TaxMasterResponse> pagedResponse = new PagedResponse<>();
		pagedResponse.setContent(content);
		pagedResponse.setPageNumber(taxPage.getNumber());
		pagedResponse.setPageSize(taxPage.getSize());
		pagedResponse.setTotalElements(taxPage.getTotalElements());
		pagedResponse.setTotalPages(taxPage.getTotalPages());
		pagedResponse.setLast(taxPage.isLast());

		return new ApiResponse<>(true, "Tax data fetched successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				pagedResponse);
	}

//	-----------------------------
	private TaxMasterResponse toResponse(TaxMaster savedTaxMaster) {
		TaxMasterResponse tmr = new TaxMasterResponse();
		tmr.setTaxMasterId(savedTaxMaster.getId());
		tmr.setTaxCode(savedTaxMaster.getTaxCode());
		tmr.setTaxName(savedTaxMaster.getTaxName());
		tmr.setTaxType(savedTaxMaster.getTaxType());
		tmr.setCompoundTax(savedTaxMaster.getCompoundTax());
		tmr.setDescription(savedTaxMaster.getDescription());
		tmr.setActive(savedTaxMaster.getActive());
		tmr.setCreatedAt(savedTaxMaster.getCreatedAt());
		tmr.setUpdatedAt(savedTaxMaster.getUpdatedAt());
		
		return tmr;
	}
}
