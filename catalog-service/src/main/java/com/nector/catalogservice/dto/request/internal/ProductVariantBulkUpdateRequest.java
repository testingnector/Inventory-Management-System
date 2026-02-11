package com.nector.catalogservice.dto.request.internal;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantBulkUpdateRequest {

    @NotEmpty(message = "Variants list cannot be empty")
    @Valid
    private List<ProductVariantBulkUpdateItem> variants;
}
