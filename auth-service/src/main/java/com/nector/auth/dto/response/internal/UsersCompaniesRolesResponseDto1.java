package com.nector.auth.dto.response.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"userId", "name", "email", "mobileNumber", "active", "companies"})
@Data
public class UsersCompaniesRolesResponseDto1 {

    private UUID userId;
    private String name;
    private String email;
    private String mobileNumber;
    private Boolean active;
    
    private List<UsersCompaniesRolesResponseDto2> companies = new ArrayList<>();
}
