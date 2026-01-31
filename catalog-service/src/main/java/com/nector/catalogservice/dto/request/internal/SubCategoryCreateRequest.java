package com.nector.catalogservice.dto.request.internal;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubCategoryCreateRequest {

    @NotBlank(message = "SubCategory code is mandatory")
    @Size(max = 50, message = "SubCategory code must be at most 50 characters")
    private String subCategoryCode;

    @NotBlank(message = "SubCategory name is mandatory")
    @Size(max = 150, message = "SubCategory name must be at most 150 characters")
    private String subCategoryName;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @Size(max = 500, message = "Description must be at most 500 characters")
    private String description;

    private Integer displayOrder;

}
