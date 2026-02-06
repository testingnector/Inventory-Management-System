package com.nector.catalogservice.dto.response.internal;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonPropertyOrder({ "companyTaxCategoryId", "taxRate", "hsnCode", "effectiveFrom",
	"effectiveTo", "active", "taxMaster"})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyTaxCategoryResponseByCompany {
    private UUID companyTaxCategoryId;
    private Double taxRate;
    private String hsnCode;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Boolean active;
    private TaxMasterResponse taxMaster;
}
