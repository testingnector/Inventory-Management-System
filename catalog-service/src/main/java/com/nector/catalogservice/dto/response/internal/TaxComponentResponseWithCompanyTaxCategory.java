package com.nector.catalogservice.dto.response.internal;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.nector.catalogservice.enums.TaxComponentType;

import lombok.Data;

@JsonPropertyOrder({ "taxComponentId", "companyTaxCategoryId", "componentType", "componentRate", "active", "createdAt", "updatedAt", "companyTaxCategory" })
@Data
public class TaxComponentResponseWithCompanyTaxCategory {
	private UUID taxComponentId;
	private TaxComponentType componentType;
	private Double componentRate;
	private Boolean active;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private CompanyTaxCategoryResponse companyTaxCategory;
}
