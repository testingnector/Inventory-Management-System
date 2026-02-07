package com.nector.orgservice.dto.response.internal;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"companyId", "companyCode", "companyName", "city", "active", "createdAt"})
@Data
public class CompanyBranchResponse {

	private UUID companyId;
	private String companyCode;
	private String companyName;
	private String city;
	private Boolean active;
	
	private BranchResponse branch;
}
