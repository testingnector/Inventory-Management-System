package com.nector.orgservice.dto.response.internal;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"branchId", "branchCode", "branchName", "city", "address", "active", "headOffice", "company"})
@Data
public class BranchCompanyResponse {
	
    private UUID branchId;
    private String branchCode;
    private String branchName;
    private String city;
    private String address;
    private Boolean active;
    private Boolean headOffice;
    
	
	private CompanyBriefResponse company;
}
