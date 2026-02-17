package com.nector.catalogservice.dto.response.internal;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({ "productId", "productCode", "productName", "description", "brandName", "modelNumber",
		"variantBased", "serialized", "batchTracked", "expiryTracked", "active", "company", "variants", "taxMappings" })

@Data
public class ProductWithTaxMappingsResponse {

	private UUID productId;
	private String productCode;
	private String productName;
	private String description;
	private String brandName;
	private String modelNumber;

	private Boolean variantBased;
	private Boolean serialized;
	private Boolean batchTracked;
	private Boolean expiryTracked;
	private Boolean active;

	private CompanyResponseInternalDto company; 

	private List<ProductVariantResponse> variants; 

	private List<ProductTaxMappingSummary> taxMappings;

	@Data
	@JsonPropertyOrder({ "taxMappingId", "productId", "taxCategory" })
	public static class ProductTaxMappingSummary {
		private UUID taxMappingId;
		private UUID productId; 
		private CompanyTaxCategoryResponse taxCategory;
	}
}
