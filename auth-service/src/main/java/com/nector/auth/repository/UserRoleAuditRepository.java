package com.nector.auth.repository;


import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nector.auth.entity.UserRoleAudit;

public interface UserRoleAuditRepository extends JpaRepository<UserRoleAudit, UUID> {
}

