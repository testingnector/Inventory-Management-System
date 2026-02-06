package com.nector.catalogservice.dto.request.internal;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class CompanyTaxCategoryUpdateRequest {

    @DecimalMin(value = "0.0", inclusive = true, message = "taxRate must be >= 0")
    private Double taxRate;

    private String hsnCode;

    private LocalDate effectiveTo;

    private Boolean active;

}
