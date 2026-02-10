package com.nector.catalogservice.dto.response.internal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({ "companyTaxCategoryId", "taxRate", "hsnCode", "effectiveFrom", "effectiveTo", "active",
		"createdAt", "updatedAt", "components", "taxMaster" })
@Data
public class CompanyTaxCategoryResponseWithTaxMaster {

	private UUID companyTaxCategoryId;
	private Double taxRate;
	private String hsnCode;
	private LocalDate effectiveFrom;
	private LocalDate effectiveTo;
	private Boolean active;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	private List<TaxComponentResponse> components;

	private TaxMasterResponse taxMaster;
}
