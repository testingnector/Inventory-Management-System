package com.nector.catalogservice.mapper;

import java.util.List;
import java.util.UUID;

import com.nector.catalogservice.dto.request.internal.ProductTaxMappingCreateRequest;
import com.nector.catalogservice.dto.response.external.CompanyResponseExternalDto;
import com.nector.catalogservice.dto.response.internal.CompanyResponseInternalDto;
import com.nector.catalogservice.dto.response.internal.CompanyTaxCategoryResponse;
import com.nector.catalogservice.dto.response.internal.ProductResponse;
import com.nector.catalogservice.dto.response.internal.ProductTaxMappingResponseWithCompanyProductProductVariantCompanyTaxCategory;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponse;
import com.nector.catalogservice.dto.response.internal.ProductWithTaxMappingsResponse;
import com.nector.catalogservice.dto.response.internal.ProductWithTaxMappingsResponse.ProductTaxMappingSummary;
import com.nector.catalogservice.entity.Product;
import com.nector.catalogservice.entity.ProductTaxMapping;

import jakarta.validation.Valid;

public class ProductTaxMappingMap {

	public static ProductTaxMapping mapToEntityFromCreationDto(@Valid ProductTaxMappingCreateRequest request,
			UUID createdBy) {

		ProductTaxMapping entity = new ProductTaxMapping();
		entity.setCompanyId(request.getCompanyId());
		entity.setProductId(request.getProductId());
		entity.setProductVariantId(request.getProductVariantId());
		entity.setCompanyTaxCategoryId(request.getCompanyTaxCategoryId());
		entity.setCreatedBy(createdBy);

		return entity;

	}

	public static CompanyResponseInternalDto mapToCompanyResponseInternalDto(
			CompanyResponseExternalDto companyResponse) {
		return ProductVariantsMapping.mapToCompanyResponseInternalDto(companyResponse);

	}

	public static ProductTaxMappingResponseWithCompanyProductProductVariantCompanyTaxCategory mapToProductTaxMappingResponseWithCompanyProductProductVariantCompanyTaxCategory(
			ProductTaxMapping entity, CompanyResponseInternalDto company, ProductResponse productResponse,
			ProductVariantResponse productVariantResponse, CompanyTaxCategoryResponse companyTaxCategoryResponse) {

		ProductTaxMappingResponseWithCompanyProductProductVariantCompanyTaxCategory response = new ProductTaxMappingResponseWithCompanyProductProductVariantCompanyTaxCategory();
		response.setProductTaxMappingId(entity.getId());
		response.setCompany(company);
		response.setProduct(productResponse);
		response.setVariant(productVariantResponse);
		response.setCompanyTaxCategory(companyTaxCategoryResponse);

		return response;

	}

	public static ProductWithTaxMappingsResponse mapToProductWithTaxMappingsResponse(Product product, CompanyResponseInternalDto company,
			List<ProductVariantResponse> variantResponses, List<ProductTaxMappingSummary> taxMappingSummaries) {
		
		ProductWithTaxMappingsResponse finalResponse = new ProductWithTaxMappingsResponse();
		finalResponse.setProductId(product.getId());
		finalResponse.setProductCode(product.getProductCode());
		finalResponse.setProductName(product.getProductName());
		finalResponse.setDescription(product.getDescription());
		finalResponse.setBrandName(product.getBrandName());
		finalResponse.setModelNumber(product.getModelNumber());
		finalResponse.setVariantBased(product.getVariantBased());
		finalResponse.setSerialized(product.getSerialized());
		finalResponse.setBatchTracked(product.getBatchTracked());
		finalResponse.setExpiryTracked(product.getExpiryTracked());
		finalResponse.setActive(product.getActive());
		finalResponse.setCompany(company);
		finalResponse.setVariants(variantResponses);
		finalResponse.setTaxMappings(taxMappingSummaries);
		
		return finalResponse;
		
	}

}
