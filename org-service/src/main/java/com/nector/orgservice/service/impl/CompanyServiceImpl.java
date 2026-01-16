package com.nector.orgservice.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.nector.orgservice.dto.request.CompanyCreateRequest;
import com.nector.orgservice.dto.request.CompanyUpdateRequest;
import com.nector.orgservice.dto.response.ApiResponse;
import com.nector.orgservice.dto.response.CompanyResponse;
import com.nector.orgservice.entity.Company;
import com.nector.orgservice.repository.CompanyRepository;
import com.nector.orgservice.service.CompanyService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyServiceImpl implements CompanyService {

	private final CompanyRepository companyRepository;

	@Transactional
	@Override
	public ApiResponse<CompanyResponse> createCompany(CompanyCreateRequest request, UUID createdBy) {
		// Duplicate checks
		if (companyRepository.existsByCompanyCode(request.getCompanyCode())) {
			throw new IllegalArgumentException("Company code already exists");
		}
		if (companyRepository.existsByPanNumber(request.getPanNumber())) {
			throw new IllegalArgumentException("PAN number already exists");
		}
		if (request.getGstNumber() != null && companyRepository.existsByGstNumber(request.getGstNumber())) {
			throw new IllegalArgumentException("GST number already exists");
		}

		Company company = new Company();
		company.setCompanyCode(request.getCompanyCode());
		company.setCompanyName(request.getCompanyName());
		company.setLegalName(request.getLegalName());
		company.setCompanyType(request.getCompanyType());
		company.setPanNumber(request.getPanNumber());
		company.setGstNumber(request.getGstNumber());
		company.setEmail(request.getEmail());
		company.setPhone(request.getPhone());
		company.setAddress(request.getAddress());
		company.setState(request.getState());
		company.setCity(request.getCity());
		company.setPincode(request.getPincode());
		company.setCountry(request.getCountry());
		company.setCreatedBy(createdBy);

		Company saveCompany = companyRepository.save(company);

		return new ApiResponse<>(true, "Company create successfully...", HttpStatus.CREATED.name(), HttpStatus.CREATED.value(),
				toResponse(saveCompany));

	}

	@Transactional
	@Override
	public ApiResponse<CompanyResponse> updateCompany(UUID companyId, CompanyUpdateRequest request, UUID updatedBy) {
		Company company = companyRepository.findById(companyId)
				.orElseThrow(() -> new IllegalArgumentException("Company not found"));

		if (request.getCompanyName() != null) {
			company.setCompanyName(request.getCompanyName());
		}

		if (request.getLegalName() != null) {
			company.setLegalName(request.getLegalName());
		}

		if (request.getCompanyType() != null) {
			company.setCompanyType(request.getCompanyType());
		}

		if (request.getPanNumber() != null) {
			company.setPanNumber(request.getPanNumber());
		}

		if (request.getGstNumber() != null) {
			company.setGstNumber(request.getGstNumber());
		}

		if (request.getEmail() != null) {
			company.setEmail(request.getEmail());
		}

		if (request.getPhone() != null) {
			company.setPhone(request.getPhone());
		}

		if (request.getAddress() != null) {
			company.setAddress(request.getAddress());
		}

		if (request.getState() != null) {
			company.setState(request.getState());
		}

		if (request.getCity() != null) {
			company.setCity(request.getCity());
		}

		if (request.getPincode() != null) {
			company.setPincode(request.getPincode());
		}

		if (request.getCountry() != null) {
			company.setCountry(request.getCountry());
		}

		// Always set the updater
		company.setUpdatedBy(updatedBy);

		Company updatedCompany = companyRepository.save(company);
		return new ApiResponse<>(true, "Company code " + updatedCompany.getCompanyCode() + " updated successfully...",
				HttpStatus.CREATED.name(), HttpStatus.CREATED.value(), toResponse(updatedCompany));
	}

	@Transactional
	@Override
	public ApiResponse<List<Object>> deleteCompany(UUID companyId) {
		Company company = companyRepository.findById(companyId)
				.orElseThrow(() -> new IllegalArgumentException("Company not found"));
		company.setActive(false);
		company.setDeletedAt(LocalDateTime.now());
		companyRepository.save(company);
		return new ApiResponse<>(true, "Company deleted successfully...", HttpStatus.OK.name(), HttpStatus.OK.value(), Collections.emptyList());
	}

	@Override
	public ApiResponse<List<CompanyResponse>> getAllCompanies() {
		
		List<Company> companies = companyRepository.findAll();
		if (companies.isEmpty()) {
			return new ApiResponse<>(true, "Companies not exists!", HttpStatus.OK.name(), HttpStatus.OK.value(), toResponseList(companies));
		}
		return new ApiResponse<>(true, "Companies fetched successfully...", HttpStatus.OK.name(), HttpStatus.OK.value(), toResponseList(companies));
	}

	@Override
	public ApiResponse<CompanyResponse> getCompanyById(UUID companyId) {
		Company company = companyRepository.findById(companyId)
				.orElseThrow(() -> new IllegalArgumentException("Company not found"));
		return new ApiResponse<>(true, "Company data fetch successfully...", HttpStatus.OK.name(), HttpStatus.OK.value(), toResponse(company));
	}
	
	@Override
	public ApiResponse<Boolean> existsCompanyById(UUID companyId) {
		
		Boolean status = companyRepository.existsById(companyId);
		if (status) {
			return new ApiResponse<>(true, "Company is exists", HttpStatus.OK.name(), HttpStatus.OK.value(), status);
		}
		else {
			return new ApiResponse<>(true, "Company is not exists", HttpStatus.NOT_FOUND.name(), HttpStatus.NOT_FOUND.value(), status);
		}
	}
	
//	------------------------------------------------------------------------------------

	private CompanyResponse toResponse(Company company) {

		CompanyResponse companyResponse = new CompanyResponse();

		companyResponse.setId(company.getId());
		companyResponse.setCompanyCode(company.getCompanyCode());
		companyResponse.setCompanyName(company.getCompanyName());
		companyResponse.setLegalName(company.getLegalName());
		companyResponse.setCompanyType(company.getCompanyType());
		companyResponse.setGstNumber(company.getGstNumber());
		companyResponse.setPanNumber(company.getPanNumber());
		companyResponse.setEmail(company.getEmail());
		companyResponse.setPhone(company.getPhone());
		companyResponse.setAddress(company.getAddress());
		companyResponse.setCountry(company.getCountry());
		companyResponse.setState(company.getState());
		companyResponse.setCity(company.getCity());
		companyResponse.setPincode(company.getPincode());
		companyResponse.setIsActive(company.getActive());

		return companyResponse;
	}

	private List<CompanyResponse> toResponseList(List<Company> companies) {
		
		List<CompanyResponse> companyListResponse = new ArrayList<>();
		
		for (Company company : companies) {
			
			CompanyResponse companyResponse = new CompanyResponse();

			companyResponse.setId(company.getId());
			companyResponse.setCompanyCode(company.getCompanyCode());
			companyResponse.setCompanyName(company.getCompanyName());
			companyResponse.setLegalName(company.getLegalName());
			companyResponse.setCompanyType(company.getCompanyType());
			companyResponse.setGstNumber(company.getGstNumber());
			companyResponse.setPanNumber(company.getPanNumber());
			companyResponse.setEmail(company.getEmail());
			companyResponse.setPhone(company.getPhone());
			companyResponse.setAddress(company.getAddress());
			companyResponse.setCountry(company.getCountry());
			companyResponse.setState(company.getState());
			companyResponse.setCity(company.getCity());
			companyResponse.setPincode(company.getPincode());
			companyResponse.setIsActive(company.getActive());
			
			companyListResponse.add(companyResponse);
		}
		
		return companyListResponse;
	}

}
