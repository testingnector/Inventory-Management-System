package com.nector.catalogservice.dto.request.internal;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryUpdateRequest {

    @Size(max = 100, message = "Category name must be at most 100 characters")
    private String categoryName;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;

    private Integer displayOrder;

    private Boolean active;
}

