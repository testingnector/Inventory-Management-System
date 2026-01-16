package com.nector.auth.dto.response;

import java.util.UUID;

import lombok.Data;

@Data
public class UserRoleResponse {

	private UUID id;
	
	private UUID userId;
	
	private String name;
	
	private String email;
	
	private String mobileNumber;
	
	private UUID roleId;
	
	private String roleCode;
	
	private String roleName;
	
	private Boolean active;
}
