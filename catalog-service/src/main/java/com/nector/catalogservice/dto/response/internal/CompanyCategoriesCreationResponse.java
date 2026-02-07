package com.nector.catalogservice.dto.response.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"companyId", "companyCode", "companyName", "active", "categories"})
@Data
public class CompanyCategoriesCreationResponse {

    private UUID companyId;
    private String companyCode;
    private String companyName;
    private Boolean active;

    private List<CategoryResponse> categories = new ArrayList<>();
}