package com.nector.auth.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.nector.auth.dto.request.external.CompanyBasicResponse;
import com.nector.auth.dto.response.ApiResponse;
import com.nector.auth.dto.response.external.CompanyIdsRequest;

import jakarta.validation.Valid;

@FeignClient(name = "ORG-SERVICE")
public interface OrgServiceClient {

	@GetMapping("/companies/exists/{id}")
	public ResponseEntity<ApiResponse<Boolean>> existsByCompanyId(@PathVariable("id") UUID companyId);
	
	@GetMapping("/companies/basic/{id}")
	public ResponseEntity<ApiResponse<CompanyBasicResponse>> getCompanyBasic(@PathVariable("id") UUID companyId);
	
	@PostMapping("/companies/basic")
	public ResponseEntity<ApiResponse<List<CompanyBasicResponse>>> getCompanyBasicByCompanyIds(@Valid @RequestBody CompanyIdsRequest request);
}

