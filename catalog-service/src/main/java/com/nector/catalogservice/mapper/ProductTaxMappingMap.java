package com.nector.catalogservice.mapper;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.nector.catalogservice.dto.request.internal.ProductTaxMappingCreateRequest;
import com.nector.catalogservice.dto.response.external.CompanyResponseExternalDto;
import com.nector.catalogservice.dto.response.internal.CompanyResponseInternalDto;
import com.nector.catalogservice.dto.response.internal.CompanyTaxCategoryResponse;
import com.nector.catalogservice.dto.response.internal.CompanyTaxCategoryWithComponentsCompanyProductsVariants;
import com.nector.catalogservice.dto.response.internal.PageMeta;
import com.nector.catalogservice.dto.response.internal.ProductResponse;
import com.nector.catalogservice.dto.response.internal.ProductTaxMappingResponseWithCompanyProductProductVariantCompanyTaxCategory;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponse;
import com.nector.catalogservice.dto.response.internal.ProductVariantWithTaxMappingsResponse;
import com.nector.catalogservice.dto.response.internal.ProductVariantWithTaxMappingsResponse.ProductVariantTaxMappingSummary;
import com.nector.catalogservice.dto.response.internal.ProductWithTaxMappingsResponse;
import com.nector.catalogservice.dto.response.internal.ProductWithTaxMappingsResponse.ProductTaxMappingSummary;
import com.nector.catalogservice.dto.response.internal.TaxComponentResponse;
import com.nector.catalogservice.entity.CompanyTaxCategory;
import com.nector.catalogservice.entity.Product;
import com.nector.catalogservice.entity.ProductTaxMapping;
import com.nector.catalogservice.entity.ProductVariant;

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

	public static ProductWithTaxMappingsResponse mapToProductWithTaxMappingsResponse(Product product,
			CompanyResponseInternalDto company, List<ProductVariantResponse> variantResponses,
			List<ProductTaxMappingSummary> taxMappingSummaries) {

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

	public static ProductVariantWithTaxMappingsResponse mapToProductWithTaxMappingsResponseForVariant(
			ProductVariant variant, CompanyResponseInternalDto company, ProductResponse productResponse,
			List<ProductVariantTaxMappingSummary> taxMappingSummaries) {

		ProductVariantWithTaxMappingsResponse response = new ProductVariantWithTaxMappingsResponse();
		response.setProductVariantId(variant.getId());
		response.setSkuCode(variant.getSkuCode());
		response.setVariantName(variant.getVariantName());
		response.setColor(variant.getColor());
		response.setSize(variant.getSize());
		response.setCustomAttributes(variant.getCustomAttributes());

		response.setMrp(variant.getMrp());
		response.setSellingPrice(variant.getSellingPrice());
		response.setPurchasePrice(variant.getPurchasePrice());

		response.setSerialized(variant.getSerialized());
		response.setBatchTracked(variant.getBatchTracked());
		response.setExpiryTracked(variant.getExpiryTracked());
		response.setActive(variant.getActive());

		response.setCompany(company);
		response.setProduct(productResponse);
		response.setTaxMappings(taxMappingSummaries);

		return response;
	}

	public static ProductResponse mapToProductResponse(Product product) {
		return ProductVariantsMapping.mapToProductResponse(product);

	}

	public static Object CompanyTaxCategoryWithComponentsCompanyProductsVariants(ProductTaxMapping mapping,
			CompanyResponseInternalDto company, List<ProductResponse> productResponses, List<ProductVariantResponse> productVariantResponses,
			CompanyTaxCategoryResponse companyTaxCategory) {
		
		
		CompanyTaxCategoryWithComponentsCompanyProductsVariants response = new CompanyTaxCategoryWithComponentsCompanyProductsVariants();
		response.setCompanyTaxCategoryId(companyTaxCategory.getCompanyTaxCategoryId());
		response.setTaxRate(companyTaxCategory.getTaxRate());
		response.setHsnCode(companyTaxCategory.getHsnCode());
		response.setEffectiveFrom(companyTaxCategory.getEffectiveFrom());
		response.setEffectiveTo(companyTaxCategory.getEffectiveTo());
		response.setActive(companyTaxCategory.getActive());
		response.setCreatedAt(companyTaxCategory.getCreatedAt());
		response.setUpdatedAt(companyTaxCategory.getUpdatedAt());
		response.setComponents(companyTaxCategory.getComponents());

		response.setCompany(company);
		response.setProducts(productResponses);
		response.setVariants(productVariantResponses);

		return response;
	}

	public static CompanyTaxCategoryWithComponentsCompanyProductsVariants mapToCompanyTaxCategoryWithComponentsCompanyProductsVariants(CompanyTaxCategory taxCategory,
			List<TaxComponentResponse> taxComponentResponses, CompanyResponseInternalDto companyDto,
			List<ProductResponse> products, List<ProductVariantResponse> variants) {
		
		CompanyTaxCategoryWithComponentsCompanyProductsVariants resultDto = new CompanyTaxCategoryWithComponentsCompanyProductsVariants();
		resultDto.setCompanyTaxCategoryId(taxCategory.getId());
		resultDto.setTaxRate(taxCategory.getTaxRate());
		resultDto.setHsnCode(taxCategory.getHsnCode());
		resultDto.setEffectiveFrom(taxCategory.getEffectiveFrom());
		resultDto.setEffectiveTo(taxCategory.getEffectiveTo());
		resultDto.setActive(taxCategory.getActive());
		resultDto.setCreatedAt(taxCategory.getCreatedAt());
		resultDto.setUpdatedAt(taxCategory.getUpdatedAt());
		resultDto.setComponents(taxComponentResponses);
		resultDto.setCompany(companyDto);
		resultDto.setProducts(products);
		resultDto.setVariants(variants);

		return resultDto;
		
	}

	public static PageMeta mapToPageMeta(Page<ProductTaxMapping> mappingsPage) {
		PageMeta emptyMeta = new PageMeta(mappingsPage.getNumber(), mappingsPage.getSize(),
				mappingsPage.getTotalElements(), mappingsPage.getTotalPages(), mappingsPage.isLast(),
				mappingsPage.isFirst(), mappingsPage.getSort().toString());
		
		return emptyMeta;
		
	}

}
