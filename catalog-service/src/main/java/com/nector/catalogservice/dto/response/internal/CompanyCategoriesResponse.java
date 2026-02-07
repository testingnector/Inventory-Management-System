package com.nector.catalogservice.dto.response.internal;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonPropertyOrder({"company", "categories"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyCategoriesResponse {


    private CompanyResponseInternalDto company;
    private List<CompanyCategoryCategoryResponse> categories;
}
