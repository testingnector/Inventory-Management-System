package com.nector.auth.dto.response.user_permission;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonPropertyOrder({ "userId", "name", "email", "mobileNumber", "active", "permissions" })
public class UserPermissionGroupResponse {

    private UUID userId;
    private String name;
    private String email;
    private String mobileNumber;
    private Boolean active;

    private List<UserPermissionResponse> permissions = new ArrayList<>();
}
