package com.nector.orgservice.dto.response.internal;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"branchId", "branchCode", "branchName", "city", "active", "createdAt"})
@Data
public class CompanyBranchesResponseDto2 {

    private UUID branchId;
    private String branchCode;
    private String branchName;
    private String city;
    private Boolean active;
    
	private LocalDateTime createdAt;
        
}