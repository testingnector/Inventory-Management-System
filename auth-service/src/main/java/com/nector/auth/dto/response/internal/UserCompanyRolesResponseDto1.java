package com.nector.auth.dto.response.internal;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"userId", "name", "email", "mobileNumber", "active"})
@Data
public class UserCompanyRolesResponseDto1 {

    private UUID userId;
    private String name;
    private String email;
    private String mobileNumber;
    private Boolean active;
    
    private UserCompanyRolesResponseDto2 company; //SINGLE COMPANY
}
