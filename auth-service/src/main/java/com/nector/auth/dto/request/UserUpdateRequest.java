package com.nector.auth.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateRequest {

    @Size(max = 35, message = "Name cannot exceed 35 characters")
    private String name;
    
    @Size(min = 8, max = 40, message = "Password must be at least 8 characters and at most 40 characters")
    private String password;

    @Pattern(
        regexp = "^(?:\\+91|91|0)?[6-9]\\d{9}$", 
        message = "Mobile number must be a valid Indian number"
    )
    private String mobileNumber;

    private Boolean active; // ADMIN or USER
	
}
