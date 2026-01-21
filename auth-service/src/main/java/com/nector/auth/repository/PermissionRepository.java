package com.nector.auth.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nector.auth.entity.Permission;
import com.nector.auth.entity.User;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID>{

    Optional<Permission> findByPermissionCode(String permissionCode);

    boolean existsByPermissionCode(String permissionCode);

	List<Permission> findByDeletedAtIsNull();

	List<Permission> findAllByIdInAndDeletedAtIsNull(List<UUID> permissionIds);

	Optional<Permission> findByIdAndDeletedAtIsNull(UUID permissionId);

	List<Permission> findByIdInAndDeletedAtIsNull(List<UUID> permissionIds);

	List<Permission> findByIdInAndActiveTrueAndDeletedAtIsNull(List<UUID> permissionIds);

	List<Permission> findByIdInAndDeletedAtIsNullAndActiveTrue(Set<UUID> permissionIds);

	Optional<Permission> findByIdAndDeletedAtIsNullAndActiveTrue(UUID permissionId);
	
}
