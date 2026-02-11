package com.nector.catalogservice.client;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.nector.catalogservice.dto.response.external.CompanyResponseExternalDto;
import com.nector.catalogservice.dto.response.internal.ApiResponse;

@Component
public class OrgServiceClientFallback implements OrgServiceClient {

	@Override
	public ResponseEntity<ApiResponse<Boolean>> existsByCompanyId(UUID companyId) {
		ApiResponse<Boolean> response = new ApiResponse<>(false,
				"Org-Service is currently unavailable. Unable to check company existence, fallback response",
				HttpStatus.SERVICE_UNAVAILABLE.name(), HttpStatus.SERVICE_UNAVAILABLE.value(), false);
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

	@Override
	public ResponseEntity<ApiResponse<CompanyResponseExternalDto>> getCompanyBasic(UUID companyId) {

		CompanyResponseExternalDto defaultCompany = new CompanyResponseExternalDto();
		defaultCompany.setCompanyId(companyId);
		defaultCompany.setCompanyName("Unknown");
		defaultCompany.setCompanyCode("UNKNOWN");
		defaultCompany.setActive(false);

		ApiResponse<CompanyResponseExternalDto> response = new ApiResponse<>(false,
				"Org-Service is currently unavailable. Unable to fetch company details, fallback response",
				HttpStatus.SERVICE_UNAVAILABLE.name(), HttpStatus.SERVICE_UNAVAILABLE.value(), defaultCompany);

		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}

}
