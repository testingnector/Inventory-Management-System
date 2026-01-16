package com.nector.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class RolePermissionRevokeRequest {

    @NotNull(message = "Role ID is required")
    private UUID roleId;

    @NotNull(message = "Permission ID is required")
    private UUID permissionId;
}
