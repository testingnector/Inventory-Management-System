package com.nector.auth.service;

import com.nector.auth.dto.response.internal.ApiResponse;

public interface EmailService {

	ApiResponse<?> sendOtpToEmail(String email, String otp);

}
