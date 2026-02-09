package com.nector.catalogservice.dto.response.internal;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({ "companyTaxCategoryId", "taxRate", "hsnCode", "effectiveFrom", "effectiveTo", "active",
		"taxMaster", "company", "taxComponents" })
@Data
public class CompanyTaxCategoryWithComponentsResponse {

	private UUID companyTaxCategoryId;
	private Double taxRate;
	private String hsnCode;
	private LocalDate effectiveFrom;
	private LocalDate effectiveTo;
	private Boolean active;

	private TaxMasterResponse taxMaster;
	private CompanyResponseInternalDto company;

	private List<TaxComponentResponse> taxComponents;
}
