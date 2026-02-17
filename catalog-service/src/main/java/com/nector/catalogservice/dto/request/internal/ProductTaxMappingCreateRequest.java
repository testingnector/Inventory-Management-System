package com.nector.catalogservice.dto.request.internal;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@JsonPropertyOrder({"companyId", "productId", "productVariantId", "companyTaxCategoryId"})
@Getter
@Setter
public class ProductTaxMappingCreateRequest {

	@NotNull(message = "Company is required")
    private UUID companyId;
	
    private UUID productId;
    
    private UUID productVariantId;
    
    @NotNull(message = "Tax category is required")
    private UUID companyTaxCategoryId;
}
