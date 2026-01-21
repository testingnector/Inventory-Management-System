package com.nector.auth.dto.response.role_permission;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class PermissionRoleGroupResponse {

	private UUID permissionId;
	private String permissionCode;
	private String permissionName;
	private String permissionDescription;
	private String moduleName;
	private Boolean active;
	
    private List<PermissionRoleResponse> roles = new ArrayList<>();

}
