package com.nector.catalogservice.dto.request.internal;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompanyTaxCategoryCreateRequest {

    @NotNull(message = "taxMasterId is required")
    private UUID taxMasterId;

    @NotNull(message = "companyId is required")
    private UUID companyId;

    @NotNull(message = "taxRate is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "taxRate must be >= 0")
    private Double taxRate;

    private String hsnCode;

    @NotNull(message = "effectiveFrom date is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

}
