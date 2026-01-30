package com.nector.auth.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.nector.auth.security.filters.ExternalServiceAuthFilter;
import com.nector.auth.security.jwt.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http,
	        JwtAuthenticationFilter jwtAuthenticationFilter,
	        ExternalServiceAuthFilter externalServiceAuthFilter) throws Exception {

	    http.csrf(csrf -> csrf.disable())
	        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers("/external/**").permitAll()
	            .requestMatchers("/companies/**").permitAll()
	            .requestMatchers("/auth/**").permitAll()
	            .requestMatchers(HttpMethod.POST, "/users/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
	            .requestMatchers("/user-roles/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
	            .requestMatchers(HttpMethod.POST, "/roles/**").hasRole("SUPER_ADMIN")
	            .requestMatchers(HttpMethod.PUT, "/roles/**").hasRole("SUPER_ADMIN")
	            .requestMatchers(HttpMethod.DELETE, "/roles/**").hasRole("SUPER_ADMIN")
	            .requestMatchers(HttpMethod.GET, "/roles/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
	            .requestMatchers(HttpMethod.POST, "/permissions/**").hasRole("SUPER_ADMIN")
	            .requestMatchers(HttpMethod.PUT, "/permissions/**").hasRole("SUPER_ADMIN")
	            .requestMatchers(HttpMethod.DELETE, "/permissions/**").hasRole("SUPER_ADMIN")
	            .requestMatchers(HttpMethod.GET, "/permissions/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
	            .anyRequest().authenticated()
	        );

	    // Filters in explicit order
	    http.addFilterBefore(externalServiceAuthFilter, UsernamePasswordAuthenticationFilter.class);
	    http.addFilterAfter(jwtAuthenticationFilter, ExternalServiceAuthFilter.class);

	    return http.build();
	}


	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}
}
