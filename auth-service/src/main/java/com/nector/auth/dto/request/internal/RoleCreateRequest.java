package com.nector.auth.dto.request.internal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleCreateRequest {

    @NotBlank(message = "Role code is mandatory")  
    @Size(max = 20, message = "Role code cannot exceed 20 characters")  
    private String roleCode;

    @NotBlank(message = "Role name is mandatory")
    @Size(max = 50, message = "Role name cannot exceed 50 characters")
    private String roleName;

    @NotBlank(message = "Description is mandatory")
    @Size(max = 100, message = "Description cannot exceed 100 characters")  
    private String description;
    
    private Boolean systemRole = false;
    
    
    
}