package com.nector.auth.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nector.auth.entity.UserSession;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

	Optional<UserSession> findBySessionTokenAndActiveTrueAndRevokedAtIsNull(String sessionToken);

	void deleteByExpiresAtBefore(LocalDateTime time);
}
