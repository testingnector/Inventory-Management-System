package com.nector.auth.dto.response.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"userId", "name", "email", "mobileNumber", "active", "companies"})
@Data
public class UserCompaniesResponse {

    private UUID userId;
    private String name;
    private String email;
    private String mobileNumber;
    private Boolean active;
    
    private List<CompanyRolesResponse> companies = new ArrayList<>();
    
}
