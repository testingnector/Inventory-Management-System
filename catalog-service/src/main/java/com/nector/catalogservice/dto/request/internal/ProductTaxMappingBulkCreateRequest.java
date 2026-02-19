package com.nector.catalogservice.dto.request.internal;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductTaxMappingBulkCreateRequest {

	@Valid
    private List<ProductTaxMappingCreateRequest> productTaxMapping;
}

