package com.nector.auth.dto.response.user_role;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"userId", "name", "email", "mobileNumber", "active", "companies"})
@Data
public class UserCompaniesRolesResponseDto1 {

    private UUID userId;
    private String name;
    private String email;
    private String mobileNumber;
    private Boolean active;
    
    private List<UserCompaniesRolesResponseDto2> companies = new ArrayList<>();
    
}
