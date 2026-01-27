package com.nector.orgservice.dto.request.internal;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BranchCreateRequestDto {

    @NotBlank(message = "Branch code is required")
    @Size(max = 20, message = "Branch code must be <= 20 chars")
    private String branchCode;

    @NotBlank(message = "Branch name is required")
    @Size(max = 100, message = "Company name must be <= 100 chars")
    private String branchName;

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    @NotBlank(message = "Address is required")
    @Size(max = 200, message = "Address must be <= 200 chars")
    private String address;

    @NotBlank(message = "Country is required")
    @Size(max = 30, message = "Country must be <= 30 chars")
    private String country = "India";

    @NotBlank(message = "State is required")
    @Size(max = 50, message = "State must be <= 50 chars")
    private String state;

    @NotBlank(message = "City is required")
    @Size(max = 50, message = "City must be <= 50 chars")
    private String city;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "[1-9][0-9]{5}", message = "Invalid Indian PIN code")
    private String pincode;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^(?:\\+91|91|0)?[6-9]\\d{9}$", 
            message = "Mobile number must be a valid Indian number"
        )
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    @Size(max = 50, message = "Email must be <= 50 chars")
    private String email;

    private Boolean headOffice;
    private Boolean active;
}

