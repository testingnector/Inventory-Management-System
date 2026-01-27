package com.nector.orgservice.dto.request.internal;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCreateRequest {

    @NotBlank(message = "Company code is required")
    @Size(max = 20, message = "Company code must be <= 20 chars")
    private String companyCode;

    @NotBlank(message = "Company name is required")
    @Size(max = 100, message = "Company name must be <= 100 chars")
    private String companyName;

    @NotBlank(message = "Legal name is required")
    @Size(max = 150, message = "Legal name must be <= 150 chars")
    private String legalName;

    @NotBlank(message = "Company type is required")
    @Size(max = 30, message = "Company type must be <= 30 chars")
    private String companyType;

    @NotBlank(message = "GST number is required")
    @Size(min = 15, max = 15, message = "GST number must be 15 chars")
    private String gstNumber;

    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}", message = "Invalid PAN format")
    private String panNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    @Size(max = 50, message = "Email must be <= 50 chars")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^(?:\\+91|91|0)?[6-9]\\d{9}$", 
            message = "Mobile number must be a valid Indian number"
        )
    private String phone;

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
}
