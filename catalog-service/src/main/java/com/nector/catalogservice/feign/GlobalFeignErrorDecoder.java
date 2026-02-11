package com.nector.catalogservice.feign;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.exception.ExternalServiceException;

import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;

@Component
public class GlobalFeignErrorDecoder implements ErrorDecoder{

	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	public Exception decode(String methodKey, Response response) {
		
		String message = "Error occurred while communicating with external service!";
		
		try {
			if (response.body() != null) {
				InputStreamReader inputStreamReader = new InputStreamReader(response.body().asInputStream(), StandardCharsets.UTF_8);
				String body = Util.toString(inputStreamReader);
				
				ApiResponse<?> apiResponse = objectMapper.readValue(body, ApiResponse.class);
				
				if (apiResponse.getMessage() != null && !apiResponse.getMessage().isEmpty()) {
					message = apiResponse.getMessage();
				}
				
			}
		} 
		catch (Exception e) {
			message = "Unable to parse error response from external service";
		}

		HttpStatus status;
		try {
			status = HttpStatus.valueOf(response.status());
		} 
		catch (Exception e) {
			 status = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		
		return new ExternalServiceException(message, status);
	}

}
