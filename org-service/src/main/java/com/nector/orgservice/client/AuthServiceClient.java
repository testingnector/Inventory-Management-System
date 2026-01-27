package com.nector.orgservice.client;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.nector.orgservice.dto.response.external.CompanyUsersResponseExternalDto;
import com.nector.orgservice.dto.response.internal.ApiResponse;

@FeignClient(name = "AUTH-SERVICE")
public interface AuthServiceClient {

	@GetMapping("/$inter@nal&/user-roles/{companyId}/users")
	public ResponseEntity<ApiResponse<List<CompanyUsersResponseExternalDto>>> getAllUsersByCompanyId(@PathVariable("companyId") UUID companyId);
}
