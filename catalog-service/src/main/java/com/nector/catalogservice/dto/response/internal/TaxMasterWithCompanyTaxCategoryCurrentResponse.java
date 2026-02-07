package com.nector.catalogservice.dto.response.internal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({ "taxMasterId", "taxCode", "taxName", "taxType", "compoundTax", "description", "active",
		"createdAt", "updatedAt", "companyTaxCategory" })
@Data
public class TaxMasterWithCompanyTaxCategoryCurrentResponse {

	private UUID taxMasterId;
	private String taxCode;
	private String taxName;
	private String taxType;
	private Boolean compoundTax;
	private String description;
	private Boolean active;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	private CompanyTaxCategoryResponse companyTaxCategory;
}
