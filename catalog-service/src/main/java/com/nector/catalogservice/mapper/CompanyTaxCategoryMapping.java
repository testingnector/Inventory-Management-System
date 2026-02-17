package com.nector.catalogservice.mapper;

import java.time.LocalDate;
import java.util.List;

import com.nector.catalogservice.dto.response.internal.CompanyTaxCategoryResponse;
import com.nector.catalogservice.dto.response.internal.TaxComponentResponse;
import com.nector.catalogservice.entity.CompanyTaxCategory;

public class CompanyTaxCategoryMapping {

	public static CompanyTaxCategoryResponse mapToCompanyTaxCategoryResponse(CompanyTaxCategory companyTaxCategory,
			List<TaxComponentResponse> taxComponents) {
		
		if (companyTaxCategory == null) {
			return null;
		}
		if (taxComponents == null) {
			return null;
		}
		
		CompanyTaxCategoryResponse response = new CompanyTaxCategoryResponse();
		response.setCompanyTaxCategoryId(companyTaxCategory.getId());
		response.setTaxRate(companyTaxCategory.getTaxRate());
		response.setHsnCode(companyTaxCategory.getHsnCode());
		response.setEffectiveFrom(companyTaxCategory.getEffectiveFrom());
		response.setEffectiveTo(companyTaxCategory.getEffectiveTo());
		boolean calculatedActive = isCurrentlyActive(companyTaxCategory, LocalDate.now());
		response.setActive(calculatedActive);
		response.setCreatedAt(companyTaxCategory.getCreatedAt());
		response.setUpdatedAt(companyTaxCategory.getUpdatedAt());
		response.setComponents(taxComponents);
		
		return response;
	}
	
	private static boolean isCurrentlyActive(CompanyTaxCategory c, LocalDate today) {
		return Boolean.TRUE.equals(c.getActive())
				&& (c.getEffectiveFrom() == null || today.isAfter(c.getEffectiveFrom()))
				&& (c.getEffectiveTo() == null || today.isBefore(c.getEffectiveTo()));
	}

	
}
