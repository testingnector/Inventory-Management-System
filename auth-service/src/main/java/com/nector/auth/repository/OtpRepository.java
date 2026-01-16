package com.nector.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nector.auth.entity.OtpVerification;

@Repository
public interface OtpRepository extends JpaRepository<OtpVerification, UUID>{

	Optional<OtpVerification> findByEmail(String email);

}
