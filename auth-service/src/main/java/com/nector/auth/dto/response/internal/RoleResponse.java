package com.nector.auth.dto.response.internal;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleResponse {
    private UUID id;
    private String roleCode;
    private String roleName;
    private String roleDescription;
    private Boolean isSystemRole;
    private Boolean isActive;
}
