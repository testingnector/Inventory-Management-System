package com.nector.catalogservice.dto.response.internal;

import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyWithTaxCategoriesResponse {
    private UUID companyId;
    private String companyCode;
    private String companyName;
    private Boolean active;
    private List<CompanyTaxCategoryResponse> taxCategories; // all tax categories of this company
}
