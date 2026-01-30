package com.nector.productcatalogservice.dto.response.internal;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"categoryId", "categoryCode", "categoryName", "description", "displayOrder", "active", "createdAt"})
@Data
public class CompanyCategoriesResponseDto2 {

    private UUID categoryId;
    private String categoryCode;
    private String categoryName;
    private String description;
    private Integer displayOrder;
    private Boolean active;
    private LocalDateTime createdAt;
}
