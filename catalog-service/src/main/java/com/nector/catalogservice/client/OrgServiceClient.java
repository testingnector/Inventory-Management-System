package com.nector.catalogservice.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.nector.catalogservice.dto.response.external.CompanyResponseExternalDto;
import com.nector.catalogservice.dto.response.internal.ApiResponse;

@FeignClient(name = "ORG-SERVICE")
public interface OrgServiceClient {

	@GetMapping("/companies/exists/{id}")
	public ResponseEntity<ApiResponse<Boolean>> existsByCompanyId(@PathVariable("id") UUID companyId);
	
	@GetMapping("/companies/detail/{id}")
	public ResponseEntity<ApiResponse<CompanyResponseExternalDto>> getCompanyBasic(@PathVariable("id") UUID companyId);
	
}

