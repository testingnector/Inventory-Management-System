package com.nector.auth.dto.request.internal;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRoleRevokeRequest {

    @NotNull(message = "UserRole id is mandatory")
    private UUID userRoleId;
}
