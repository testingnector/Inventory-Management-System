package com.nector.auth.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nector.auth.entity.RolePermission;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, UUID>{

	Optional<RolePermission> findByRoleIdAndPermissionId(UUID roleId, UUID permissionId);

	List<RolePermission> findByRoleIdAndActiveTrue(UUID roleId);

	List<RolePermission> findByRoleId(UUID roleId);

	List<RolePermission> findByPermissionId(UUID permissionId);

	List<RolePermission> findByRoleIdInAndActiveIsTrue(List<UUID> rolesIds);

	List<UUID> findRoleIdsByPermissionId(UUID permissionId);

	List<UUID> findRoleIdsByPermissionIdAndActiveTrueAndDeletedAtIsNull(UUID permissionId);

	List<RolePermission> findByRoleIdInAndActiveTrueAndDeletedAtIsNull(List<UUID> roleIds);

	List<RolePermission> findByPermissionIdAndActiveTrueAndDeletedAtIsNull(UUID permissionId);

	List<RolePermission> findByRoleIdAndPermissionIdIn(UUID roleId, List<UUID> permissionIds);

}
