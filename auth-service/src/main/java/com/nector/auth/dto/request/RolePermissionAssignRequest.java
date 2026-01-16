package com.nector.auth.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class RolePermissionAssignRequest {

    @NotNull(message = "Role ID is required")
    private UUID roleId;

    @NotEmpty(message = "At least one permission is required")
    private List<UUID> permissionIds;
}
