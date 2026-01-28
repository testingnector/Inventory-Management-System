package com.nector.orgservice.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class FeignConfig {

    @Value("${external.services.auth-service-token}")
    private String authServiceToken;

    @Bean
    public RequestInterceptor authServiceInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Authorization", "Service " + authServiceToken);
        };
    }
}
