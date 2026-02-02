package com.nector.catalogservice.dto.response.internal;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonPropertyOrder({"categoryId", "categoryCode", "categoryName", "description", "displayOrder", "active", "createdAt", "company"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCCsCategoriesResponseDto1 {


    private CompanyResponseInternalDto company;
    private List<CompanyCCsCategoriesResponseDto2> categories;
}
