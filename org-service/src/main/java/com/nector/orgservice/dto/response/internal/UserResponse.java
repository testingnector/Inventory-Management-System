package com.nector.orgservice.dto.response.internal;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"userId", "name", "email", "mobileNumber", "userIsActive", "userRoleId", "assignedIsActive", "assignedAt", "revokedAt"})
@Data
public class UserResponse {

    private UUID userId;
    private String name;
    private String email;
    private String mobileNumber;
    private Boolean userIsActive;
}
