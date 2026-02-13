package com.nector.catalogservice.dto.response.internal;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonPropertyOrder({ "productVariantId", "skuCode", "variantName", "color", "size", "customAttributes", "mrp",
		"sellingPrice", "purchasePrice", "serialized", "batchTracked", "expiryTracked", "active", "product", "company",
		"uom" })
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantResponse {

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

	private ProductResponse product;
	private CompanyResponseInternalDto company;
	private UomResponse uom;
}
