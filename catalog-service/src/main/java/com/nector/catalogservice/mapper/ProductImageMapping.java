package com.nector.catalogservice.mapper;

import java.util.List;
import java.util.UUID;

import com.nector.catalogservice.dto.request.internal.ProductImageCreateRequest;
import com.nector.catalogservice.dto.response.external.CompanyResponseExternalDto;
import com.nector.catalogservice.dto.response.internal.CompanyResponseInternalDto;
import com.nector.catalogservice.dto.response.internal.ProductImageResponse;
import com.nector.catalogservice.dto.response.internal.ProductImageResponseWithCompanyProductProductVariant;
import com.nector.catalogservice.dto.response.internal.ProductResponse;
import com.nector.catalogservice.dto.response.internal.ProductResponseWithCompanyImages;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponse;
import com.nector.catalogservice.entity.Product;
import com.nector.catalogservice.entity.ProductImage;
import com.nector.catalogservice.entity.ProductVariant;

public class ProductImageMapping {

	public static ProductImage toEntityWithCreation(ProductImageCreateRequest request, UUID createdBy) {
		if (request == null) {
			return null;
		}

		ProductImage image = new ProductImage();
		image.setProductId(request.getProductId());
		image.setProductVariantId(request.getProductVariantId());
		image.setCompanyId(request.getCompanyId());
		image.setImageType(request.getImageType());
		image.setAltText(request.getAltText());
		image.setPrimary(request.getPrimary() != null ? request.getPrimary() : false);
		image.setDisplayOrder(request.getDisplayOrder());
		image.setCreatedBy(createdBy);

		return image;
	}

	public static ProductImageResponseWithCompanyProductProductVariant mapToProductImageResponseWithCompanyProductProductVariant(
			ProductImage savedImage, CompanyResponseExternalDto companyResponse, Product product,
			ProductVariant productVariant) {

		if (savedImage == null) {
			return null;
		}

		ProductImageResponseWithCompanyProductProductVariant response = new ProductImageResponseWithCompanyProductProductVariant();

		response.setProductImageId(savedImage.getId());
		response.setImageUrl(savedImage.getImageUrl());
		response.setImageType(savedImage.getImageType());
		response.setAltText(savedImage.getAltText());
		response.setPrimary(savedImage.getPrimary());
		response.setDisplayOrder(savedImage.getDisplayOrder());
		response.setActive(savedImage.getActive());

		CompanyResponseInternalDto company = ProductVariantsMapping.mapToCompanyResponseInternalDto(companyResponse);
		response.setCompany(company);
		
		ProductResponse productResponse = ProductVariantsMapping.mapToProductResponse(product);
		response.setProduct(productResponse);
		
		if (productVariant != null) {
			ProductVariantResponse variantDto = new ProductVariantResponse();
			variantDto.setProductVariantId(productVariant.getId());
			variantDto.setSkuCode(productVariant.getSkuCode());
			variantDto.setVariantName(productVariant.getVariantName());
			variantDto.setColor(productVariant.getColor());
			variantDto.setSize(productVariant.getSize());
			variantDto.setCustomAttributes(productVariant.getCustomAttributes());
			variantDto.setMrp(productVariant.getMrp());
			variantDto.setSellingPrice(productVariant.getSellingPrice());
			variantDto.setPurchasePrice(productVariant.getPurchasePrice());
			variantDto.setSerialized(productVariant.getSerialized());
			variantDto.setBatchTracked(productVariant.getBatchTracked());
			variantDto.setExpiryTracked(productVariant.getExpiryTracked());
			variantDto.setActive(productVariant.getActive());

			response.setProductVariant(variantDto);
		}

		return response;
	}

	public static ProductImageResponse mapToProductImageResponse(ProductImage image) {
		ProductImageResponse response = new ProductImageResponse();
		response.setProductImageId(image.getId());
		response.setImageUrl(image.getImageUrl());
		response.setImageType(image.getImageType());
		response.setAltText(image.getAltText());
		response.setPrimary(image.getPrimary());
		response.setDisplayOrder(image.getDisplayOrder());
		response.setActive(image.getActive());
		
		return response;
	}

	public static CompanyResponseInternalDto mapToCompanyResponseInternalDto(CompanyResponseExternalDto companyResponse) {
		CompanyResponseInternalDto company = ProductVariantsMapping.mapToCompanyResponseInternalDto(companyResponse);
		return company;
	}

	public static ProductResponseWithCompanyImages mapToProductResponseWithCompanyImages(Product product, CompanyResponseInternalDto company, List<ProductImageResponse> productImages) {
		
		ProductResponseWithCompanyImages response = new ProductResponseWithCompanyImages();
		response.setProductId(product.getId());
		response.setProductCode(product.getProductCode());
		response.setProductName(product.getProductName());
		response.setDescription(product.getDescription());
		response.setBrandName(product.getBrandName());
		response.setModelNumber(product.getModelNumber());

		response.setVariantBased(product.getVariantBased());
		response.setSerialized(product.getSerialized());
		response.setBatchTracked(product.getBatchTracked());
		response.setExpiryTracked(product.getExpiryTracked());
		response.setActive(product.getActive());
		
		response.setCompany(company);
		response.setImages(productImages);
		
		return response;
		
		
	}

}

