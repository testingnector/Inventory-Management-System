package com.nector.auth.dto.response.role_permission;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonPropertyOrder({ "roleId", "roleCode", "roleName", "roleDescription", "systemRole", "active", "permissions" })
public class RolePermissionGroupResponse {

	private UUID roleId;
    private String roleCode;
    private String roleName;
    private String roleDescription;
    private Boolean systemRole;
    private Boolean active;

    private List<RolePermissionResponse> permissions = new ArrayList<>();
}
