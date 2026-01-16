package com.nector.auth.security.jwt;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nector.auth.entity.UserSession;
import com.nector.auth.repository.UserSessionRepository;
import com.nector.auth.security.CustomUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
	private JwtTokenProvider jwtTokenProvider;

	@Autowired
	private CustomUserDetailsService customUserDetailsService;

	@Autowired
	private UserSessionRepository userSessionRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String authHeader = request.getHeader("Authorization");

		if (authHeader != null && !authHeader.isEmpty() && authHeader.startsWith("Bearer ")) {

			String token = authHeader.substring(7);

			String username;

			try {
				username = jwtTokenProvider.getUsernameFromToken(token);
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}

			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

				UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

				Optional<UserSession> sessionOpt = userSessionRepository
						.findBySessionTokenAndActiveTrueAndRevokedAtIsNull(token);

				if (sessionOpt.isEmpty()) {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					return;
				}

				UserSession session = sessionOpt.get();

				if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					return;
				}

				session.setLastActivityAt(LocalDateTime.now());
				userSessionRepository.save(session);

				if (jwtTokenProvider.validateToken(token, userDetails)) {

					UUID userId = jwtTokenProvider.getUserIdFromToken(token);
					List<String> roles = jwtTokenProvider.getRolesFromToken(token);

					List<GrantedAuthority> authorities = new ArrayList<>();
					for (String role : roles) {
						authorities.add(new SimpleGrantedAuthority(role.startsWith("ROLE_") ? role : "ROLE_" + role));
					}

					UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
							userDetails, null, authorities);

					SecurityContextHolder.getContext().setAuthentication(authentication);
				}

			}

		}

		filterChain.doFilter(request, response);
	}

}
