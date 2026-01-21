package com.nector.auth.dto.response.user_permission;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonPropertyOrder({ "permissionId", "permissionCode", "permissionName", "permissionDescription", "moduleName", "permissions", "active", "users" })
public class PermissionUsersGroupResponse {

    private UUID permissionId;
    private String permissionCode;
    private String permissionName;
    private String permissionDescription;
    private String moduleName;
    private Boolean active;

    private List<PermissionUserResponse> users = new ArrayList<>();
}

