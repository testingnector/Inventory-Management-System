package com.nector.catalogservice.dto.request.internal;


import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductTaxMappingBulkDeleteRequest {

    @NotEmpty(message = "At least one mapping ID must be provided")
    private List<@NotNull(message = "Mapping ID cannot be null") UUID> ids;
}

