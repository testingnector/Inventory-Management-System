package com.nector.orgservice.dto.response.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"companyId", "companyCode", "companyName", "active", "users"})
@Data
public class CompanyUsersResponseDto1 {

    private UUID companyId;
    private String companyCode;
    private String companyName;
    private Boolean active;

    private List<CompanyUsersResponseDto2> users = new ArrayList<>();
}
