package com.nector.auth.dto.response.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"permissionId", "permissionCode", "permissionName", "permissionDescription", "moduleName", "active", "roles"})
@Data
public class PermissionRolesResponse {

	private UUID permissionId;
	private String permissionCode;
	private String permissionName;
	private String permissionDescription;
	private String moduleName;
	private Boolean active;
	
    private List<AssignedRoleResponse> roles = new ArrayList<>();
}
