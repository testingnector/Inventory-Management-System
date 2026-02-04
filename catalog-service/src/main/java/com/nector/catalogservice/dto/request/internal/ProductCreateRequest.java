package com.nector.catalogservice.dto.request.internal;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductCreateRequest {


    @NotBlank(message = "Product code is required")
    @Size(max = 30, message = "Product code must not exceed 30 characters")
    private String productCode;

    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Product name must not exceed 100 characters")
    private String productName;

    @NotNull(message = "Company ID is required")
    private UUID companyId;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    private UUID subCategoryId;

    @Size(max = 300, message = "Description must not exceed 300 characters")
    private String description;

    @Size(max = 70, message = "Brand name must not exceed 70 characters")
    private String brandName;

    @Size(max = 100, message = "Model number must not exceed 100 characters")
    private String modelNumber;

    private Boolean variantBased;
    private Boolean serialized;
    private Boolean batchTracked;
    private Boolean expiryTracked;

}

