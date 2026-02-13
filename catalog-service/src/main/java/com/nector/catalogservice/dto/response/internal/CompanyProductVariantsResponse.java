package com.nector.catalogservice.dto.response.internal;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonPropertyOrder({ "companyId", "companyCode", "companyName", "active", "variants" })
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CompanyProductVariantsResponse {

	private UUID companyId;
	private String companyCode;
	private String companyName;
	private Boolean active;

	private List<ProductVariantResponseWithProductAndUom> variants;
}
