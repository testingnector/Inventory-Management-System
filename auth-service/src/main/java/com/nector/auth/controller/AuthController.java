package com.nector.auth.controller;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nector.auth.dto.request.LoginRequest;
import com.nector.auth.dto.request.VerifyOtpRequest;
import com.nector.auth.dto.response.ApiResponse;
import com.nector.auth.entity.UserSession;
import com.nector.auth.repository.UserSessionRepository;
import com.nector.auth.security.CustomUserDetails;
import com.nector.auth.security.CustomUserDetailsService;
import com.nector.auth.security.jwt.JwtTokenProvider;
import com.nector.auth.security.util.DeviceUtils;
import com.nector.auth.service.EmailService;
import com.nector.auth.service.OtpService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private OtpService otpService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private CustomUserDetailsService customUserDetailsService;

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private UserSessionRepository userSessionRepository;

	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {

		String email = loginRequest.getEmail();
		String password = loginRequest.getPassword();

		UsernamePasswordAuthenticationToken upat = new UsernamePasswordAuthenticationToken(email, password);
		Authentication authentication = authenticationManager.authenticate(upat);

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();

		String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

		otpService.saveOtpInDatabase(email, otp);

		ApiResponse<?> response = emailService.sendOtpToEmail(email, otp);

		if (response.isSuccess()) {
			return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		} else {
			return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		}

	}

	@PostMapping("/verify-otp")
	public ResponseEntity<ApiResponse<?>> verifyOtp(@Valid @RequestBody VerifyOtpRequest verifyOtpRequest,
			HttpServletRequest request) {

		ApiResponse<List<Object>> response = otpService.validateOtp(verifyOtpRequest.getEmail(), verifyOtpRequest.getOtp());

		if (!response.isSuccess()) {
			return ResponseEntity.status(response.getHttpStatusCode()).body(response);
		}

		UserDetails userDetails = customUserDetailsService.loadUserByUsername(verifyOtpRequest.getEmail());

		ApiResponse<Map<String, String>> jwtResponse = jwtTokenProvider.generateJwtToken(userDetails);

		String token = (String) ((Map<?, ?>) jwtResponse.getData()).get("token");

		CustomUserDetails cud = (CustomUserDetails) userDetails;

		UserSession session = new UserSession();
		session.setUserId(cud.getUserId());
		session.setSessionToken(token);
		session.setTokenType("ACCESS");
		session.setIpAddress(request.getRemoteAddr());
		session.setDeviceType(DeviceUtils.resolveDeviceType(request));
		session.setUserAgent(request.getHeader("User-Agent"));
		session.setExpiresAt(jwtTokenProvider.getExpiryDateFromNow());
		session.setActive(true);

		userSessionRepository.save(session);

		return ResponseEntity.ok(jwtResponse);
	}

	@PostMapping("/logout")
	public ResponseEntity<ApiResponse<List<Object>>> logout(@RequestHeader("Authorization") String authHeader) {

		String token = authHeader.substring(7);

		userSessionRepository.findBySessionTokenAndActiveTrueAndRevokedAtIsNull(token).ifPresent(session -> {
			session.setActive(false);
			session.setLogoutAt(LocalDateTime.now());
			userSessionRepository.save(session);
		});

		return ResponseEntity.ok(new ApiResponse<>(true, "Log out successfully...", HttpStatus.OK.name(), HttpStatus.OK.value(), Collections.emptyList()));
	}

}
