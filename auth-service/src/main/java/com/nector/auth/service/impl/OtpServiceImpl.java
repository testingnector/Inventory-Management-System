package com.nector.auth.service.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nector.auth.dto.response.internal.ApiResponse;
import com.nector.auth.entity.OtpVerification;
import com.nector.auth.repository.OtpRepository;
import com.nector.auth.service.OtpService;

import jakarta.transaction.Transactional;

@Service
public class OtpServiceImpl implements OtpService {

	@Autowired
	private OtpRepository otpRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Value("${otp.expiry.minutes}")
	private Long otpExpiryMinutes;

	@Override
	@Transactional
	public void saveOtpInDatabase(String email, String otp) {

		OtpVerification otpEntity = otpRepository.findByEmail(email).orElse(new OtpVerification());

		if (otpEntity.getEmail() == null) {
			otpEntity.setEmail(email);
		}

		otpEntity.setOtp(passwordEncoder.encode(otp));
		otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
		otpEntity.setUsed(false);

		otpRepository.save(otpEntity);
	}

	@Override
	public ApiResponse<List<Object>> validateOtp(String email, String otp) {

		Optional<OtpVerification> optionalOtp = otpRepository.findByEmail(email);

		if (optionalOtp.isEmpty()) {
			return new ApiResponse<>(false, "OTP not found for given email! Please try again", HttpStatus.NOT_FOUND.name(),
					HttpStatus.NOT_FOUND.value(), Collections.emptyList());
		}

		OtpVerification otpEntity = optionalOtp.get();

		if (otpEntity.isUsed()) {
			return new ApiResponse<>(false, "This OTP is already used. Please request a new one.", HttpStatus.CONFLICT.name(),
					HttpStatus.CONFLICT.value(), Collections.emptyList());
		}

		if (LocalDateTime.now().isAfter(otpEntity.getExpiryTime())) {
			return new ApiResponse<>(false, "OTP has expired!", HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.value(),
					Collections.emptyList());
		}

		if (!passwordEncoder.matches(otp, otpEntity.getOtp())) {
			return new ApiResponse<>(false, "Invalid OTP!", HttpStatus.BAD_REQUEST.name(), HttpStatus.BAD_REQUEST.value(),
					Collections.emptyList());
		}

		// Mark OTP as used
		otpEntity.setUsed(true);
		otpRepository.save(otpEntity);

		return new ApiResponse<>(true, "OTP verified successfully", HttpStatus.OK.name(), HttpStatus.OK.value(), null);
	}

}
