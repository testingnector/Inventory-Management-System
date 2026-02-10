package com.nector.catalogservice.dto.response.internal;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonPropertyOrder({ "skuCode", "variantName", "color", "size", "customAttributes", "mrp", "sellingPrice", "serialized",
		"batchTracked", "expiryTracked", "active", "product", "company", "uom" })
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantResponse {

	private String skuCode;
	private String variantName;
	private String color;
	private String size;
	private String customAttributes;

	private Double mrp;
	private Double sellingPrice;

	private Boolean serialized;
	private Boolean batchTracked;
	private Boolean expiryTracked;
	private Boolean active;

	private ProductResponse product;
	private CompanyResponseInternalDto company;
	private UomResponse uom;
}
