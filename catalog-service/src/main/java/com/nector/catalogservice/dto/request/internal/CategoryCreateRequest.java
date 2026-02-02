package com.nector.catalogservice.dto.request.internal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
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
    @Size(max = 70, message = "Category name must be at most 100 characters")
    private String categoryName;

    @Size(max = 100, message = "Description must be at most 255 characters")
    private String description;

    private Integer displayOrder;

}
