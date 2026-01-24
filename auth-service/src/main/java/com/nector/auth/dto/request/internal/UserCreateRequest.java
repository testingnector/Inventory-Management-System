package com.nector.auth.dto.request.internal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateRequest {

	@NotBlank(message = "Name is mandatory")
    @Size(max = 35, message = "Name cannot exceed 35 characters")
    private String name;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    @Size(max = 40, message = "Email cannot exceed 40 characters")
    private String email;

    @NotBlank(message = "Password is mandatory")
    @Size(min = 8, max = 40, message = "Password must be at least 8 characters and at most 40 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,40}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
        )
    private String password;

    @NotBlank(message = "Mobile number is mandatory")
    @Pattern(
        regexp = "^(?:\\+91|91|0)?[6-9]\\d{9}$", 
        message = "Mobile number must be a valid Indian number"
    )
    private String mobileNumber;
    
}
