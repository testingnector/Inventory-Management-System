package com.nector.auth.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.nector.auth.security.jwt.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

	@Autowired
	private JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable())

				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.authorizeHttpRequests(auth -> auth

						// allow internal org-service feign call
						.requestMatchers("/companies/**").permitAll()
//						.requestMatchers("/$inter@nal&/**").permitAll()

						// Public endpoints
						.requestMatchers("/auth/**").permitAll()

						// User creation
						.requestMatchers(HttpMethod.POST, "/users").hasAnyRole("SUPER_ADMIN", "ADMIN")

						// User Role assign
						.requestMatchers("/user-roles/**").hasAnyRole("SUPER_ADMIN", "ADMIN")

						// Role management (specific)
						.requestMatchers(HttpMethod.POST, "/roles/**").hasRole("SUPER_ADMIN")
						.requestMatchers(HttpMethod.PUT, "/roles/**").hasRole("SUPER_ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/roles/**").hasRole("SUPER_ADMIN")
						.requestMatchers(HttpMethod.GET, "/roles/**").hasAnyRole("SUPER_ADMIN", "ADMIN")

						// Permission management (specific)
						.requestMatchers(HttpMethod.POST, "/permissions/**").hasRole("SUPER_ADMIN")
						.requestMatchers(HttpMethod.PUT, "/permissions/**").hasRole("SUPER_ADMIN")
						.requestMatchers(HttpMethod.DELETE, "/permissions/**").hasRole("SUPER_ADMIN")
						.requestMatchers(HttpMethod.GET, "/permissions/**").hasAnyRole("SUPER_ADMIN", "ADMIN")

						// Catch-all for everything else
						.anyRequest().authenticated());

		// Add JWT filter
		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}
}
