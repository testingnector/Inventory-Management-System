package com.nector.catalogservice.dto.response.internal;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.nector.catalogservice.enums.TaxComponentType;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@JsonPropertyOrder({ "taxComponentId", "companyTaxCategoryId", "componentType", "componentRate", "active" })
@Setter
@Getter
public class TaxComponentResponse {
	private UUID taxComponentId;
	private TaxComponentType componentType;
	private Double componentRate;
	private Boolean active;
	
	
}
