package com.nector.catalogservice.dto.response.internal;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"companyCategoryId", "active", "createdAt", "company", "category"})
@Data
public class Company_CategoryResponse {

    private UUID companyCategoryId;
    private Boolean active;
    private LocalDateTime createdAt;
    
    private CompanyResponseInternalDto company;
    private CategoryResponse category;

}
