package com.nector.auth.dto.response.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;
@JsonPropertyOrder({"roleId", "roleCode", "roleName", "active", "companies"})
@Data
public class RoleCompaniesUsersResponseDto1 {

    private UUID roleId;
    private String roleCode;
    private String roleName;
    private Boolean active;
    private List<RoleCompaniesUsersResponseDto2> companies = new ArrayList<>();
}
