package com.nector.productcatalogservice.dto.request.internal;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateRequest {

    @NotBlank(message = "Category code is mandatory")
    @Size(max = 50, message = "Category code must be at most 50 characters")
    private String categoryCode;

    @NotBlank(message = "Category name is mandatory")
    @Size(max = 100, message = "Category name must be at most 100 characters")
    private String categoryName;

    @NotNull(message = "Company ID is mandatory")
    private UUID companyId;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;

    private Integer displayOrder;

}
