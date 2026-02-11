package com.nector.catalogservice.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.nector.catalogservice.config.FeignConfig;
import com.nector.catalogservice.dto.request.external.CompanyIdsRequestDto;
import com.nector.catalogservice.dto.response.external.CompanyResponseExternalDto;
import com.nector.catalogservice.dto.response.internal.ApiResponse;

import jakarta.validation.Valid;

@FeignClient(name = "ORG-SERVICE", configuration = FeignConfig.class, fallback = OrgServiceClientFallback.class)
public interface OrgServiceClient {

	@GetMapping("/companies/exists/{id}")
	public ResponseEntity<ApiResponse<Boolean>> existsByCompanyId(@PathVariable("id") UUID companyId);

	@GetMapping("/companies/detail/{id}")
	public ResponseEntity<ApiResponse<CompanyResponseExternalDto>> getCompanyBasic(@PathVariable("id") UUID companyId);

	@PostMapping("/details")
	public ResponseEntity<ApiResponse<List<CompanyResponseExternalDto>>> getCompaniesDetailsByCompanyIds(
			@Valid @RequestBody CompanyIdsRequestDto request);
}
