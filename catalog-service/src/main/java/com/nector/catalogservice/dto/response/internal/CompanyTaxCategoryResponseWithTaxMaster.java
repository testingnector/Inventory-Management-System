package com.nector.catalogservice.dto.response.internal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({ "companyTaxCategoryId", "taxRate", "hsnCode", "effectiveFrom", "effectiveTo", "active",
		"createdAt", "updatedAt", "taxMaster" })
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

	private TaxMasterResponse taxMaster;
}
