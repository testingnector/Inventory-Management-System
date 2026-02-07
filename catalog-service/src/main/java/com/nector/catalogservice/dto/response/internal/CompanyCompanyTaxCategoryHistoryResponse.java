package com.nector.catalogservice.dto.response.internal;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"company", "taxMaster"})
@Data
public class CompanyCompanyTaxCategoryHistoryResponse {
    private CompanyResponseInternalDto company;
    private List<TaxMasterWithCompanyTaxCategoryHistoryResponse> taxMaster;
}
