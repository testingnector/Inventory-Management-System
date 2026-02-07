package com.nector.auth.dto.response.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.nector.auth.dto.response.external.CompanyResponseExternalDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonPropertyOrder({"companyId", "companyCode", "companyName", "active"})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyResponseDto {
    private UUID companyId;
    private String companyCode;
    private String companyName;
    private Boolean active;
    
    private List<RoleAndUserRoleResponse> roles = new ArrayList<>();

}

