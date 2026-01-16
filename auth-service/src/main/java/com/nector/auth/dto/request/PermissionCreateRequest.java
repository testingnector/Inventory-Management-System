package com.nector.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionCreateRequest {

    @NotBlank(message = "Permission code is required")
    @Size(min = 3, max = 30, message = "Permission code must be between 3 and 30 characters")
    @Pattern(
        regexp = "^[A-Z_]+$",
        message = "Permission code must contain only uppercase letters and underscores"
    )
    private String permissionCode;

    @NotBlank(message = "Permission name is required")
    @Size(min = 3, max = 40, message = "Permission name must be between 3 and 40 characters")
    private String permissionName;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 150, message = "Description must be between 10 and 150 characters")
    private String description;

    @NotBlank(message = "Module name is required")
    @Size(min = 3, max = 30, message = "Module name must be between 3 and 30 characters")
    private String moduleName;
}

