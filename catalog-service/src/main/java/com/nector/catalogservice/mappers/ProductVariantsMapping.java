package com.nector.catalogservice.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.nector.catalogservice.dto.request.internal.ProductVariantCreateRequest;
import com.nector.catalogservice.dto.response.external.CompanyResponseExternalDto;
import com.nector.catalogservice.dto.response.internal.BaseUomDetails;
import com.nector.catalogservice.dto.response.internal.CompanyResponseInternalDto;
import com.nector.catalogservice.dto.response.internal.ProductResponse;
import com.nector.catalogservice.dto.response.internal.ProductResponseWithProductVariants;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponse;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponseWithCompanyAndUom;
import com.nector.catalogservice.dto.response.internal.UomResponse;
import com.nector.catalogservice.entity.Product;
import com.nector.catalogservice.entity.ProductVariant;
import com.nector.catalogservice.entity.Uom;
import com.nector.catalogservice.exception.ExternalServiceException;

public class ProductVariantsMapping {

	public static ProductVariant toEntityWithCreation(ProductVariantCreateRequest request, UUID createdBy) {

		ProductVariant variant = new ProductVariant();
		variant.setSkuCode(request.getSkuCode());
		variant.setVariantName(request.getVariantName());
		variant.setProductId(request.getProductId());
		variant.setCompanyId(request.getCompanyId());
		variant.setColor(request.getColor());
		variant.setSize(request.getSize());
		variant.setCustomAttributes(request.getCustomAttributes());
		variant.setMrp(request.getMrp());
		variant.setSellingPrice(request.getSellingPrice());
		variant.setPurchasePrice(request.getPurchasePrice());
		variant.setUomId(request.getUomId());
		variant.setConversionFactor(request.getConversionFactor());
		variant.setSerialized(request.getSerialized());
		variant.setBatchTracked(request.getBatchTracked());
		variant.setExpiryTracked(request.getExpiryTracked());
		variant.setCreatedBy(createdBy);

		return variant;
	}

	public static ProductResponse mapToProductResponse(Product product) {

		if (product == null)
			return null;

		ProductResponse response = new ProductResponse();
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

		return response;

	}

	public static UomResponse mapToUomResponse(Uom uom, Uom baseUom) {
		if (uom == null)
			return null;

		UomResponse uomResponse = new UomResponse();
		uomResponse.setUomId(uom.getId());
		uomResponse.setUomCode(uom.getUomCode());
		uomResponse.setUomName(uom.getUomName());
		uomResponse.setUomType(uom.getUomType());
		uomResponse.setActive(uom.getActive());
		uomResponse.setCreatedAt(uom.getCreatedAt());
		uomResponse.setUpdatedAt(uom.getUpdatedAt());

		if (baseUom != null) {
			BaseUomDetails baseDetails = new BaseUomDetails();
			baseDetails.setBaseUomId(baseUom.getId());
			baseDetails.setUomCode(baseUom.getUomCode());
			baseDetails.setUomName(baseUom.getUomName());
			baseDetails.setUomType(baseUom.getUomType());
			baseDetails.setActive(baseUom.getActive());

			uomResponse.setBaseUomDetails(baseDetails);
		}

		return uomResponse;
	}

	public static ProductVariantResponse mapToProductVariantResponse(ProductVariant variant,
			ProductResponse productResponse, CompanyResponseInternalDto companyResponse, UomResponse uomResponse) {

		if (variant == null)
			return null;

		ProductVariantResponse response = new ProductVariantResponse();
		response.setSkuCode(variant.getSkuCode());
		response.setVariantName(variant.getVariantName());
		response.setColor(variant.getColor());
		response.setSize(variant.getSize());
		response.setCustomAttributes(variant.getCustomAttributes());
		response.setMrp(variant.getMrp() != null ? variant.getMrp().doubleValue() : null);
		response.setSellingPrice(variant.getSellingPrice() != null ? variant.getSellingPrice().doubleValue() : null);
		response.setSerialized(variant.getSerialized());
		response.setBatchTracked(variant.getBatchTracked());
		response.setExpiryTracked(variant.getExpiryTracked());
		response.setActive(variant.getActive());

		response.setProduct(productResponse);
		if (companyResponse == null) {
		    throw new ExternalServiceException("Company data missing",
		            HttpStatus.INTERNAL_SERVER_ERROR);
		}
		response.setCompany(companyResponse);
		response.setUom(uomResponse);

		return response;
	}

	public static CompanyResponseInternalDto mapToCompanyResponseInternalDto(
			CompanyResponseExternalDto companyResponse) {
		
		CompanyResponseInternalDto crid = new CompanyResponseInternalDto();
		crid.setCompanyId(companyResponse.getCompanyId());
		crid.setCompanyCode(companyResponse.getCompanyCode());
		crid.setCompanyName(companyResponse.getCompanyName());
		crid.setActive(companyResponse.getActive());
		
		return crid;
		
	}

	public static List<ProductVariantResponseWithCompanyAndUom> mapVariantsCompanyAndUom(List<ProductVariant> variants,
			Map<UUID, CompanyResponseInternalDto> companyMap, Map<UUID, Uom> uomMap, Map<UUID, Uom> baseUomMap) {
	
		List<ProductVariantResponseWithCompanyAndUom> response = new ArrayList<>();
		for (ProductVariant variant : variants) {
			
			ProductVariantResponseWithCompanyAndUom pvrwcu = new ProductVariantResponseWithCompanyAndUom();
			pvrwcu.setSkuCode(variant.getSkuCode());
			pvrwcu.setVariantName(variant.getVariantName());
			pvrwcu.setColor(variant.getColor());
			pvrwcu.setSize(variant.getSize());
			pvrwcu.setCustomAttributes(variant.getCustomAttributes());
			pvrwcu.setMrp(variant.getMrp() != null ? variant.getMrp().doubleValue() : null);
			pvrwcu.setSellingPrice(variant.getSellingPrice() != null ? variant.getSellingPrice().doubleValue() : null);
			pvrwcu.setSerialized(variant.getSerialized());
			pvrwcu.setBatchTracked(variant.getBatchTracked());
			pvrwcu.setExpiryTracked(variant.getExpiryTracked());
			pvrwcu.setActive(variant.getActive());

			CompanyResponseInternalDto company = companyMap.get(variant.getCompanyId());
			pvrwcu.setCompany(company);
			
			Uom uom = uomMap.get(variant.getUomId());
			UomResponse uomResponse = new UomResponse();
			uomResponse.setUomId(uom.getId());
			uomResponse.setUomCode(uom.getUomCode());
			uomResponse.setUomName(uom.getUomName());
			uomResponse.setUomType(uom.getUomType());
			uomResponse.setActive(uom.getActive());
			uomResponse.setCreatedAt(uom.getCreatedAt());
			uomResponse.setUpdatedAt(uom.getUpdatedAt());

			Uom baseUom = baseUomMap.get(uom.getBaseUomId());
			if (baseUom != null) {
				BaseUomDetails baseDetails = new BaseUomDetails();
				baseDetails.setBaseUomId(baseUom.getId());
				baseDetails.setUomCode(baseUom.getUomCode());
				baseDetails.setUomName(baseUom.getUomName());
				baseDetails.setUomType(baseUom.getUomType());
				baseDetails.setActive(baseUom.getActive());

				uomResponse.setBaseUomDetails(baseDetails);
			}
			
			response.add(pvrwcu);
			
		}
		
		return response;
		
	}

	public static ProductResponseWithProductVariants mapProductResponseWithProductVariants(Product product,
			List<ProductVariantResponseWithCompanyAndUom> mapVariantsCompanyAndUoms) {

		ProductResponseWithProductVariants response = new ProductResponseWithProductVariants();
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
		
		response.setVariants(mapVariantsCompanyAndUoms);
		
		return response;
	}
	
	

}
