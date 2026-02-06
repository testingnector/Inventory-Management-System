package com.nector.catalogservice.dto.response.internal;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({ "company", "taxCategories", "page"})
@Data
public class CompanyTaxCategoryPageResponse {
	
	CompanyResponseInternalDto company;
	List<CompanyTaxCategoryResponseByCompany> taxCategories;
	PageMeta page;
}
