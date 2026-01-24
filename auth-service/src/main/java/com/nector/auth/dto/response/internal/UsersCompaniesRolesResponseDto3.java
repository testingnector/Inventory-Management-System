package com.nector.auth.dto.response.internal;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonPropertyOrder({"roleId", "roleCode", "roleName", "roleIsActive", "userRoleId", "assignedIsActive", "assignedAt", "revokedAt"})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsersCompaniesRolesResponseDto3 {
    private UUID roleId;
    private String roleCode;
    private String roleName;
    private Boolean roleIsActive;
    private UUID userRoleId;
    private Boolean assignedIsActive;
    private LocalDateTime assignedAt;
    private LocalDateTime revokedAt;
}

