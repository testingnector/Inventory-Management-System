package com.nector.orgservice.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.nector.orgservice.dto.request.internal.BranchCreateRequestDto;
import com.nector.orgservice.dto.response.internal.ApiResponse;
import com.nector.orgservice.dto.response.internal.BranchCompanyResponseDto1;
import com.nector.orgservice.dto.response.internal.BranchCompanyResponseDto2;
import com.nector.orgservice.dto.response.internal.CompanyBranchResponseDto1;
import com.nector.orgservice.dto.response.internal.CompanyBranchResponseDto2;
import com.nector.orgservice.dto.response.internal.CompanyBranchesResponseDto1;
import com.nector.orgservice.dto.response.internal.CompanyBranchesResponseDto2;
import com.nector.orgservice.entity.Branch;
import com.nector.orgservice.entity.Company;
import com.nector.orgservice.exception.DuplicateResourceException;
import com.nector.orgservice.exception.ResourceNotFoundException;
import com.nector.orgservice.repository.BranchRepository;
import com.nector.orgservice.repository.CompanyRepository;
import com.nector.orgservice.service.BranchService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

	private final BranchRepository branchRepository;
	private final CompanyRepository companyRepository;

	@Override
	public ApiResponse<CompanyBranchResponseDto1> createBranch(BranchCreateRequestDto dto, UUID createdBy) {

		// CHECK DUPLICATE
		if (branchRepository.existsByBranchCode(dto.getBranchCode())) {
			throw new DuplicateResourceException("Branch code '" + dto.getBranchCode() + "' already exists");
		}

		Company company = companyRepository.findByIdAndDeletedAtIsNullAndActiveTrue(dto.getCompanyId())
				.orElseThrow(() -> new ResourceNotFoundException("Company does not exist!"));

		Branch branch = new Branch();
		branch.setBranchCode(dto.getBranchCode());
		branch.setBranchName(dto.getBranchName());
		branch.setCompanyId(dto.getCompanyId());
		branch.setAddress(dto.getAddress());
		branch.setCity(dto.getCity());
		branch.setState(dto.getState());
		branch.setCountry(dto.getCountry());
		branch.setPincode(dto.getPincode());
		branch.setPhone(dto.getPhone());
		branch.setEmail(dto.getEmail());
		branch.setHeadOffice(dto.getHeadOffice() != null ? dto.getHeadOffice() : false);
		branch.setActive(dto.getActive() != null ? dto.getActive() : true);
		branch.setCreatedBy(createdBy);

		Branch savedBranch = branchRepository.save(branch);

		// BUILD RESPONSE
		CompanyBranchResponseDto2 cbrd = new CompanyBranchResponseDto2();
		cbrd.setBranchId(savedBranch.getId());
		cbrd.setBranchCode(savedBranch.getBranchCode());
		cbrd.setBranchName(savedBranch.getBranchName());
		cbrd.setCity(savedBranch.getCity());
		cbrd.setActive(savedBranch.getActive());
		cbrd.setCreatedAt(savedBranch.getCreatedAt());

		CompanyBranchResponseDto1 cbrdt = new CompanyBranchResponseDto1();
		cbrdt.setCompanyId(savedBranch.getCompanyId());
		cbrdt.setCompanyCode(company.getCompanyCode());
		cbrdt.setCompanyName(company.getCompanyName());
		cbrdt.setCity(company.getCity());
		cbrdt.setActive(company.getActive());
		cbrdt.setBranch(cbrd);

		return new ApiResponse<>(true, "Branch created successfully", HttpStatus.CREATED.name(),
				HttpStatus.CREATED.value(), cbrdt);
	}

	@Override
	public ApiResponse<BranchCompanyResponseDto1> getBranchById(UUID id) {
		Branch branch = branchRepository.findByIdAndDeletedAtIsNullAndActiveTrue(id)
				.orElseThrow(() -> new ResourceNotFoundException("Branch is not found or inactive"));

		Company company = companyRepository.findByIdAndDeletedAtIsNullAndActiveTrue(branch.getCompanyId())
				.orElseThrow(() -> new ResourceNotFoundException("Company does not exist!"));

		BranchCompanyResponseDto2 bcrd = new BranchCompanyResponseDto2();
		bcrd.setCompanyId(branch.getCompanyId());
		bcrd.setCompanyCode(company.getCompanyCode());
		bcrd.setCompanyName(company.getCompanyName());
		bcrd.setCity(branch.getCity());
		bcrd.setActive(branch.getActive());
		bcrd.setCreatedAt(branch.getCreatedAt());

		BranchCompanyResponseDto1 bcrdt = new BranchCompanyResponseDto1();
		bcrdt.setBranchId(branch.getId());
		bcrdt.setBranchCode(branch.getBranchCode());
		bcrdt.setBranchName(branch.getBranchName());
		bcrdt.setCity(branch.getCity());
		bcrdt.setActive(branch.getActive());
		bcrdt.setCompany(bcrd);

		return new ApiResponse<>(true, "Branch fetched successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				bcrdt);

	}

	@Override
	public ApiResponse<CompanyBranchesResponseDto1> getBranchesByCompany(UUID companyId) {

	    Company company = companyRepository.findByIdAndDeletedAtIsNullAndActiveTrue(companyId)
	            .orElseThrow(() -> new ResourceNotFoundException("Company does not exist!"));

	    List<Branch> branches = branchRepository.findByCompanyIdAndDeletedAtIsNullAndActiveTrue(companyId);

	    if (branches.isEmpty()) {
	        throw new ResourceNotFoundException(
	                "No active branches found for the company with ID " + companyId);
	    }

	    List<CompanyBranchesResponseDto2> branchDtos = branches.stream().map(branch -> {
	        CompanyBranchesResponseDto2 dto = new CompanyBranchesResponseDto2();
	        dto.setBranchId(branch.getId());
	        dto.setBranchCode(branch.getBranchCode());
	        dto.setBranchName(branch.getBranchName());
	        dto.setCity(branch.getCity());
	        dto.setActive(branch.getActive());
	        dto.setCreatedAt(branch.getCreatedAt());
	        return dto;
	    }).toList();

	    CompanyBranchesResponseDto1 companyDto = new CompanyBranchesResponseDto1();
	    companyDto.setCompanyId(company.getId());
	    companyDto.setCompanyCode(company.getCompanyCode());
	    companyDto.setCompanyName(company.getCompanyName());
	    companyDto.setCity(company.getCity());
	    companyDto.setActive(company.getActive());
	    companyDto.setBranches(branchDtos);

	    return new ApiResponse<>(
	            true,
	            "Active branches fetched successfully",
	            HttpStatus.OK.name(),
	            HttpStatus.OK.value(),
	            companyDto
	    );
	}

 


}
