package com.nector.catalogservice.dto.response.internal;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonPropertyOrder({ "productImageId", "imageUrl", "imageType", "altText", "primary", "displayOrder", "active",
		"company", "product", "productVariant" })
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductImageResponseWithCompanyProductProductVariant {

	private UUID productImageId;
	private String imageUrl;
	private String imageType;
	private String altText;
	private Boolean primary;
	private Integer displayOrder;
	private Boolean active;

	private CompanyResponseInternalDto company;
	private ProductResponse product;
	private ProductVariantResponse productVariant;

}
