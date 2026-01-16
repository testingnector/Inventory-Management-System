package com.nector.auth.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionUpdateRequest {

	@Size(min = 3, max = 40, message = "Permission name must be between 3 and 40 characters")
    private String permissionName;
	
	@Size(min = 10, max = 150, message = "Description must be between 10 and 150 characters")
    private String description;
    
	private Boolean active;
    
    
}
