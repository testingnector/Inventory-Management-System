package com.nector.orgservice.dto.response.internal;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"companyId", "companyCode", "companyName", "city", "active", "createdAt", "branches"})
@Data
public class CompanyBranchesResponseDto1 {
    private UUID companyId;
    private String companyCode;
    private String companyName;
    private String city;
    private Boolean active;

    private List<CompanyBranchesResponseDto2> branches;
}

