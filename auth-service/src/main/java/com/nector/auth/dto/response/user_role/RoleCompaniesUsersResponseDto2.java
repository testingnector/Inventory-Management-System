package com.nector.auth.dto.response.user_role;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"companyId", "companyCode", "companyName", "active", "users"})
@Data
public class RoleCompaniesUsersResponseDto2 {

    private UUID companyId;
    private String companyCode;
    private String companyName;
    private Boolean active;

    private List<RoleCompaniesUsersResponseDto3> users = new ArrayList<>();
}
