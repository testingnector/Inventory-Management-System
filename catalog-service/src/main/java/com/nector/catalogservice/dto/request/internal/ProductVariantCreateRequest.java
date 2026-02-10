package com.nector.catalogservice.dto.request.internal;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantCreateRequest {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    @NotBlank(message = "SKU code is required")
    private String skuCode;

    @NotBlank(message = "Variant name is required")
    private String variantName;

    private String color;

    private String size;

    private String customAttributes; 

    @NotNull(message = "MRP is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "MRP cannot be negative")
    private BigDecimal mrp;

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Selling price cannot be negative")
    private BigDecimal sellingPrice;

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Purchase price cannot be negative")
    private BigDecimal purchasePrice;

    @NotNull(message = "UOM ID is required")
    private UUID uomId;

    @NotNull(message = "Conversion factor is required")
    @Positive(message = "Conversion factor must be greater than 0")
    private BigDecimal conversionFactor;

    private Boolean serialized = false;
    private Boolean batchTracked = false;
    private Boolean expiryTracked = false;

}
