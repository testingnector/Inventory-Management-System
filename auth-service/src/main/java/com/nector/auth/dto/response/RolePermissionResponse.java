package com.nector.auth.dto.response;

import java.util.UUID;

import lombok.Data;

@Data
public class RolePermissionResponse {

    // Role info
	private UUID roleId;
    private String roleCode;
    private String roleName;

    // Permission info
    private UUID permissionId;
    private String permissionCode;
    private String permissionName;
    private String moduleName;

    private Boolean active;
}
