package com.nector.auth.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nector.auth.entity.User;
import com.nector.auth.entity.UserPermission;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, UUID>{

	Optional<UserPermission> findByUserIdAndPermissionIdAndActiveTrue(UUID userId, UUID permissionId);

	Optional<UserPermission> findByUserIdAndPermissionId(UUID userId, UUID permissionId);

	Optional<UserPermission> findByUserIdAndActiveTrue(UUID userId);

}
