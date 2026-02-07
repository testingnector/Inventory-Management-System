package com.nector.catalogservice.dto.response.internal;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"company", "taxMaster", "companyTaxCategoryHistory"})
@Data
public class CompanyTaxMasterCompanyTaxCategoryHistory {

	private CompanyResponseInternalDto company;
	private TaxMasterResponse taxMaster;
	
	private List<CompanyTaxCategoryResponse> companyTaxCategoryHistory;
	
}
