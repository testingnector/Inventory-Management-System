package com.nector.auth.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignRoleRequest {

    @NotNull(message = "User id is mandatory")
    private UUID userId;

    @NotNull(message = "Role id is mandatory")
    private UUID roleId;

    @NotNull(message = "Company id is mandatory")
    private UUID companyId;

    private UUID branchId;
}

