package com.nector.catalogservice.advice;

import java.util.UUID;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.nector.catalogservice.dto.common.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalApiResponseHandler implements ResponseBodyAdvice<Object> {

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return returnType.getParameterType().equals(ApiResponse.class);
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {

		if (body instanceof ApiResponse<?> apiResponse) {
			HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();

			String traceId = (String) servletRequest.getAttribute("traceId");
			if (traceId == null)
				traceId = UUID.randomUUID().toString();
			apiResponse.setTraceId(traceId);

			apiResponse.setPath(servletRequest.getRequestURI());

			apiResponse.setApiVersion(extractVersion(servletRequest.getRequestURI()));
		}

		return body;
	}

	private String extractVersion(String uri) {
		String[] parts = uri.split("/");
		for (String part : parts) {
			if (part.matches("v\\d+"))
				return part;
		}
		return "v1";
	}
}
