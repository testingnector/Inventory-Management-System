package com.nector.auth.dto.response.internal;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PermissionResponse {

    private UUID permissionId;
    private String permissionCode;
    private String permissionName;
    private String description;
    private String moduleName;
    private Boolean active;
    private LocalDateTime createdAt;
}
