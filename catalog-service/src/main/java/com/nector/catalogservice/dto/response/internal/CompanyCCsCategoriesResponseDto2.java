package com.nector.catalogservice.dto.response.internal;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonPropertyOrder({"companyCategoryId", "active", "createdAt", "deletedAt", "category"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCCsCategoriesResponseDto2 {

    private UUID companyCategoryId;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
    private CategoryResponse category;

}
