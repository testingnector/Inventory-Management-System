package com.nector.auth.dto.response.user_permission;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonPropertyOrder({ "permissionId", "permissionCode", "permissionName", "permissionDescription", "moduleName", "permissionActive", "allowed", "assignedActive", "assignedAt" })
public class UserPermissionResponse{

//	-------PERMISSION RESPONSE----------
	private UUID permissionId;
	private String permissionCode;
	private String permissionName;
	private String permissionDescription;
	private String moduleName;
	private Boolean permissionActive;

//	-------ROLE_PERMISSION RESPONSE----------
	private Boolean allowed;
	private Boolean assignedActive;
	private LocalDateTime assignedAt;

}
