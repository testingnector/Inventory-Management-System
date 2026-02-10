package com.nector.catalogservice.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nector.catalogservice.client.OrgServiceClient;
import com.nector.catalogservice.dto.request.internal.ProductVariantCreateRequest;
import com.nector.catalogservice.dto.response.external.CompanyResponseExternalDto;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CompanyResponseInternalDto;
import com.nector.catalogservice.dto.response.internal.ProductResponse;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponse;
import com.nector.catalogservice.dto.response.internal.UomResponse;
import com.nector.catalogservice.entity.Product;
import com.nector.catalogservice.entity.ProductVariant;
import com.nector.catalogservice.entity.Uom;
import com.nector.catalogservice.exception.DuplicateResourceException;
import com.nector.catalogservice.exception.OrgServiceException;
import com.nector.catalogservice.mappers.ProductVariantsMapping;
import com.nector.catalogservice.repository.ProductRepository;
import com.nector.catalogservice.repository.ProductVariantRepository;
import com.nector.catalogservice.repository.UomRepository;
import com.nector.catalogservice.service.ProductVariantService;

import feign.FeignException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

	private final ProductVariantRepository productVariantRepository;
	private final OrgServiceClient orgServiceClient;
	private final ProductRepository productRepository;
	private final UomRepository uomRepository;

	@Transactional
	@Override
	public ApiResponse<ProductVariantResponse> createProductVariant(ProductVariantCreateRequest request,
			UUID createdBy) {
		
		productVariantRepository.findBySkuCodeAndCompanyId(request.getSkuCode(), request.getCompanyId()).ifPresent(v -> {
			throw new DuplicateResourceException("Variant SKU already exists for this company");
		});
		
		ProductVariant productVariant = ProductVariantsMapping.toEntityWithCreation(request, createdBy);

		ProductVariant saved = productVariantRepository.save(productVariant);

		CompanyResponseExternalDto companyResponse;
		try {
			companyResponse = orgServiceClient.getCompanyBasic(request.getCompanyId()).getBody().getData();
		} catch (FeignException e) {
			HttpStatus status = HttpStatus.resolve(e.status());
			String message = (status == HttpStatus.NOT_FOUND) ? "Company not found!"
					: "Error while communicating with Organization Service";
			throw new OrgServiceException(message, status, e);
		}
		
		CompanyResponseInternalDto companyResponseInternalDto = ProductVariantsMapping.mapToCompanyResponseInternalDto(companyResponse);
		
		Product product = productRepository.findByIdAndDeletedAtIsNull(saved.getProductId()).orElse(null);
		ProductResponse productResponse = ProductVariantsMapping.mapToProductResponse(product);
		
		Uom uom = uomRepository.findByIdAndDeletedAtIsNull(saved.getUomId()).orElse(null);
		Uom baseUom = null;
		if (uom.getBaseUomId() != null) {
			baseUom = uomRepository.findByIdAndDeletedAtIsNull(uom.getBaseUomId()).orElse(null);
		}
		
		UomResponse uomResponse = ProductVariantsMapping.mapToUomResponse(uom, baseUom);
		
		ProductVariantResponse productVariantResponse = ProductVariantsMapping.mapToProductVariantResponse(saved, productResponse, companyResponseInternalDto, uomResponse);

		return new ApiResponse<>(true, "Product variant created successfully", "OK", 200, productVariantResponse);
	}


	
	
	
	
	
	
	
	
	
	
	









	
}
