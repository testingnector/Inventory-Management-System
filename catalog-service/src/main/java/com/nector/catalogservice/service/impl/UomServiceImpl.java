package com.nector.catalogservice.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nector.catalogservice.dto.request.internal.UomCreateRequest;
import com.nector.catalogservice.dto.request.internal.UomUpdateRequest;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.BaseUomDetails;
import com.nector.catalogservice.dto.response.internal.UomResponse;
import com.nector.catalogservice.entity.Uom;
import com.nector.catalogservice.exception.DuplicateResourceException;
import com.nector.catalogservice.exception.InactiveResourceException;
import com.nector.catalogservice.exception.ResourceNotFoundException;
import com.nector.catalogservice.exception.ResponseStatusException;
import com.nector.catalogservice.repository.UomRepository;
import com.nector.catalogservice.service.UomService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UomServiceImpl implements UomService {

	private final UomRepository uomRepository;

	@Transactional
	@Override
	public ApiResponse<UomResponse> createUom(@Valid UomCreateRequest request, UUID createdBy) {

		if (uomRepository.existsByUomCode(request.getUomCode())) {
			throw new DuplicateResourceException("UOM code already exists");
		}

		Uom uom = new Uom();
		uom.setUomCode(request.getUomCode());
		uom.setUomName(request.getUomName());
		uom.setUomType(request.getUomType());
		uom.setCreatedBy(createdBy);

		if (request.getBaseUomId() != null) {
			Uom baseUom = uomRepository.findByIdAndDeletedAtIsNull(request.getBaseUomId())
					.orElseThrow(() -> new ResourceNotFoundException("Base UOM not found"));

			if (!baseUom.getUomType().equals(request.getUomType())) {
				throw new ResponseStatusException("Base UOM type must match UOM type");
			}

			uom.setBaseUomId(baseUom.getId());
		}

		Uom saved = uomRepository.save(uom);

		UomResponse response = mapToResponse(saved);

		return new ApiResponse<>(true, "UOM created successfully", HttpStatus.CREATED.name(),
				HttpStatus.CREATED.value(), response);
	}

	@Transactional
	@Override
	public ApiResponse<UomResponse> updateUom(UUID uomId, @Valid UomUpdateRequest request, UUID updatedBy) {

	    Uom uom = uomRepository.findByIdAndDeletedAtIsNull(uomId)
	            .orElseThrow(() -> new ResourceNotFoundException("Resource not found: UOM"));

	    if (request.getBaseUomId() != null && request.getBaseUomId().equals(uomId)) {
	        throw new ResponseStatusException("Bad request: UOM cannot be its own base");
	    }

	    if (request.getBaseUomId() != null) {
	        Uom baseUom = uomRepository.findByIdAndDeletedAtIsNull(request.getBaseUomId())
	                .orElseThrow(() -> new ResourceNotFoundException("Resource not found: Base UOM"));

	        if (uom.getUomType() != null && request.getUomType() != null && !baseUom.getUomType().equals(request.getUomType())) {
	            throw new ResponseStatusException("Bad request: Base UOM type must match updated UOM type");
	        }

	        UUID currentBaseId = baseUom.getBaseUomId();
	        while (currentBaseId != null) {
	            if (currentBaseId.equals(uomId)) {
	                throw new ResponseStatusException("Bad request: Circular reference detected in base UOM chain");
	            }
	            currentBaseId = uomRepository.findByIdAndDeletedAtIsNull(currentBaseId)
	                    .map(Uom::getBaseUomId)
	                    .orElse(null);
	        }

	        uom.setBaseUomId(baseUom.getId());
	    }

	    if (request.getUomName() != null) {
	        uom.setUomName(request.getUomName());
	    }

	    if (request.getUomType() != null) {
	        List<Uom> derivedUoms = uomRepository.findByBaseUomIdAndDeletedAtIsNullAndActiveTrue(uomId);
	        for (Uom du : derivedUoms) {
	            if (!du.getUomType().equals(request.getUomType())) {
	                throw new ResponseStatusException(
	                        "Bad request: Cannot change UOM type because derived UOM '" + du.getUomCode() + "' has different type");
	            }
	        }
	        uom.setUomType(request.getUomType());
	    }

	    if (request.getActive() != null) {
	        uom.setActive(request.getActive());
	    }

	    uom.setUpdatedBy(updatedBy);

	    Uom saved = uomRepository.save(uom);

	    return new ApiResponse<>(true, "UOM updated successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
	            mapToResponse(saved));
	}


	@Transactional
	@Override
	public ApiResponse<List<Object>> deleteUom(UUID uomId, UUID deletedBy) {
		Uom uom = uomRepository.findByIdAndDeletedAtIsNull(uomId)
				.orElseThrow(() -> new ResourceNotFoundException("UOM not found"));

		uom.setDeletedAt(LocalDateTime.now());
		uom.setDeletedBy(deletedBy);
		uom.setActive(false);

		uomRepository.save(uom);

		return new ApiResponse<>(true, "UOM deleted successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				Collections.emptyList());
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<UomResponse> getUomById(UUID uomId) {

		Uom uom = uomRepository.findByIdAndDeletedAtIsNull(uomId)
				.orElseThrow(() -> new ResourceNotFoundException("UOM not found"));

		if (!uom.getActive()) {
			throw new InactiveResourceException("The UOM is inactive");
		}
		UomResponse response = mapToResponse(uom);

		return new ApiResponse<>(true, "UOM fetched successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				response);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<UomResponse> getUomByCode(String uomCode) {

		Uom uom = uomRepository.findByUomCodeAndDeletedAtIsNull(uomCode)
				.orElseThrow(() -> new ResourceNotFoundException("UOM not found"));

		if (!uom.getActive()) {
			throw new InactiveResourceException("The UOM is inactive");
		}
		UomResponse response = mapToResponse(uom);

		return new ApiResponse<>(true, "UOM fetched successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				response);

	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<List<UomResponse>> getAllUomWithStatus(boolean activeStatus) {

		List<Uom> uoms = uomRepository.findByActiveAndDeletedAtIsNull(activeStatus);

		if (uoms.isEmpty()) {
			if (activeStatus) {
				throw new ResourceNotFoundException("Active UOMs not found");
			} else {
				throw new ResourceNotFoundException("Inactive UOMs not found");
			}
		}

		List<UomResponse> uomResponses = new ArrayList<>();
		for (Uom uom : uoms) {

			UomResponse response = mapToResponse(uom);
			uomResponses.add(response);
		}

		String message = activeStatus ? "Active UOMs fetched successfully" : "Inactive UOMs fetched successfully";

		return new ApiResponse<>(true, message, HttpStatus.OK.name(), HttpStatus.OK.value(), uomResponses);

	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<List<UomResponse>> getAllUoms() {

		List<Uom> uoms = uomRepository.findByDeletedAtIsNull();

		if (uoms.isEmpty()) {
			throw new ResourceNotFoundException("No UOMs found");
		}

		List<UomResponse> responses = new ArrayList<>();

		for (Uom uom : uoms) {
			responses.add(mapToResponse(uom));
		}

		return new ApiResponse<>(true, "All UOMs fetched successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				responses);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<List<UomResponse>> getBaseUoms() {

		List<Uom> baseUoms = uomRepository.findByBaseUomIdIsNullAndDeletedAtIsNullAndActiveTrue();

		if (baseUoms.isEmpty()) {
			throw new ResourceNotFoundException("Base UOMs not found or inactive");
		}

		List<UomResponse> responses = new ArrayList<>();
		for (Uom uom : baseUoms) {
			responses.add(mapToResponse(uom));
		}

		return new ApiResponse<>(true, "Base UOMs fetched successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				responses);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<List<UomResponse>> getDerivedUomsByBaseUomId(UUID baseUomId) {

		Uom baseUom = uomRepository.findByIdAndDeletedAtIsNull(baseUomId)
				.orElseThrow(() -> new ResourceNotFoundException("Base UOM not found"));

		if (!baseUom.getActive()) {
			throw new InactiveResourceException("Base UOM is inactive");
		}

		List<Uom> derivedUoms = uomRepository.findByBaseUomIdAndDeletedAtIsNullAndActiveTrue(baseUomId);

		if (derivedUoms.isEmpty()) {
			throw new ResourceNotFoundException("No derived UOMs found for this base UOM");
		}

		List<UomResponse> responses = new ArrayList<>();
		for (Uom uom : derivedUoms) {
			responses.add(mapToResponse(uom));
		}

		return new ApiResponse<>(true, "Derived UOMs fetched successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				responses);
	}
	
	

//	---------------------------HELPER METHOD------------------------
	private UomResponse mapToResponse(Uom uom) {
		UomResponse response = new UomResponse();
		response.setUomId(uom.getId());
		response.setUomCode(uom.getUomCode());
		response.setUomName(uom.getUomName());
		response.setUomType(uom.getUomType());
		response.setActive(uom.getActive());
		response.setCreatedAt(uom.getCreatedAt());
		response.setUpdatedAt(uom.getUpdatedAt());

		if (uom.getBaseUomId() != null) {
			uomRepository.findByIdAndDeletedAtIsNull(uom.getBaseUomId()).ifPresent(base -> {
				BaseUomDetails baseDetails = new BaseUomDetails();
				baseDetails.setBaseUomId(base.getId());
				baseDetails.setUomCode(base.getUomCode());
				baseDetails.setUomName(base.getUomName());
				baseDetails.setUomType(base.getUomType());
				baseDetails.setActive(base.getActive());
				response.setBaseUomDetails(baseDetails);
			});
		}

		return response;
	}

}
