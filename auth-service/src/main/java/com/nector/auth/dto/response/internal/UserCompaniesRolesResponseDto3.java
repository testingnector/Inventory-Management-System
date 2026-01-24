package com.nector.auth.dto.response.internal;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"roleId", "roleCode", "roleName", "active", "userRoleId", "assignedIsActive", "assignedAt", "revokedAt"})
@Data
public class UserCompaniesRolesResponseDto3 {

    private UUID roleId;
    private String roleCode;
    private String roleName;
    private Boolean active;
    
    private UUID userRoleId;
    private Boolean assignedIsActive;
    private LocalDateTime assignedAt;
    private LocalDateTime revokedAt;
}
