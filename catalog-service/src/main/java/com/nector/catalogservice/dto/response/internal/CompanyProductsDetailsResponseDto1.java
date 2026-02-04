package com.nector.catalogservice.dto.response.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"companyId", "companyCode", "companyName", "active", "products"})
@Data
public class CompanyProductsDetailsResponseDto1 {

    private UUID companyId;
    private String companyCode;
    private String companyName;
    private Boolean active;

    private List<CompanyProductsDetailsResponseDto2> products = new ArrayList<>();
}
