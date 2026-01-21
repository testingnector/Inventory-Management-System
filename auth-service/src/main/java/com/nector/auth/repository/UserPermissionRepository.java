package com.nector.auth.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nector.auth.entity.User;
import com.nector.auth.entity.UserPermission;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, UUID>{


	Optional<UserPermission> findByUserIdAndPermissionId(UUID userId, UUID permissionId);

	List<UserPermission> findByUserIdAndActiveTrue(UUID userId);

	List<UserPermission> findByUserId(UUID userId);

	List<UserPermission> findByPermissionId(UUID permissionId);

	Boolean existsByUserIdAndPermissionIdAndActiveTrue(UUID userId, UUID permissionId);

	Optional<UserPermission> findByUserIdAndPermissionIdAndDeletedAtIsNull(UUID userId, UUID permissionId);

	Optional<UserPermission> findByUserIdAndPermissionIdAndActiveTrueAndDeletedAtIsNull(UUID userId, UUID permissionId);

	List<UserPermission> findByUserIdAndDeletedAtIsNull(UUID userId);

	List<UserPermission> findByPermissionIdAndDeletedAtIsNull(UUID permissionId);

	List<UserPermission> findByDeletedAtIsNull();

	List<UserPermission> findByUserIdInAndDeletedAtIsNullAndActiveTrue(List<UUID> userIds);

	List<UserPermission> findByUserIdAndActiveTrueAndDeletedAtIsNull(UUID userId);

}
