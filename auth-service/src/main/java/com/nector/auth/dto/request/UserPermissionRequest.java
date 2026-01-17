package com.nector.auth.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPermissionRequest {

	@NotNull(message = "User id is mandatory")
	private UUID userId;
	
	@NotNull(message = "Permission id is mandatory")
	private UUID permissionId;
	
	@NotNull(message = "Allowed field is mandatory")
	private Boolean allowed;
}
