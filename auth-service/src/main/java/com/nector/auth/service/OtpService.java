package com.nector.auth.service;

import java.util.List;

import com.nector.auth.dto.response.internal.ApiResponse;

public interface OtpService {

	void saveOtpInDatabase(String email, String otp);

	ApiResponse<List<Object>> validateOtp(String email, String otp);

}
