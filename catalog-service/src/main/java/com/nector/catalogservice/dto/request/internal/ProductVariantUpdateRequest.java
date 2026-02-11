package com.nector.catalogservice.dto.request.internal;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantUpdateRequest {

    @Size(max = 100, message = "Variant name must be at most 100 characters")
    private String variantName;

    @Size(max = 30, message = "Color must be at most 30 characters")
    private String color;

    @Size(max = 20, message = "Size must be at most 20 characters")
    private String size;

    private Map<String, Object> customAttributes;

    @DecimalMin(value = "0.0", inclusive = true, message = "MRP cannot be negative")
    @Digits(integer = 13, fraction = 2, message = "MRP must have up to 13 digits and 2 decimal places")
    private BigDecimal mrp;

    @DecimalMin(value = "0.0", inclusive = true, message = "Selling price cannot be negative")
    @Digits(integer = 13, fraction = 2, message = "Selling price must have up to 13 digits and 2 decimal places")
    private BigDecimal sellingPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Purchase price cannot be negative")
    @Digits(integer = 13, fraction = 2, message = "Purchase price must have up to 13 digits and 2 decimal places")
    private BigDecimal purchasePrice;

    private UUID uomId;

    @DecimalMin(value = "0.0", inclusive = false, message = "Conversion factor must be greater than 0")
    @Digits(integer = 6, fraction = 4, message = "Conversion factor must have up to 6 digits and 4 decimal places")
    private BigDecimal conversionFactor;

    private Boolean serialized;
    private Boolean batchTracked;
    private Boolean expiryTracked;
    private Boolean active;
}
