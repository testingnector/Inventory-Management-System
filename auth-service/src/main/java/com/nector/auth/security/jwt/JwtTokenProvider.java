package com.nector.auth.security.jwt;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.nector.auth.dto.response.internal.ApiResponse;
import com.nector.auth.security.CustomUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

	@Value("${jwt.token.validity}")
	private Long jwtTokenValidity;

	@Value("${jwt.secret}")
	private String jwtSecret;

	public ApiResponse<Map<String, String>> generateJwtToken(UserDetails userDetails) {

		CustomUserDetails customUserDetails = (CustomUserDetails) userDetails; // ✅ CAST

		List<String> roles = new ArrayList<>();
		for (GrantedAuthority authority : customUserDetails.getAuthorities()) {
			roles.add(authority.getAuthority());
		}

		Map<String, Object> claims = new HashMap<>();
		claims.put("roles", roles);
		claims.put("userId", customUserDetails.getUserId().toString());
		

		String token = Jwts.builder()
				.setClaims(claims)
				.setSubject(customUserDetails.getUsername())
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + jwtTokenValidity * 1000))
				.signWith(getKey(), SignatureAlgorithm.HS512).compact();

		return new ApiResponse<>(true, "Jwt token generated successfully", HttpStatus.OK.name(), 200, Map.of("token", token));
	}

	public SecretKey getKey() {
		return Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret));
	}

	// ================= CLAIMS =================

	public Claims getClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(getKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	public String getUsernameFromToken(String token) {
		return getClaims(token).getSubject();
	}

	public UUID getUserIdFromToken(String token) {
		return UUID.fromString(getClaims(token).get("userId", String.class));
	}
//
//	public UUID getCompanyIdFromToken(String token) {
//		return UUID.fromString(getClaims(token).get("companyId", String.class));
//	}

	public List<String> getRolesFromToken(String token) {
		return getClaims(token).get("roles", List.class);
	}

	private boolean isTokenExpired(String token) {
		return getClaims(token).getExpiration().before(new Date());
	}

	public boolean validateToken(String token, UserDetails userDetails) {
		try {
			return getUsernameFromToken(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
		} catch (Exception e) {
			return false;
		}
	}
	
	public LocalDateTime getExpiryDateFromNow() {
	    return LocalDateTime.now().plusSeconds(jwtTokenValidity);
	}

}
