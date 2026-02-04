package com.nector.catalogservice.dto.request.internal;

import java.util.UUID;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductUpdateRequest {

    @Size(max = 100, message = "Product name must not exceed 100 characters")
    private String productName;

    @Size(max = 300, message = "Description must not exceed 300 characters")
    private String description;

    @Size(max = 70, message = "Brand name must not exceed 70 characters")
    private String brandName;

    @Size(max = 100, message = "Model number must not exceed 100 characters")
    private String modelNumber;

    private UUID categoryId;
    private UUID subCategoryId;

    private Boolean variantBased;
    private Boolean serialized;
    private Boolean batchTracked;
    private Boolean expiryTracked;
    
    private Boolean active;
}
