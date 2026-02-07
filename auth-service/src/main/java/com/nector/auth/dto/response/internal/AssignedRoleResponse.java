package com.nector.auth.dto.response.internal;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"roleId", "roleCode", "roleName", "roleDescription", "systemRole", "roleIsActive", "allowed", "assignedActive", "assignedAt"})
@Data
public class AssignedRoleResponse {

	private UUID roleId;
    private String roleCode;
    private String roleName;
    private String roleDescription;
    private Boolean systemRole;
    private Boolean roleIsActive;
    
	private Boolean allowed;
	private Boolean assignedActive;
	private LocalDateTime assignedAt;
}
