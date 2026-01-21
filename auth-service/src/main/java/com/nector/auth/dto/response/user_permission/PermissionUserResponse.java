package com.nector.auth.dto.response.user_permission;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonPropertyOrder({ "userId", "name", "email", "mobileNumber", "userIsActive", "allowed", "assignedActive", "assignedAt" })
public class PermissionUserResponse {

    private UUID userId;
    private String name;
    private String email;
    private String mobileNumber;
    private Boolean userIsActive;
    
    private Boolean allowed;
    private Boolean assignedActive;
    private LocalDateTime assignedAt;
}
