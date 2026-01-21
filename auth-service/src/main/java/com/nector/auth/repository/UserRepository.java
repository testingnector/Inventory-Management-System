package com.nector.auth.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nector.auth.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>{

	boolean existsByEmail(String email);

	Optional<User> findByEmail(String email);

	List<User> findByDeletedAtNull();

	Optional<User> findByIdAndDeletedAtIsNull(UUID userId);

	List<User> findByIdInAndDeletedAtIsNull(List<UUID> userIds);

	List<User> findByDeletedAtIsNull();

	Optional<User> findByIdAndActiveTrueAndDeletedAtIsNull(UUID userId);

	List<User> findByIdInAndDeletedAtIsNullAndActiveTrue(Set<UUID> userIds);
	

}
