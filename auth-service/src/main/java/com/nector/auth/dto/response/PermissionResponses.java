package com.nector.auth.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PermissionResponses {

    private UUID id;
    private String permissionCode;
    private String permissionName;
    private String description;
    private String moduleName;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
