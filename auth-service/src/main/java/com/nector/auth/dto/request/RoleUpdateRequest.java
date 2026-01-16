package com.nector.auth.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleUpdateRequest {
	
    @Size(max = 50, message = "Role name cannot exceed 50 characters")
	private String roleName;
    
    @Size(max = 100, message = "Description cannot exceed 100 characters")  
	private String description; // only description update
    
	private Boolean active;   // enable/disable
}
