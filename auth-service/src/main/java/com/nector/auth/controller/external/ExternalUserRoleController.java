package com.nector.auth.controller.external;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nector.auth.dto.response.external.CompanyUsersResponseExternalDto;
import com.nector.auth.dto.response.internal.ApiResponse;
import com.nector.auth.service.UserRoleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/external/user-roles")
@RequiredArgsConstructor
public class ExternalUserRoleController {

	private final UserRoleService userRoleService;
	
	@GetMapping("/{companyId}/users")
	public ResponseEntity<ApiResponse<List<CompanyUsersResponseExternalDto>>> getAllUsersByCompanyId(@PathVariable("companyId") UUID companyId) {
		ApiResponse<List<CompanyUsersResponseExternalDto>> response = userRoleService.getAllUsersByCompanyId(companyId);
		System.out.println(response.toString());
		return ResponseEntity.status(response.getHttpStatusCode()).body(response);
	}
	
	

}
