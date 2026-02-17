package com.nector.catalogservice.service.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nector.catalogservice.client.OrgServiceClient;
import com.nector.catalogservice.dto.request.internal.ProductTaxMappingCreateRequest;
import com.nector.catalogservice.dto.request.internal.ProductTaxMappingUpdateRequest;
import com.nector.catalogservice.dto.response.external.CompanyResponseExternalDto;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CompanyResponseInternalDto;
import com.nector.catalogservice.dto.response.internal.CompanyTaxCategoryResponse;
import com.nector.catalogservice.dto.response.internal.ProductResponse;
import com.nector.catalogservice.dto.response.internal.ProductTaxMappingResponseWithCompanyProductProductVariantCompanyTaxCategory;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponse;
import com.nector.catalogservice.dto.response.internal.ProductWithTaxMappingsResponse;
import com.nector.catalogservice.dto.response.internal.TaxComponentResponse;
import com.nector.catalogservice.entity.CompanyTaxCategory;
import com.nector.catalogservice.entity.Product;
import com.nector.catalogservice.entity.ProductTaxMapping;
import com.nector.catalogservice.entity.ProductVariant;
import com.nector.catalogservice.entity.TaxComponent;
import com.nector.catalogservice.exception.ExternalServiceException;
import com.nector.catalogservice.exception.InactiveResourceException;
import com.nector.catalogservice.exception.ResourceNotFoundException;
import com.nector.catalogservice.mapper.CompanyTaxCategoryMapping;
import com.nector.catalogservice.mapper.ProductTaxMappingMap;
import com.nector.catalogservice.mapper.ProductVariantsMapping;
import com.nector.catalogservice.repository.CompanyTaxCategoryRepository;
import com.nector.catalogservice.repository.ProductRepository;
import com.nector.catalogservice.repository.ProductTaxMappingRepository;
import com.nector.catalogservice.repository.ProductVariantRepository;
import com.nector.catalogservice.repository.TaxComponentRepository;
import com.nector.catalogservice.service.ProductTaxMappingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductTaxMappingServiceImpl implements ProductTaxMappingService {

	private final ProductTaxMappingRepository productTaxMappingRepository;
	private final OrgServiceClient orgServiceClient;
	private final ProductRepository productRepository;
	private final ProductVariantRepository productVariantRepository;
	private final CompanyTaxCategoryRepository companyTaxCategoryRepository;
	private final TaxComponentRepository taxComponentRepository;

	@Override
	@Transactional
	public ApiResponse<?> createProductTaxMapping(@Valid ProductTaxMappingCreateRequest request, UUID createdBy) {

		if ((request.getProductId() == null && request.getProductVariantId() == null)
				|| (request.getProductId() != null && request.getProductVariantId() != null)) {
			throw new IllegalArgumentException("Either productId OR productVariantId must be provided");
		}

		var response = orgServiceClient.getCompanyBasic(request.getCompanyId());
		if (response == null || response.getBody() == null || response.getBody().getData() == null) {
			throw new ExternalServiceException("Invalid response from Organization Service",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		CompanyResponseExternalDto companyResponse = response.getBody().getData();

		if (companyResponse == null) {
			throw new ResourceNotFoundException("Company not found");
		}
		if (Boolean.FALSE.equals(companyResponse.getActive())) {
			throw new InactiveResourceException("Company is inactive");
		}

		if (request.getProductId() != null) {
			var product = productRepository.findByIdAndDeletedAtIsNull(request.getProductId())
					.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

			if (!product.getActive()) {
				throw new InactiveResourceException("Product is inactive");
			}

			if (!product.getCompanyId().equals(request.getCompanyId())) {
				throw new IllegalArgumentException("Product does not belong to company");
			}

			if (productTaxMappingRepository.existsByCompanyIdAndProductIdAndCompanyTaxCategoryIdAndDeletedAtIsNull(
					request.getCompanyId(), request.getProductId(), request.getCompanyTaxCategoryId())) {
				throw new IllegalArgumentException("Tax already mapped to product");
			}
		}

		if (request.getProductVariantId() != null) {
			var variant = productVariantRepository.findByIdAndDeletedAtIsNull(request.getProductVariantId())
					.orElseThrow(() -> new ResourceNotFoundException("Variant not found"));

			if (!variant.getActive()) {
				throw new InactiveResourceException("Variant is inactive");
			}

			if (!variant.getCompanyId().equals(request.getCompanyId())) {
				throw new IllegalArgumentException("Variant does not belong to company");
			}

			if (productTaxMappingRepository
					.existsByCompanyIdAndProductVariantIdAndCompanyTaxCategoryIdAndDeletedAtIsNull(
							request.getCompanyId(), request.getProductVariantId(), request.getCompanyTaxCategoryId())) {
				throw new IllegalArgumentException("Tax already mapped to variant");
			}
		}

		CompanyTaxCategory companyTaxCategory = companyTaxCategoryRepository
				.findByIdAndDeletedAtIsNull(request.getCompanyTaxCategoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Tax category not found"));

		if (!companyTaxCategory.getActive()) {
			throw new InactiveResourceException("Tax category is inactive");
		}

		ProductTaxMapping entity = ProductTaxMappingMap.mapToEntityFromCreationDto(request, createdBy);

		ProductTaxMapping saved = productTaxMappingRepository.save(entity);

		Product product = request.getProductId() != null
				? productRepository.findByIdAndDeletedAtIsNullAndActiveTrue(saved.getProductId()).orElse(null)
				: null;

		ProductVariant variant = request.getProductVariantId() != null
				? productVariantRepository.findByIdAndDeletedAtIsNullAndActiveTrue(saved.getProductVariantId())
						.orElse(null)
				: null;
		

		List<TaxComponent> taxComponents = taxComponentRepository
				.findAllByCompanyTaxCategoryIdAndActiveTrueAndDeletedAtIsNull(companyTaxCategory.getId());

		CompanyResponseInternalDto company = ProductTaxMappingMap.mapToCompanyResponseInternalDto(companyResponse);

		ProductResponse productResponse = product != null ? ProductVariantsMapping.mapToProductResponse(product) : null;

		ProductVariantResponse productVariantResponse = variant != null
				? ProductVariantsMapping.mapToProductVariantResponse(variant)
				: null;

		List<TaxComponentResponse> taxComponentResponse = taxComponents != null ? taxComponents.stream().map(t -> {
			TaxComponentResponse r = new TaxComponentResponse();
			r.setTaxComponentId(t.getId());
			r.setComponentType(t.getComponentType());
			r.setComponentRate(t.getComponentRate());
			r.setActive(t.getActive());

			return r;
		}).toList() : null;

		CompanyTaxCategoryResponse companyTaxCategoryResponse = companyTaxCategory != null
				? CompanyTaxCategoryMapping.mapToCompanyTaxCategoryResponse(companyTaxCategory, taxComponentResponse)
				: null;

		ProductTaxMappingResponseWithCompanyProductProductVariantCompanyTaxCategory finalResponse = ProductTaxMappingMap
				.mapToProductTaxMappingResponseWithCompanyProductProductVariantCompanyTaxCategory(saved, company,
						productResponse, productVariantResponse, companyTaxCategoryResponse);

		return new ApiResponse<>(true, "Product tax mapping created successfully", HttpStatus.CREATED.name(),
				HttpStatus.CREATED.value(), finalResponse);
	}

	@Override
	@Transactional
	public ApiResponse<?> updateProductTaxMapping(UUID companyId, UUID id,
			@Valid ProductTaxMappingUpdateRequest request, UUID updatedBy) {

		ProductTaxMapping existing = productTaxMappingRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product tax mapping not found"));

		if (!existing.getCompanyId().equals(companyId)) {
			throw new IllegalArgumentException("Product or Variant does not belong to this company");
		}

		CompanyTaxCategory companyTaxCategory = companyTaxCategoryRepository
				.findByIdAndDeletedAtIsNull(request.getCompanyTaxCategoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Tax category not found"));
		if (!companyTaxCategory.getActive())
			throw new InactiveResourceException("Tax category is inactive");

		existing.setCompanyTaxCategoryId(request.getCompanyTaxCategoryId());
		existing.setUpdatedBy(updatedBy);

		ProductTaxMapping saved = productTaxMappingRepository.save(existing);

		var response = orgServiceClient.getCompanyBasic(saved.getCompanyId());
		if (response == null || response.getBody() == null || response.getBody().getData() == null) {
			throw new ExternalServiceException("Invalid response from Organization Service",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		CompanyResponseExternalDto companyResponse = response.getBody().getData();
		if (companyResponse == null) {
			throw new ResourceNotFoundException("Company not found");
		}
		if (Boolean.FALSE.equals(companyResponse.getActive())) {
			throw new InactiveResourceException("Company is inactive");
		}

		Product product = saved.getProductId() != null
				? productRepository.findByIdAndDeletedAtIsNullAndActiveTrue(saved.getProductId()).orElse(null)
				: null;

		ProductVariant variant = saved.getProductVariantId() != null
				? productVariantRepository.findByIdAndDeletedAtIsNullAndActiveTrue(saved.getProductVariantId())
						.orElse(null)
				: null;

		List<TaxComponent> taxComponents = taxComponentRepository
				.findAllByCompanyTaxCategoryIdAndActiveTrueAndDeletedAtIsNull(companyTaxCategory.getId());

		CompanyResponseInternalDto company = ProductTaxMappingMap.mapToCompanyResponseInternalDto(companyResponse);
		ProductResponse productResponse = product != null ? ProductVariantsMapping.mapToProductResponse(product) : null;
		ProductVariantResponse productVariantResponse = variant != null
				? ProductVariantsMapping.mapToProductVariantResponse(variant)
				: null;

		List<TaxComponentResponse> taxComponentResponse = taxComponents.stream().map(t -> {
			TaxComponentResponse r = new TaxComponentResponse();
			r.setTaxComponentId(t.getId());
			r.setComponentType(t.getComponentType());
			r.setComponentRate(t.getComponentRate());
			r.setActive(t.getActive());
			return r;
		}).toList();

		CompanyTaxCategoryResponse companyTaxCategoryResponse = CompanyTaxCategoryMapping
				.mapToCompanyTaxCategoryResponse(companyTaxCategory, taxComponentResponse);

		ProductTaxMappingResponseWithCompanyProductProductVariantCompanyTaxCategory finalResponse = ProductTaxMappingMap
				.mapToProductTaxMappingResponseWithCompanyProductProductVariantCompanyTaxCategory(saved, company,
						productResponse, productVariantResponse, companyTaxCategoryResponse);

		return new ApiResponse<>(true, "Product tax mapping updated successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), finalResponse);
	}

	@Override
	@Transactional
	public ApiResponse<Void> deleteProductTaxMapping(UUID id, UUID companyId, UUID deletedBy) {

		ProductTaxMapping entity = productTaxMappingRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new ResourceNotFoundException("Mapping not found or already deleted"));

		if (!entity.getCompanyId().equals(companyId)) {
			throw new IllegalArgumentException("Product or Variant does not belong to this company");
		}

		entity.setDeletedAt(LocalDateTime.now());
		entity.setDeletedBy(deletedBy);

		productTaxMappingRepository.save(entity);

		return new ApiResponse<>(true, "Deleted successfully", HttpStatus.OK.name(), HttpStatus.OK.value(), null);
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse<?> getByCompanyAndId(UUID id, UUID companyId) {

		ProductTaxMapping entity = productTaxMappingRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new ResourceNotFoundException("Mapping not found"));

		if (!entity.getCompanyId().equals(companyId)) {
			throw new IllegalArgumentException("Product or Variant does not belong to this company");
		}

		var response = orgServiceClient.getCompanyBasic(entity.getCompanyId());
		if (response == null || response.getBody() == null || response.getBody().getData() == null) {
			throw new ExternalServiceException("Invalid response from Organization Service",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		CompanyResponseExternalDto companyResponse = response.getBody().getData();
		if (companyResponse == null) {
			throw new ResourceNotFoundException("Company not found");
		}
		if (Boolean.FALSE.equals(companyResponse.getActive())) {
			throw new InactiveResourceException("Company is inactive");
		}

		Product product = entity.getProductId() != null
				? productRepository.findByIdAndDeletedAtIsNullAndActiveTrue(entity.getProductId()).orElse(null)
				: null;
		if (product != null && !product.getActive()) {
			throw new InactiveResourceException("Associated product is inactive");
		}

		ProductVariant variant = entity.getProductVariantId() != null
				? productVariantRepository.findByIdAndDeletedAtIsNullAndActiveTrue(entity.getProductVariantId())
						.orElse(null)
				: null;
		if (variant != null && !variant.getActive()) {
			throw new InactiveResourceException("Associated variant is inactive");
		}

		CompanyTaxCategory companyTaxCategory = companyTaxCategoryRepository
				.findByIdAndDeletedAtIsNull(entity.getCompanyTaxCategoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Tax category not found"));
		if (!companyTaxCategory.getActive())
			throw new InactiveResourceException("Associated tax category is inactive");

		List<TaxComponent> taxComponents = taxComponentRepository
				.findAllByCompanyTaxCategoryIdAndActiveTrueAndDeletedAtIsNull(companyTaxCategory.getId());

		CompanyResponseInternalDto company = ProductTaxMappingMap.mapToCompanyResponseInternalDto(companyResponse);
		ProductResponse productResponse = product != null ? ProductVariantsMapping.mapToProductResponse(product) : null;
		ProductVariantResponse productVariantResponse = variant != null
				? ProductVariantsMapping.mapToProductVariantResponse(variant)
				: null;

		List<TaxComponentResponse> taxComponentResponse = taxComponents.stream().map(t -> {
			TaxComponentResponse r = new TaxComponentResponse();
			r.setTaxComponentId(t.getId());
			r.setComponentType(t.getComponentType());
			r.setComponentRate(t.getComponentRate());
			r.setActive(t.getActive());
			return r;
		}).toList();

		CompanyTaxCategoryResponse companyTaxCategoryResponse = CompanyTaxCategoryMapping
				.mapToCompanyTaxCategoryResponse(companyTaxCategory, taxComponentResponse);

		ProductTaxMappingResponseWithCompanyProductProductVariantCompanyTaxCategory finalResponse = ProductTaxMappingMap
				.mapToProductTaxMappingResponseWithCompanyProductProductVariantCompanyTaxCategory(entity, company,
						productResponse, productVariantResponse, companyTaxCategoryResponse);

		return new ApiResponse<>(true, "Product tax mapping fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), finalResponse);

	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse<?> getByCompanyIdAndProductId(UUID companyId, UUID productId) {

		if (companyId == null || productId == null) {
			throw new IllegalArgumentException("CompanyId and ProductId must be provided");
		}

		var response = orgServiceClient.getCompanyBasic(companyId);
		if (response == null || response.getBody() == null || response.getBody().getData() == null) {
			throw new ExternalServiceException("Invalid response from Organization Service",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		CompanyResponseExternalDto companyResponse = response.getBody().getData();

		if (companyResponse == null) {
			throw new ResourceNotFoundException("Company not found");
		}
		if (Boolean.FALSE.equals(companyResponse.getActive())) {
			throw new InactiveResourceException("Company is inactive");
		}

		Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		if (!companyId.equals(product.getCompanyId())) {
			throw new IllegalArgumentException("Product does not belong to given company");
		}
		if (!Boolean.TRUE.equals(product.getActive())) {
			throw new InactiveResourceException("Product is inactive");
		}

		List<ProductVariant> variants = Optional
				.ofNullable(productVariantRepository.findByProductIdAndDeletedAtIsNullAndActiveTrue(productId))
				.orElse(Collections.emptyList()).stream().filter(Objects::nonNull).toList();

		List<ProductVariantResponse> variantResponses = variants.stream()
				.map(ProductVariantsMapping::mapToProductVariantResponse).toList();

		List<ProductTaxMapping> taxMappings = productTaxMappingRepository
				.findAllByCompanyIdAndProductIdAndDeletedAtIsNull(companyId, productId);
		
		Set<UUID> companyTaxCategoryIds = taxMappings.stream().map(ProductTaxMapping::getCompanyTaxCategoryId).collect(Collectors.toSet());
		Map<UUID, CompanyTaxCategory> companyTaxCategoryMap = companyTaxCategoryRepository
				.findByIdInAndDeletedAtIsNull(companyTaxCategoryIds).stream()
				.collect(Collectors.toMap(CompanyTaxCategory::getId, c -> c));
		
		List<ProductWithTaxMappingsResponse.ProductTaxMappingSummary> taxMappingSummaries = taxMappings.stream()
				.map(mapping -> {
					
					var taxCategory = companyTaxCategoryMap.get(mapping.getCompanyTaxCategoryId());

					ProductWithTaxMappingsResponse.ProductTaxMappingSummary summary = new ProductWithTaxMappingsResponse.ProductTaxMappingSummary();
					summary.setTaxMappingId(mapping.getId());
					summary.setProductId(mapping.getProductId());
					summary.setTaxCategory(
							taxCategory != null
									? CompanyTaxCategoryMapping.mapToCompanyTaxCategoryResponse(taxCategory,
											Collections.emptyList())
									: null);
					return summary;
				}).toList();

		CompanyResponseInternalDto company = ProductTaxMappingMap.mapToCompanyResponseInternalDto(companyResponse);

		ProductWithTaxMappingsResponse finalResponse = ProductTaxMappingMap.mapToProductWithTaxMappingsResponse(product, company, variantResponses, taxMappingSummaries);
		
		return new ApiResponse<>(true, "Product with tax mappings fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), finalResponse);
	}

	@Override
	public ApiResponse<?> getByCompanyIdAndVariantId(UUID companyId, UUID variantId) {
		// TODO Auto-generated method stub
		return null;
	}

}
