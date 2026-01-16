package com.nector.auth.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

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

    // Mapping info
    private Boolean active;
    private LocalDateTime assignedAt;
    private String assignedBy;   
    private LocalDateTime revokedAt;
    private String revokedBy;
}
