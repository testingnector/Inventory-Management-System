package com.nector.catalogservice.dto.request.internal;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantBulkUpdateItem {

    @NotNull(message = "Variant ID is required")
    private UUID variantId;

    @Size(max = 100, message = "Variant name must be at most 100 characters")
    private String variantName;

    @Size(max = 30, message = "Color must be at most 30 characters")
    private String color;

    @Size(max = 20, message = "Size must be at most 20 characters")
    private String size;

    private Map<String, Object> customAttributes;

    @DecimalMin(value = "0.0", inclusive = true, message = "MRP cannot be negative")
    private BigDecimal mrp;

    @DecimalMin(value = "0.0", inclusive = true, message = "Selling price cannot be negative")
    private BigDecimal sellingPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Purchase price cannot be negative")
    private BigDecimal purchasePrice;

    private Boolean serialized;
    private Boolean batchTracked;
    private Boolean expiryTracked;
    private Boolean active;
}
