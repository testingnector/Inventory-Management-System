package com.nector.catalogservice.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.nector.catalogservice.dto.request.internal.ProductVariantCreateRequest;
import com.nector.catalogservice.dto.response.external.CompanyResponseExternalDto;
import com.nector.catalogservice.dto.response.internal.BaseUomDetails;
import com.nector.catalogservice.dto.response.internal.CompanyProductVariantsResponse;
import com.nector.catalogservice.dto.response.internal.CompanyResponseInternalDto;
import com.nector.catalogservice.dto.response.internal.ProductResponse;
import com.nector.catalogservice.dto.response.internal.ProductResponseWithProductVariants;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponse;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponseWithCompanyAndUom;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponseWithProductAndUom;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponseWithProductCompanyUom;
import com.nector.catalogservice.dto.response.internal.UomResponse;
import com.nector.catalogservice.entity.Product;
import com.nector.catalogservice.entity.ProductVariant;
import com.nector.catalogservice.entity.Uom;

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

	public static ProductVariantResponseWithProductCompanyUom mapToProductVariantResponseWithProductCompanyUom(ProductVariant variant,
			ProductResponse productResponse, CompanyResponseInternalDto companyResponse, UomResponse uomResponse) {

		if (variant == null) {
			return null;
		}

		ProductVariantResponseWithProductCompanyUom response = new ProductVariantResponseWithProductCompanyUom();
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

		response.setProduct(productResponse);
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
		if (variants == null || variants.isEmpty())
			return response;

		for (ProductVariant variant : variants) {
			if (variant == null)
				continue;

			ProductVariantResponseWithCompanyAndUom pvrwcu = new ProductVariantResponseWithCompanyAndUom();
			pvrwcu.setProductVariantId(variant.getId());
			pvrwcu.setSkuCode(variant.getSkuCode());
			pvrwcu.setVariantName(variant.getVariantName());
			pvrwcu.setColor(variant.getColor());
			pvrwcu.setSize(variant.getSize());
			pvrwcu.setCustomAttributes(variant.getCustomAttributes());
			pvrwcu.setMrp(variant.getMrp());
			pvrwcu.setSellingPrice(variant.getSellingPrice());
			pvrwcu.setPurchasePrice(variant.getPurchasePrice());
			pvrwcu.setSerialized(variant.getSerialized());
			pvrwcu.setBatchTracked(variant.getBatchTracked());
			pvrwcu.setExpiryTracked(variant.getExpiryTracked());
			pvrwcu.setActive(variant.getActive());

			CompanyResponseInternalDto company = (variant.getCompanyId() != null)
					? companyMap.get(variant.getCompanyId())
					: null;
			pvrwcu.setCompany(company);

			Uom uom = (variant.getUomId() != null) ? uomMap.get(variant.getUomId()) : null;
			UomResponse uomResponse = null;
			if (uom != null) {
				uomResponse = new UomResponse();
				uomResponse.setUomId(uom.getId());
				uomResponse.setUomCode(uom.getUomCode());
				uomResponse.setUomName(uom.getUomName());
				uomResponse.setUomType(uom.getUomType());
				uomResponse.setActive(uom.getActive());
				uomResponse.setCreatedAt(uom.getCreatedAt());
				uomResponse.setUpdatedAt(uom.getUpdatedAt());

				Uom baseUom = (uom.getBaseUomId() != null) ? baseUomMap.get(uom.getBaseUomId()) : null;
				if (baseUom != null) {
					BaseUomDetails baseDetails = new BaseUomDetails();
					baseDetails.setBaseUomId(baseUom.getId());
					baseDetails.setUomCode(baseUom.getUomCode());
					baseDetails.setUomName(baseUom.getUomName());
					baseDetails.setUomType(baseUom.getUomType());
					baseDetails.setActive(baseUom.getActive());
					uomResponse.setBaseUomDetails(baseDetails);
				}
			}
			pvrwcu.setUom(uomResponse);

			response.add(pvrwcu);
		}

		return response;
	}

	public static ProductResponseWithProductVariants mapProductResponseWithProductVariants(Product product,
			List<ProductVariantResponseWithCompanyAndUom> variants) {

		if (product == null) {
			return null;
		}

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

		response.setVariants(variants != null ? variants : new ArrayList<>());

		return response;
	}

	public static ProductVariantResponseWithProductAndUom mapToProductVariantResponseWithProductAndUom(
			ProductVariant variant, ProductResponse productResponse, UomResponse uomResponse) {
		if (variant == null) {
			return null;
		}

		ProductVariantResponseWithProductAndUom response = new ProductVariantResponseWithProductAndUom();
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

		response.setProduct(productResponse);
		response.setUom(uomResponse);

		return response;
	}

	public static CompanyProductVariantsResponse mapVariantsWithCompany(CompanyResponseInternalDto companyInternal,
			List<ProductVariantResponseWithProductAndUom> responseList) {

		CompanyProductVariantsResponse response = new CompanyProductVariantsResponse();
		response.setCompanyId(companyInternal.getCompanyId());
		response.setCompanyCode(companyInternal.getCompanyCode());
		response.setCompanyName(companyInternal.getCompanyName());
		response.setActive(companyInternal.getActive());

		response.setVariants(responseList);

		return response;

	}

	public static ProductVariantResponseWithProductAndUom mapToVariantWithProductAndUom(ProductVariant variant,
			Product product, Uom uom, Uom baseUom) {

		if (variant == null)
			return null;

		ProductResponse productResponse = product != null ? mapToProductResponse(product) : null;

		UomResponse uomResponse = uom != null ? mapToUomResponse(uom, baseUom) : null;

		return new ProductVariantResponseWithProductAndUom(variant.getId(), variant.getSkuCode(),
				variant.getVariantName(), variant.getColor(), variant.getSize(), variant.getCustomAttributes(),
				variant.getMrp(), variant.getSellingPrice(), variant.getPurchasePrice(), variant.getSerialized(),
				variant.getBatchTracked(), variant.getExpiryTracked(), variant.getActive(), productResponse,
				uomResponse);
	}

	public static ProductVariantResponse mapToProductVariantResponse(ProductVariant variant) {
		ProductVariantResponse response = new ProductVariantResponse();
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
		
		return response;
	}

}
