package com.nector.auth.security.gateway;

import java.net.http.HttpHeaders;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.nector.auth.security.jwt.JwtTokenProvider;

@Component
public class AuthHeaderFilter {

//    private final JwtTokenProvider jwtTokenProvider;
//
//    @Override
//    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//
//        String authHeader = exchange.getRequest()
//                .getHeaders()
//                .getFirst(HttpHeaders.AUTHORIZATION);
//
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            return chain.filter(exchange);
//        }
//
//        String token = authHeader.substring(7);
//
//        String userId = jwtUtil.getUserId(token);
//        String role = jwtUtil.getRole(token);
//        String companyId = jwtUtil.getCompanyId(token);
//
//        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
//                .header("X-USER-ID", userId)
//                .header("X-USER-ROLE", role)
//                .header("X-COMPANY-ID", companyId)
//                .build();
//
//        return chain.filter(exchange.mutate().request(mutatedRequest).build());
//    }
}
