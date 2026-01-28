package com.nector.orgservice.dto.request.internal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BranchUpdateRequestDto {

	@Size(max = 100, message = "Company name must be <= 100 chars")
	private String branchName;

	@Size(max = 200, message = "Address must be <= 200 chars")
	private String address;

	@Size(max = 30, message = "Country must be <= 30 chars")
	private String country;

	@Size(max = 50, message = "State must be <= 50 chars")
	private String state;

	@Size(max = 50, message = "City must be <= 50 chars")
	private String city;

	@Pattern(regexp = "[1-9][0-9]{5}", message = "Invalid Indian PIN code")
	private String pincode;

	@Pattern(regexp = "^(?:\\+91|91|0)?[6-9]\\d{9}$", message = "Mobile number must be a valid Indian number")
	private String phone;

	@Email(message = "Invalid email")
	@Size(max = 50, message = "Email must be <= 50 chars")
	private String email;

	private Boolean active;
}
