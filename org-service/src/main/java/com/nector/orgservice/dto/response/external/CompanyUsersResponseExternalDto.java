package com.nector.orgservice.dto.response.external;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"userId", "name", "email", "mobileNumber", "active"})
@Data
public class CompanyUsersResponseExternalDto {
    private UUID userId;
    private String name;
    private String email;
    private String mobileNumber;
    private Boolean active;
}
