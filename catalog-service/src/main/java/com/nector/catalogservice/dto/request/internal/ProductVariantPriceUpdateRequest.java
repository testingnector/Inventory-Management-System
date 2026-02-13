package com.nector.catalogservice.dto.request.internal;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductVariantPriceUpdateRequest {

	@NotNull(message = "MRP is required")
	@DecimalMin(value = "0.0", inclusive = true, message = "MRP cannot be negative")
	private BigDecimal mrp;

	@NotNull(message = "Selling price is required")
	@DecimalMin(value = "0.0", inclusive = true, message = "Selling price cannot be negative")
	private BigDecimal sellingPrice;

	@NotNull(message = "Purchase price is required")
	@DecimalMin(value = "0.0", inclusive = true, message = "Purchase price cannot be negative")
	private BigDecimal purchasePrice;
}
