package com.nector.orgservice.dto.response.internal;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"branchId", "branchCode", "branchName", "city", "active", "company"})
@Data
public class BranchCompanyResponseDto1 {
	
    private UUID branchId;
    private String branchCode;
    private String branchName;
    private String city;
    private Boolean active;
	
	private BranchCompanyResponseDto2 company;
}
