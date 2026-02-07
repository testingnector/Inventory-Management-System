package com.nector.catalogservice.dto.response.internal;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.Setter;

@JsonPropertyOrder({ "companyId", "companyCode", "companyName", "active", "taxCategories"})
@Getter
@Setter
public class CompanyWithTaxCategoriesResponse {
    private UUID companyId;
    private String companyCode;
    private String companyName;
    private Boolean active;
    private List<CompanyTaxCategoryResponseWithTaxMaster> taxCategories; 
}
