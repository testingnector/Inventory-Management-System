package com.nector.catalogservice.dto.request.internal;

import java.util.UUID;

import com.nector.catalogservice.enums.TaxComponentType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class CreateTaxComponentRequest {

    @NotNull(message = "Company tax category is mandatory")
    private UUID companyTaxCategoryId;

    @NotNull(message = "Component type is mandatory")
    private TaxComponentType componentType;

    @NotNull(message = "Component rate is mandatory")
    @PositiveOrZero
    private Double componentRate;
}
