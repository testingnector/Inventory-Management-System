package com.nector.auth.dto.response.role_permission;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonPropertyOrder({"permissionId", "permissionCode", "permissionName", "permissionDescription", "moduleName", "permissionIsActive", "allowed", "assignedActive", "assignedAt"})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RolePermissionResponse {

//	-------PERMISSION RESPONSE----------
	private UUID permissionId;
	private String permissionCode;
	private String permissionName;
	private String permissionDescription;
	private String moduleName;
	private Boolean permissionIsActive;

//	-------ROLE_PERMISSION RESPONSE----------
	private Boolean allowed;
	private Boolean assignedActive;
	private LocalDateTime assignedAt;
}
