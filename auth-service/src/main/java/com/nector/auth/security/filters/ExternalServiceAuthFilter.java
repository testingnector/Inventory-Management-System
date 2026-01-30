package com.nector.auth.security.filters;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(1)
public class ExternalServiceAuthFilter extends OncePerRequestFilter {

	@Value("${external.services.org-service}")
	private String orgServiceToken;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String path = request.getRequestURI();
		if (path.startsWith("/external/")) {
			String authHeader = request.getHeader("Authorization");
			if (authHeader == null || !authHeader.equals("Service " + orgServiceToken)) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden: Invalid external service token");
				return;
			}

			// set ROLE_EXTERNAL_SERVICE for security context
			UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("ORG_SERVICE",
					null, List.of(new SimpleGrantedAuthority("ROLE_EXTERNAL_SERVICE")));
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		filterChain.doFilter(request, response);
	}
}
