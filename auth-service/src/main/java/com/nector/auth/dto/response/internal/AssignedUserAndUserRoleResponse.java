package com.nector.auth.dto.response.internal;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"userId", "name", "email", "mobileNumber", "userIsActive", "userRoleId", "assignedIsActive", "assignedAt", "revokedAt"})
@Data
public class AssignedUserAndUserRoleResponse {

    private UUID userId;
    private String name;
    private String email;
    private String mobileNumber;
    private Boolean userIsActive;
    
    private UUID userRoleId;
    private Boolean assignedIsActive;
    private LocalDateTime assignedAt;
    private LocalDateTime revokedAt;
}
