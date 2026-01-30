package com.nector.orgservice.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nector.orgservice.dto.request.internal.BranchCreateRequestDto;
import com.nector.orgservice.dto.request.internal.BranchUpdateRequestDto;
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

	@Transactional
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
		cbrd.setAddress(savedBranch.getAddress());
		cbrd.setActive(savedBranch.getActive());
		cbrd.setHeadOffice(branch.getHeadOffice());
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

	@Transactional
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
		bcrdt.setAddress(branch.getAddress());
		bcrdt.setActive(branch.getActive());
		bcrdt.setHeadOffice(branch.getHeadOffice());
		bcrdt.setCompany(bcrd);

		return new ApiResponse<>(true, "Branch fetched successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				bcrdt);

	}

	@Transactional
	@Override
	public ApiResponse<CompanyBranchesResponseDto1> getBranchesByCompany(UUID companyId) {

		Company company = companyRepository.findByIdAndDeletedAtIsNullAndActiveTrue(companyId)
				.orElseThrow(() -> new ResourceNotFoundException("Company does not exist!"));

		List<Branch> branches = branchRepository.findByCompanyIdAndDeletedAtIsNullAndActiveTrue(companyId);

		if (branches.isEmpty()) {
			throw new ResourceNotFoundException("No active branches found for the company with ID " + companyId);
		}

		List<CompanyBranchesResponseDto2> branchDtos = branches.stream().map(branch -> {
			CompanyBranchesResponseDto2 dto = new CompanyBranchesResponseDto2();
			dto.setBranchId(branch.getId());
			dto.setBranchCode(branch.getBranchCode());
			dto.setBranchName(branch.getBranchName());
			dto.setCity(branch.getCity());
			dto.setAddress(branch.getAddress());
			dto.setActive(branch.getActive());
			dto.setHeadOffice(branch.getHeadOffice());
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

		return new ApiResponse<>(true, "Active branches fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), companyDto);
	}

	@Transactional
	@Override
	public ApiResponse<BranchCompanyResponseDto1> updateBranch(UUID branchId, BranchUpdateRequestDto dto,
			UUID updatedBy) {

		Branch branch = branchRepository.findByIdAndDeletedAtIsNull(branchId)
				.orElseThrow(() -> new ResourceNotFoundException("Branch not found or already deleted"));

		if (dto.getBranchName() != null)
			branch.setBranchName(dto.getBranchName());
		if (dto.getAddress() != null)
			branch.setAddress(dto.getAddress());
		if (dto.getCountry() != null)
			branch.setCountry(dto.getCountry());
		if (dto.getState() != null)
			branch.setState(dto.getState());
		if (dto.getCity() != null)
			branch.setCity(dto.getCity());
		if (dto.getPincode() != null)
			branch.setPincode(dto.getPincode());
		if (dto.getPhone() != null)
			branch.setPhone(dto.getPhone());
		if (dto.getEmail() != null)
			branch.setEmail(dto.getEmail());

		if (dto.getActive() != null) {
		    branch.setActive(dto.getActive());

		    if (!dto.getActive()) { 
		        if (Boolean.TRUE.equals(branch.getHeadOffice())) {
		            branch.setHeadOffice(false); 
		        }
		    }
		}


		branch.setUpdatedBy(updatedBy);

		Branch updatedBranch = branchRepository.save(branch);

		Company company = companyRepository.findByIdAndDeletedAtIsNullAndActiveTrue(updatedBranch.getCompanyId())
				.orElseThrow(() -> new ResourceNotFoundException("Company does not exist"));

		// Response mapping
		BranchCompanyResponseDto2 companyDto = new BranchCompanyResponseDto2();
		companyDto.setCompanyId(company.getId());
		companyDto.setCompanyCode(company.getCompanyCode());
		companyDto.setCompanyName(company.getCompanyName());
		companyDto.setCity(company.getCity());
		companyDto.setActive(company.getActive());
		companyDto.setCreatedAt(company.getCreatedAt());

		BranchCompanyResponseDto1 responseDto = new BranchCompanyResponseDto1();
		responseDto.setBranchId(updatedBranch.getId());
		responseDto.setBranchCode(updatedBranch.getBranchCode());
		responseDto.setBranchName(updatedBranch.getBranchName());
		responseDto.setCity(updatedBranch.getCity());
		responseDto.setAddress(updatedBranch.getAddress());
		responseDto.setActive(updatedBranch.getActive());
		responseDto.setHeadOffice(updatedBranch.getHeadOffice());
		responseDto.setCompany(companyDto);

		return new ApiResponse<>(true, "Branch updated successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				responseDto);
	}

	@Transactional
	@Override
	public ApiResponse<Void> deleteBranch(UUID branchId, UUID deletedBy) {

		Branch branch = branchRepository.findByIdAndDeletedAtIsNull(branchId)
				.orElseThrow(() -> new ResourceNotFoundException("Branch not found or already deleted"));

		branch.setDeletedAt(LocalDateTime.now());
		branch.setDeletedBy(deletedBy);
		branch.setActive(false);

		branchRepository.save(branch);

		return new ApiResponse<>(true, "Branch deleted successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				null);
	}

	@Override
	public ApiResponse<BranchCompanyResponseDto1> getBranchByCode(String branchCode) {

		Branch branch = branchRepository.findByBranchCodeAndDeletedAtIsNullAndActiveTrue(branchCode)
				.orElseThrow(() -> new ResourceNotFoundException("Branch not found or inactive"));

		Company company = companyRepository.findByIdAndDeletedAtIsNullAndActiveTrue(branch.getCompanyId())
				.orElseThrow(() -> new ResourceNotFoundException("Company does not exist"));

//		BUILD RESPONSE
		BranchCompanyResponseDto2 companyDto = new BranchCompanyResponseDto2();
		companyDto.setCompanyId(company.getId());
		companyDto.setCompanyCode(company.getCompanyCode());
		companyDto.setCompanyName(company.getCompanyName());
		companyDto.setCity(company.getCity());
		companyDto.setActive(company.getActive());
		companyDto.setCreatedAt(company.getCreatedAt());

		BranchCompanyResponseDto1 responseDto = new BranchCompanyResponseDto1();
		responseDto.setBranchId(branch.getId());
		responseDto.setBranchCode(branch.getBranchCode());
		responseDto.setBranchName(branch.getBranchName());
		responseDto.setCity(branch.getCity());
		responseDto.setAddress(branch.getAddress());
		responseDto.setActive(branch.getActive());
		responseDto.setHeadOffice(branch.getHeadOffice());
		responseDto.setCompany(companyDto);

		return new ApiResponse<>(true, "Branch fetched successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				responseDto);
	}

	@Override
	public ApiResponse<BranchCompanyResponseDto1> getHeadOfficeByCompanyId(UUID companyId) {

		Branch branch = branchRepository.findByCompanyIdAndHeadOfficeTrueAndDeletedAtIsNullAndActiveTrue(companyId)
				.orElseThrow(() -> new ResourceNotFoundException("Active head office not found"));

		Company company = companyRepository.findByIdAndDeletedAtIsNullAndActiveTrue(companyId)
				.orElseThrow(() -> new ResourceNotFoundException("Company not found"));

		// Company DTO
		BranchCompanyResponseDto2 companyDto = new BranchCompanyResponseDto2();
		companyDto.setCompanyId(company.getId());
		companyDto.setCompanyCode(company.getCompanyCode());
		companyDto.setCompanyName(company.getCompanyName());
		companyDto.setCity(company.getCity());
		companyDto.setActive(company.getActive());
		companyDto.setCreatedAt(company.getCreatedAt());

		// Branch DTO
		BranchCompanyResponseDto1 dto = new BranchCompanyResponseDto1();
		dto.setBranchId(branch.getId());
		dto.setBranchCode(branch.getBranchCode());
		dto.setBranchName(branch.getBranchName());
		dto.setCity(branch.getCity());
		dto.setAddress(branch.getAddress());
		dto.setActive(branch.getActive());
		dto.setHeadOffice(branch.getHeadOffice());
		dto.setCompany(companyDto);

		return new ApiResponse<>(true, "Head office fetched successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				dto);
	}

	@Transactional
	@Override
	public ApiResponse<Void> changeHeadOffice(UUID branchId, UUID updatedBy) {

		Branch newHeadOffice = branchRepository.findByIdAndDeletedAtIsNull(branchId)
				.orElseThrow(() -> new ResourceNotFoundException("Branch not found or deleted"));

		if (!Boolean.TRUE.equals(newHeadOffice.getActive())) {
			throw new RuntimeException("Inactive branch cannot be head office");
		}

		UUID companyId = newHeadOffice.getCompanyId();

		branchRepository.findByCompanyIdAndHeadOfficeTrueAndDeletedAtIsNullAndActiveTrue(companyId).ifPresent(oldHo -> {
			oldHo.setHeadOffice(false);
			oldHo.setUpdatedBy(updatedBy);
			branchRepository.save(oldHo);
		});

		newHeadOffice.setHeadOffice(true);
		newHeadOffice.setUpdatedBy(updatedBy);
		branchRepository.save(newHeadOffice);

		return new ApiResponse<>(true, "Head office changed successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				null);
	}

}
