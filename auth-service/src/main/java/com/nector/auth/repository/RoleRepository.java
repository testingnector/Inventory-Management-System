package com.nector.auth.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nector.auth.dto.response.RoleResponse;
import com.nector.auth.entity.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByIdAndDeletedAtIsNull(UUID roleId);

    Optional<Role> findByRoleCode(String roleCode);

	boolean existsByRoleCode(String roleCode);

	List<Role> findByDeletedAtIsNull();

	List<Role> findByIdInAndDeletedAtIsNull(List<UUID> roleIds);

	List<Role> findByDeletedAtIsNullAndActiveTrue();

	List<Role> findByIdInAndDeletedAtIsNullAndActiveTrue(List<UUID> roleIds);

	Optional<Role> findByIdAndDeletedAtIsNullAndActiveTrue(UUID roleId);

}
