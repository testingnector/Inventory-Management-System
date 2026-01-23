package com.nector.orgservice.dto.response.external;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"companyId", "companyCode", "companyName", "active"})
@Data
public class CompanyBasicResponse {
    private UUID companyId;
    private String companyCode;
    private String companyName;
    private Boolean active;

}

