package com.nector.catalogservice.dto.response.internal;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductTaxMappingResponseWithCompanyProductProductVariantCompanyTaxCategory {

    private UUID productTaxMappingId;
    
    private CompanyResponseInternalDto company;
    private ProductResponse product;
    private ProductVariantResponse variant;
    private CompanyTaxCategoryResponse companyTaxCategory;
    
}

