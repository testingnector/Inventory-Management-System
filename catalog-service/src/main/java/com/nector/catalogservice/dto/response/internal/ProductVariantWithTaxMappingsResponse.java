package com.nector.catalogservice.dto.response.internal;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({ "productVariantId", "skuCode", "variantName", "color", "size", "customAttributes", "mrp",
	"sellingPrice", "purchasePrice", "serialized", "batchTracked", "expiryTracked", "active" })
@Data
public class ProductVariantWithTaxMappingsResponse {

	private UUID productVariantId;
	private String skuCode;
	private String variantName;
	private String color;
	private String size;
	private Map<String, Object> customAttributes;

	private BigDecimal mrp;
	private BigDecimal sellingPrice;
	private BigDecimal purchasePrice;

	private Boolean serialized;
	private Boolean batchTracked;
	private Boolean expiryTracked;
	private Boolean active;

	private CompanyResponseInternalDto company; 

	private ProductResponse product; 

	private List<ProductVariantTaxMappingSummary> taxMappings;

	@Data
	@JsonPropertyOrder({ "taxMappingId", "variantId", "taxCategory" })
	public static class ProductVariantTaxMappingSummary {
		private UUID taxMappingId;
		private UUID variantId; 
		private CompanyTaxCategoryResponse taxCategory;
	}
}
