package com.nector.catalogservice.dto.response.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CompanyTaxMappingsPageResponse {

    private CompanyResponseInternalDto company;

    private PaginatedResponse<ProductTaxMappingResponseWithCompanyProductProductVariantCompanyTaxCategory> taxMappings;
}
