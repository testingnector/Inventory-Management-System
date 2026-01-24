package com.nector.auth.dto.request.internal;

import lombok.Data;

@Data
public class VerifyOtpRequest {

	private String email;
	private String otp;
		
}
