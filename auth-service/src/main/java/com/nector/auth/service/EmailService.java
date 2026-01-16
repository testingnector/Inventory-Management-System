package com.nector.auth.service;

import com.nector.auth.dto.response.ApiResponse;

public interface EmailService {

	ApiResponse<?> sendOtpToEmail(String email, String otp);

}
