package com.nector.auth.dto.response.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"companyId", "companyCode", "companyName", "active", "roles"})
@Data
public class CompanyRolesResponse {

	private UUID companyId;
    private String companyCode;
    private String companyName;
    private Boolean active;

    private List<RoleAndUserRoleResponse> roles = new ArrayList<>();
}
