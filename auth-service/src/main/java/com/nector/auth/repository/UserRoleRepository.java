package com.nector.auth.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nector.auth.entity.User;
import com.nector.auth.entity.UserRole;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID>{

	List<UserRole> findByUserId(UUID id);

	boolean existsByUserIdAndRoleId(UUID id, UUID id2);

	boolean existsByUserIdAndRoleIdAndCompanyIdAndActiveTrue(UUID userId, UUID roleId, UUID companyId);

	Optional<UserRole> findByIdAndActiveTrue(UUID userRoleId);

	Optional<UserRole> findByUserIdAndRoleIdAndCompanyIdAndActiveFalse(UUID userId, UUID roleId, UUID companyId);

	List<UserRole> findByUserIdAndActiveTrue(UUID userId);

	List<UserRole> findByDeletedAtNull();

}
