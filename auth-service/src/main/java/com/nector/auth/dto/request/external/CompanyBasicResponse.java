package com.nector.auth.dto.request.external;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonPropertyOrder({"companyId", "companyCode", "companyName", "active"})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyBasicResponse {
    private UUID companyId;
    private String companyCode;
    private String companyName;
    private Boolean active;
}
