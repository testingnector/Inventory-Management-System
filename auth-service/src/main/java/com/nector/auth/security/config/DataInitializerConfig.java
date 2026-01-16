package com.nector.auth.security.config;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.nector.auth.entity.Role;
import com.nector.auth.entity.User;
import com.nector.auth.entity.UserRole;
import com.nector.auth.repository.RoleRepository;
import com.nector.auth.repository.UserRepository;
import com.nector.auth.repository.UserRoleRepository;
import com.nector.auth.util.constant.SystemConstants;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializerConfig {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        UUID systemCompanyId = SystemConstants.SYSTEM_COMPANY_ID;

        // 1️⃣ Ensure SUPER_ADMIN role exists
        Role superAdminRole = roleRepository
                .findByRoleCode("SUPER_ADMIN")
                .orElseGet(() -> createSuperAdminRole(systemCompanyId));

        // 2️⃣ Ensure Super Admin user exists
        User superAdminUser = userRepository
                .findByEmail(SystemConstants.SUPER_ADMIN_EMAIL)
                .orElseGet(this::createSuperAdminUser);

        // 3️⃣ Assign role to user (user_roles)
        boolean roleAlreadyAssigned =
                userRoleRepository.existsByUserIdAndRoleId(
                        superAdminUser.getId(),
                        superAdminRole.getId()
                );

        if (!roleAlreadyAssigned) {
            UserRole userRole = new UserRole();
            userRole.setUserId(superAdminUser.getId());
            userRole.setRoleId(superAdminRole.getId());
            userRole.setCompanyId(systemCompanyId);
            userRole.setAssignedBy(superAdminUser.getId()); // self assigned

            userRoleRepository.save(userRole);
        }
    }

    private Role createSuperAdminRole(UUID companyId) {
        Role role = new Role();
        role.setRoleCode("SUPER_ADMIN");
        role.setRoleName("Super Administrator");
        role.setDescription("System Super Admin with all privileges");
        role.setSystemRole(true);
        role.setCreatedBy(companyId); // system

        return roleRepository.save(role);
    }

    private User createSuperAdminUser() {
        User user = new User();
        user.setName(SystemConstants.SUPER_ADMIN_NAME);
        user.setEmail(SystemConstants.SUPER_ADMIN_EMAIL);
        user.setMobileNumber(SystemConstants.SUPER_ADMIN_PHONE);
        user.setPasswordHash(passwordEncoder.encode(SystemConstants.SUPER_ADMIN_PASSWORD));
        user.setPasswordAlgorithm("BCRYPT");

        return userRepository.save(user);
    }
}
