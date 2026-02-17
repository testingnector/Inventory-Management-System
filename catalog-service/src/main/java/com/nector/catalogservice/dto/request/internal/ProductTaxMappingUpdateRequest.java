package com.nector.catalogservice.dto.request.internal;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductTaxMappingUpdateRequest {

    @NotNull(message = "Company Tax Category ID is required")
    private UUID companyTaxCategoryId;
}

