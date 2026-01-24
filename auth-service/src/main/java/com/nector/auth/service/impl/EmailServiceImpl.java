package com.nector.auth.service.impl;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.nector.auth.dto.response.internal.ApiResponse;
import com.nector.auth.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {

	@Autowired
	private JavaMailSender javaMailSender;

	@Override
	public ApiResponse<?> sendOtpToEmail(String email, String otp) {

		try {
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(email);
			mailMessage.setSubject("Your Otp code");
			mailMessage.setText("Hi,\n\n" + "Use the following OTP to complete your login:\n\n" + otp + "\n\n"
					+ "⏰ Valid for 1 minute only.\n" + "🔒 Do not share this OTP with anyone.\n\n" + "Regards from,\n"
					+ "Bindeshwar Team");

			javaMailSender.send(mailMessage);

			return new ApiResponse<>(true, "Otp send successfully...", HttpStatus.OK.name(), HttpStatus.OK.value(),
					"Otp is valid for 1 minute only.");
		
		} catch (Exception e) {
			return new ApiResponse<>(false, "Error occurred during otp sending process!",
					HttpStatus.INTERNAL_SERVER_ERROR.name(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
					Collections.emptyList());
		}
	}

}
