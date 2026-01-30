package com.nector.productcatalogservice.dto.response.internal;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"companyId", "companyCode", "companyName", "active", "categories"})
@Data
public class CompanyCategoriesResponseDto1 {

    private UUID companyId;
    private String companyCode;
    private String companyName;
    private Boolean active;
    
    private List<CompanyCategoriesResponseDto2> categories;
}
