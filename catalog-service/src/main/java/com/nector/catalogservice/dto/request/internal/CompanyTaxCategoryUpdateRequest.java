package com.nector.catalogservice.dto.request.internal;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

@Data
public class CompanyTaxCategoryUpdateRequest {

    @DecimalMin(value = "0.0", inclusive = true, message = "taxRate must be >= 0")
    private Double taxRate;

    private String hsnCode;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectiveTo;

    private Boolean active;

}
