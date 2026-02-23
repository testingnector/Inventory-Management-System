package com.nector.catalogservice.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nector.catalogservice.client.OrgServiceClient;
import com.nector.catalogservice.dto.request.external.CompanyIdsRequestDto;
import com.nector.catalogservice.dto.request.internal.ProductTaxMappingBulkCreateRequest;
import com.nector.catalogservice.dto.request.internal.ProductTaxMappingCreateRequest;
import com.nector.catalogservice.dto.request.internal.ProductTaxMappingUpdateRequest;
import com.nector.catalogservice.dto.response.external.CompanyResponseExternalDto;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CompanyResponseInternalDto;
import com.nector.catalogservice.dto.response.internal.CompanyTaxCategoryResponse;
import com.nector.catalogservice.dto.response.internal.CompanyTaxCategoryWithComponentsCompanyProductsVariants;
import com.nector.catalogservice.dto.response.internal.CompanyTaxMappingsPageResponse;
import com.nector.catalogservice.dto.response.internal.PageMeta;
import com.nector.catalogservice.dto.response.internal.PaginatedResponse;
import com.nector.catalogservice.dto.response.internal.ProductResponse;
import com.nector.catalogservice.dto.response.internal.ProductTaxMappingResponseWithCompanyProductProductVariantCompanyTaxCategory;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponse;
import com.nector.catalogservice.dto.response.internal.ProductVariantWithTaxMappingsResponse;
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
import com.nector.catalogservice.specification.ProductTaxMappingSpecification;

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

		Set<UUID> companyTaxCategoryIds = taxMappings.stream().map(ProductTaxMapping::getCompanyTaxCategoryId)
				.collect(Collectors.toSet());
		Map<UUID, CompanyTaxCategory> companyTaxCategoryMap = companyTaxCategoryRepository
				.findByIdInAndDeletedAtIsNull(companyTaxCategoryIds).stream()
				.collect(Collectors.toMap(CompanyTaxCategory::getId, c -> c));

		Map<UUID, List<TaxComponent>> taxComponentGroup = taxComponentRepository
				.findByCompanyTaxCategoryIdInAndDeletedAtIsNull(new ArrayList<>(companyTaxCategoryIds)).stream()
				.collect(Collectors.groupingBy(TaxComponent::getCompanyTaxCategoryId));

		List<ProductWithTaxMappingsResponse.ProductTaxMappingSummary> taxMappingSummaries = taxMappings.stream()
				.map(mapping -> {

					var taxCategory = companyTaxCategoryMap.get(mapping.getCompanyTaxCategoryId());

					List<TaxComponent> taxComponents = taxCategory != null
							? taxComponentGroup.getOrDefault(taxCategory.getId(), Collections.emptyList())
							: Collections.emptyList();

					List<TaxComponentResponse> taxComponentResponses = taxComponents.stream().map(t -> {
						TaxComponentResponse r = new TaxComponentResponse();
						r.setTaxComponentId(t.getId());
						r.setComponentType(t.getComponentType());
						r.setComponentRate(t.getComponentRate());
						r.setActive(t.getActive());
						return r;
					}).toList();

					ProductWithTaxMappingsResponse.ProductTaxMappingSummary summary = new ProductWithTaxMappingsResponse.ProductTaxMappingSummary();
					summary.setTaxMappingId(mapping.getId());
					summary.setProductId(mapping.getProductId());
					summary.setTaxCategory(
							taxCategory != null
									? CompanyTaxCategoryMapping.mapToCompanyTaxCategoryResponse(taxCategory,
											taxComponentResponses)
									: null);
					return summary;
				}).toList();

		CompanyResponseInternalDto company = ProductTaxMappingMap.mapToCompanyResponseInternalDto(companyResponse);

		ProductWithTaxMappingsResponse finalResponse = ProductTaxMappingMap.mapToProductWithTaxMappingsResponse(product,
				company, variantResponses, taxMappingSummaries);

		return new ApiResponse<>(true, "Product with tax mappings fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), finalResponse);
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse<?> getByCompanyIdAndVariantId(UUID companyId, UUID variantId) {

		if (companyId == null || variantId == null) {
			throw new IllegalArgumentException("CompanyId and VariantId must be provided");
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

		ProductVariant variant = productVariantRepository.findByIdAndDeletedAtIsNull(variantId)
				.orElseThrow(() -> new ResourceNotFoundException("Variant not found"));

		if (!companyId.equals(variant.getCompanyId())) {
			throw new IllegalArgumentException("Variant does not belong to given company");
		}

		if (!Boolean.TRUE.equals(variant.getActive())) {
			throw new InactiveResourceException("Variant is inactive");
		}

		UUID productId = variant.getProductId();
		if (productId == null) {
			throw new IllegalStateException("Variant is not linked to any product");
		}
		Product product = productRepository.findByIdAndDeletedAtIsNull(variant.getProductId())
				.orElseThrow(() -> new ResourceNotFoundException("Associated product is not found"));
		if (!Boolean.TRUE.equals(product.getActive())) {
			throw new InactiveResourceException("Associated product is inactive");
		}

		List<ProductTaxMapping> taxMappings = productTaxMappingRepository
				.findAllByCompanyIdAndProductVariantIdAndDeletedAtIsNull(companyId, variantId);

		Set<UUID> companyTaxCategoryIds = taxMappings.stream().map(ProductTaxMapping::getCompanyTaxCategoryId)
				.collect(Collectors.toSet());

		Map<UUID, CompanyTaxCategory> companyTaxCategoryMap = companyTaxCategoryRepository
				.findByIdInAndDeletedAtIsNull(companyTaxCategoryIds).stream()
				.collect(Collectors.toMap(CompanyTaxCategory::getId, c -> c));

		Map<UUID, List<TaxComponent>> taxComponentGroup = taxComponentRepository
				.findByCompanyTaxCategoryIdInAndDeletedAtIsNull(new ArrayList<>(companyTaxCategoryIds)).stream()
				.collect(Collectors.groupingBy(TaxComponent::getCompanyTaxCategoryId));

		List<ProductVariantWithTaxMappingsResponse.ProductVariantTaxMappingSummary> taxMappingSummaries = taxMappings
				.stream().map(mapping -> {

					var taxCategory = companyTaxCategoryMap.get(mapping.getCompanyTaxCategoryId());

					List<TaxComponent> taxComponents = taxCategory != null
							? taxComponentGroup.getOrDefault(taxCategory.getId(), Collections.emptyList())
							: Collections.emptyList();

					List<TaxComponentResponse> taxComponentResponses = taxComponents.stream().map(t -> {
						TaxComponentResponse r = new TaxComponentResponse();
						r.setTaxComponentId(t.getId());
						r.setComponentType(t.getComponentType());
						r.setComponentRate(t.getComponentRate());
						r.setActive(t.getActive());
						return r;
					}).toList();

					ProductVariantWithTaxMappingsResponse.ProductVariantTaxMappingSummary summary = new ProductVariantWithTaxMappingsResponse.ProductVariantTaxMappingSummary();

					summary.setTaxMappingId(mapping.getId());
					summary.setVariantId(mapping.getProductVariantId());
					summary.setTaxCategory(
							taxCategory != null
									? CompanyTaxCategoryMapping.mapToCompanyTaxCategoryResponse(taxCategory,
											taxComponentResponses)
									: null);

					return summary;
				}).toList();

		CompanyResponseInternalDto company = ProductTaxMappingMap.mapToCompanyResponseInternalDto(companyResponse);

		ProductResponse productResponse = ProductTaxMappingMap.mapToProductResponse(product);

		ProductVariantWithTaxMappingsResponse finalResponse = ProductTaxMappingMap
				.mapToProductWithTaxMappingsResponseForVariant(variant, company, productResponse, taxMappingSummaries);

		return new ApiResponse<>(true, "Variant with tax mappings fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), finalResponse);
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse<?> getByCompanyIdAndTaxCategoryId(UUID companyId, UUID taxCategoryId) {

		if (companyId == null || taxCategoryId == null) {
			throw new IllegalArgumentException("CompanyId and TaxCategoryId must be provided");
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
		CompanyResponseInternalDto companyDto = ProductTaxMappingMap.mapToCompanyResponseInternalDto(companyResponse);

		CompanyTaxCategory taxCategory = companyTaxCategoryRepository.findByIdAndDeletedAtIsNull(taxCategoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Tax category not found"));

		if (!companyId.equals(taxCategory.getCompanyId())) {
			throw new IllegalArgumentException("Tax category does not belong to given company");
		}
		if (!Boolean.TRUE.equals(taxCategory.getActive())) {
			throw new InactiveResourceException("Tax category is inactive");
		}

		List<TaxComponent> taxComponents = taxComponentRepository
				.findAllByCompanyTaxCategoryIdAndActiveTrueAndDeletedAtIsNull(taxCategoryId);
		List<TaxComponentResponse> taxComponentResponses = taxComponents.stream().map(t -> {
			TaxComponentResponse r = new TaxComponentResponse();
			r.setTaxComponentId(t.getId());
			r.setComponentType(t.getComponentType());
			r.setComponentRate(t.getComponentRate());
			r.setActive(t.getActive());
			return r;
		}).toList();

		List<ProductTaxMapping> mappings = productTaxMappingRepository
				.findAllByCompanyIdAndCompanyTaxCategoryIdAndDeletedAtIsNull(companyId, taxCategoryId);

		Set<UUID> productIds = mappings.stream().map(ProductTaxMapping::getProductId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Set<UUID> variantIds = mappings.stream().map(ProductTaxMapping::getProductVariantId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		List<ProductResponse> products = productIds.isEmpty() ? List.of()
				: productRepository.findByIdInAndDeletedAtIsNullAndActiveTrue(new ArrayList<>(productIds)).stream()
						.map(ProductVariantsMapping::mapToProductResponse).toList();

		List<ProductVariantResponse> variants = variantIds.isEmpty() ? List.of()
				: productVariantRepository.findByIdInAndDeletedAtIsNullAndActiveTrue(new ArrayList<>(variantIds))
						.stream().map(ProductVariantsMapping::mapToProductVariantResponse).toList();

		CompanyTaxCategoryWithComponentsCompanyProductsVariants resultDto = ProductTaxMappingMap
				.mapToCompanyTaxCategoryWithComponentsCompanyProductsVariants(taxCategory, taxComponentResponses,
						companyDto, products, variants);

		return new ApiResponse<>(true, "Tax category with associated products & variants fetched successfully",
				HttpStatus.OK.name(), HttpStatus.OK.value(), resultDto);

	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse<?> getAllByCompanyId(UUID companyId, int page, int size) {

		if (companyId == null) {
			throw new IllegalArgumentException("CompanyId must be provided");
		}

		var response = orgServiceClient.getCompanyBasic(companyId);
		if (response == null || response.getBody() == null || response.getBody().getData() == null) {
			throw new ExternalServiceException("Invalid response from Organization Service",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		CompanyResponseExternalDto companyResponse = response.getBody().getData();

		if (Boolean.FALSE.equals(companyResponse.getActive())) {
			throw new InactiveResourceException("Company is inactive");
		}

		CompanyResponseInternalDto company = ProductTaxMappingMap.mapToCompanyResponseInternalDto(companyResponse);

		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

		Page<ProductTaxMapping> mappingsPage = productTaxMappingRepository
				.findAllByCompanyIdAndDeletedAtIsNull(companyId, pageable);

		List<ProductTaxMapping> mappings = mappingsPage.getContent();

		if (mappings.isEmpty()) {

			PageMeta emptyMeta = ProductTaxMappingMap.mapToPageMeta(mappingsPage);

			PaginatedResponse<ProductTaxMappingResponseWithCompanyProductProductVariantCompanyTaxCategory> emptyPage = new PaginatedResponse<>(
					List.of(), emptyMeta);

			return new ApiResponse<>(true, "No tax mappings found", HttpStatus.OK.name(), HttpStatus.OK.value(),
					new CompanyTaxMappingsPageResponse(company, emptyPage));
		}

		Set<UUID> productIds = mappings.stream().map(ProductTaxMapping::getProductId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Set<UUID> variantIds = mappings.stream().map(ProductTaxMapping::getProductVariantId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Set<UUID> taxCategoryIds = mappings.stream().map(ProductTaxMapping::getCompanyTaxCategoryId)
				.collect(Collectors.toSet());

		Map<UUID, Product> productMap = productIds.isEmpty() ? Collections.emptyMap()
				: productRepository.findByIdInAndDeletedAtIsNullAndActiveTrue(new ArrayList<>(productIds)).stream()
						.collect(Collectors.toMap(Product::getId, p -> p));

		Map<UUID, ProductVariant> variantMap = variantIds.isEmpty() ? Collections.emptyMap()
				: productVariantRepository.findByIdInAndDeletedAtIsNullAndActiveTrue(new ArrayList<>(variantIds))
						.stream().collect(Collectors.toMap(ProductVariant::getId, v -> v));

		Map<UUID, CompanyTaxCategory> taxCategoryMap = companyTaxCategoryRepository
				.findByIdInAndDeletedAtIsNull(taxCategoryIds).stream()
				.collect(Collectors.toMap(CompanyTaxCategory::getId, c -> c));

		Map<UUID, List<TaxComponent>> taxComponentMap = taxComponentRepository
				.findByCompanyTaxCategoryIdInAndDeletedAtIsNull(new ArrayList<>(taxCategoryIds)).stream()
				.collect(Collectors.groupingBy(TaxComponent::getCompanyTaxCategoryId));

		List<ProductTaxMappingResponseWithCompanyProductProductVariantCompanyTaxCategory> dtoList = mappings.stream()
				.map(mapping -> {

					Product product = productMap.get(mapping.getProductId());
					ProductVariant variant = variantMap.get(mapping.getProductVariantId());
					CompanyTaxCategory taxCategory = taxCategoryMap.get(mapping.getCompanyTaxCategoryId());
					List<TaxComponent> components = taxCategory != null
							? taxComponentMap.getOrDefault(taxCategory.getId(), List.of())
							: List.of();

					ProductResponse productResponse = product != null
							? ProductVariantsMapping.mapToProductResponse(product)
							: null;

					ProductVariantResponse variantResponse = variant != null
							? ProductVariantsMapping.mapToProductVariantResponse(variant)
							: null;

					List<TaxComponentResponse> taxComponentResponses = components != null
							? components.stream().map(c -> {
								TaxComponentResponse r = new TaxComponentResponse();
								r.setTaxComponentId(c.getId());
								r.setComponentType(c.getComponentType());
								r.setComponentRate(c.getComponentRate());
								r.setActive(c.getActive());
								return r;
							}).toList()
							: null;

					CompanyTaxCategoryResponse taxCategoryResponse = taxCategory != null
							? CompanyTaxCategoryMapping.mapToCompanyTaxCategoryResponse(taxCategory,
									taxComponentResponses)
							: null;

					return ProductTaxMappingMap
							.mapToProductTaxMappingResponseWithCompanyProductProductVariantCompanyTaxCategory(mapping,
									null, productResponse, variantResponse, taxCategoryResponse);
				}).toList();

		PageMeta pageMeta = ProductTaxMappingMap.mapToPageMeta(mappingsPage);

		PaginatedResponse<ProductTaxMappingResponseWithCompanyProductProductVariantCompanyTaxCategory> paginated = new PaginatedResponse<>(
				dtoList, pageMeta);

		CompanyTaxMappingsPageResponse finalResponse = new CompanyTaxMappingsPageResponse(company, paginated);

		return new ApiResponse<>(true, "Product tax mappings fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), finalResponse);
	}

	@Override
	@Transactional
	public ApiResponse<List<Object>> createBulk(ProductTaxMappingBulkCreateRequest request, UUID createdBy) {

		if (request.getProductTaxMapping() == null || request.getProductTaxMapping().isEmpty()) {
			throw new IllegalArgumentException("Request list cannot be empty");
		}

		List<Object> entitiesToSave = new ArrayList<>();

		for (ProductTaxMappingCreateRequest item : request.getProductTaxMapping()) {

			ApiResponse<?> productTaxMapping = createProductTaxMapping(item, createdBy);
			entitiesToSave.add(productTaxMapping.getData());
		}

		return new ApiResponse<>(true, "Bulk tax mapping with product created successfully", HttpStatus.CREATED.name(),
				HttpStatus.CREATED.value(), entitiesToSave);
	}

	@Override
	@Transactional
	public ApiResponse<Void> deleteBulk(List<UUID> ids, UUID deletedBy) {
		List<ProductTaxMapping> mappings = productTaxMappingRepository.findAllByIdInAndDeletedAtIsNull(ids);

		mappings.forEach(mapping -> {
			mapping.setDeletedAt(LocalDateTime.now());
			mapping.setDeletedBy(deletedBy);
		});

		productTaxMappingRepository.saveAll(mappings);

		return new ApiResponse<>(true, "Product tax mappings deleted successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), null);
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse<Boolean> existsMapping(UUID companyId, UUID productId, UUID variantId, UUID taxCategoryId) {

		if ((productId == null && variantId == null) || (productId != null && variantId != null)) {
			throw new IllegalArgumentException("Exactly one of productId or variantId must be provided");
		}

		boolean exists;

		if (productId != null) {
			exists = productTaxMappingRepository.existsByCompanyIdAndProductIdAndCompanyTaxCategoryId(companyId,
					productId, taxCategoryId);
		} else {
			exists = productTaxMappingRepository.existsByCompanyIdAndProductVariantIdAndCompanyTaxCategoryId(companyId,
					variantId, taxCategoryId);
		}

		return new ApiResponse<>(true, "Existence check completed", HttpStatus.OK.name(), HttpStatus.OK.value(),
				exists);
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse<?> searchProductTaxMappings(UUID companyId, UUID productId, UUID variantId, UUID taxCategoryId,
			boolean includeInactiveCompanies, boolean includeInactiveProducts, boolean includeInactiveVariants,
			boolean includeInactiveCompanyTaxCategories, int page, int size, String sortBy, String sortDir) {

		if (productId != null && variantId != null)
			throw new IllegalArgumentException("Cannot filter by both productId and variantId");

		CompanyResponseExternalDto companyResponse = null;
		if (companyId != null) {
			var response = orgServiceClient.getCompanyBasic(companyId);
			companyResponse = response.getBody().getData();

			if (!includeInactiveCompanies && Boolean.FALSE.equals(companyResponse.getActive())) {
				throw new InactiveResourceException("Company inactive");
			}
		}

		Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

		Pageable pageable = PageRequest.of(page, size, sort);

		Specification<ProductTaxMapping> spec = ProductTaxMappingSpecification.search(companyId, productId, variantId,
				taxCategoryId);

		Page<ProductTaxMapping> pageResult = productTaxMappingRepository.findAll(spec, pageable);

		List<ProductTaxMapping> mappings = pageResult.getContent();

		if (mappings.isEmpty()) {
			return new ApiResponse<>(true, "No data found", HttpStatus.OK.name(), HttpStatus.OK.value(), pageResult);
		}

		Set<UUID> companyIds = mappings.stream().map(ProductTaxMapping::getCompanyId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		List<CompanyResponseExternalDto> companiesExternal = Optional
				.ofNullable(orgServiceClient
						.getCompaniesDetailsByCompanyIds(new CompanyIdsRequestDto(new ArrayList<>(companyIds))))
				.map(r -> r.getBody()).map(ApiResponse::getData).orElse(Collections.emptyList());

		Map<UUID, CompanyResponseExternalDto> companyMap = companiesExternal.stream().filter(Objects::nonNull)
				.filter(c -> includeInactiveCompanies || Boolean.TRUE.equals(c.getActive()))
				.collect(Collectors.toMap(CompanyResponseExternalDto::getCompanyId, c -> c));

		Set<UUID> productIds = mappings.stream().map(ProductTaxMapping::getProductId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Map<UUID, Product> productMap = Optional
				.ofNullable(productRepository.findByIdInAndDeletedAtIsNull(new ArrayList<>(productIds)))
				.orElse(Collections.emptyList()).stream()
				.filter(p -> includeInactiveProducts || Boolean.TRUE.equals(p.getActive()))
				.collect(Collectors.toMap(Product::getId, p -> p));

		Set<UUID> variantIds = mappings.stream().map(ProductTaxMapping::getProductVariantId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Map<UUID, ProductVariant> variantMap = Optional
				.ofNullable(productVariantRepository.findByIdInAndDeletedAtIsNull(new ArrayList<>(variantIds)))
				.orElse(Collections.emptyList()).stream()
				.filter(v -> includeInactiveVariants || Boolean.TRUE.equals(v.getActive()))
				.collect(Collectors.toMap(ProductVariant::getId, v -> v));

		Set<UUID> taxIds = mappings.stream().map(ProductTaxMapping::getCompanyTaxCategoryId)
				.collect(Collectors.toSet());

		Map<UUID, CompanyTaxCategory> taxCategoryMap = Optional
				.ofNullable(companyTaxCategoryRepository.findByIdInAndDeletedAtIsNull(taxIds))
				.orElse(Collections.emptyList()).stream()
				.filter(c -> includeInactiveCompanyTaxCategories || Boolean.TRUE.equals(c.getActive()))
				.collect(Collectors.toMap(CompanyTaxCategory::getId, c -> c));

		mappings = mappings.stream()
			    .filter(m -> m.getCompanyId() == null || companyMap.containsKey(m.getCompanyId()))
			    .filter(m -> m.getProductId() == null || productMap.containsKey(m.getProductId()))
			    .filter(m -> m.getProductVariantId() == null || variantMap.containsKey(m.getProductVariantId()))
			    .filter(m -> m.getCompanyTaxCategoryId() == null
			            || taxCategoryMap.containsKey(m.getCompanyTaxCategoryId()))
			    .toList();
		
		Map<UUID, List<TaxComponent>> taxComponentMap = taxComponentRepository
				.findByCompanyTaxCategoryIdInAndDeletedAtIsNull(new ArrayList<>(taxIds)).stream()
				.collect(Collectors.groupingBy(TaxComponent::getCompanyTaxCategoryId));

		List<ProductTaxMappingResponseWithCompanyProductProductVariantCompanyTaxCategory> finalResponse = mappings
				.stream().map(m -> {

					CompanyResponseExternalDto companyDto = companyMap.get(m.getCompanyId());
					Product product = productMap.get(m.getProductId());
					ProductVariant productVariant = variantMap.get(m.getProductVariantId());
					CompanyTaxCategory taxCategory = taxCategoryMap.get(m.getCompanyTaxCategoryId());
					List<TaxComponent> components = taxCategory != null
							? taxComponentMap.getOrDefault(taxCategory.getId(), List.of())
							: List.of();
					CompanyResponseInternalDto companyResponseInternalDto = companyDto != null
							? ProductTaxMappingMap.mapToCompanyResponseInternalDto(companyDto)
							: null;
					
					ProductResponse productResponse = product != null ? ProductTaxMappingMap.mapToProductResponse(product) : null;
					
					ProductVariantResponse productVariantResponse = productVariant != null ? ProductVariantsMapping.mapToProductVariantResponse(productVariant) : null;
					
					List<TaxComponentResponse> taxComponentResponses = components != null
							? components.stream().map(c -> {
								TaxComponentResponse r = new TaxComponentResponse();
								r.setTaxComponentId(c.getId());
								r.setComponentType(c.getComponentType());
								r.setComponentRate(c.getComponentRate());
								r.setActive(c.getActive());
								return r;
							}).toList()
							: null;

					CompanyTaxCategoryResponse taxCategoryResponse = taxCategory != null
							? CompanyTaxCategoryMapping.mapToCompanyTaxCategoryResponse(taxCategory,
									taxComponentResponses)
							: null;
					
					return ProductTaxMappingMap.mapToProductTaxMappingResponseWithCompanyProductProductVariantCompanyTaxCategory(m, companyResponseInternalDto, productResponse, productVariantResponse, taxCategoryResponse);
				}).toList();

		PageMeta pageMeta = ProductTaxMappingMap.mapToPageMeta(pageResult);

		PaginatedResponse<ProductTaxMappingResponseWithCompanyProductProductVariantCompanyTaxCategory> paginated = new PaginatedResponse<>(
				finalResponse, pageMeta);
		
		return new ApiResponse<>(true, "Search completed successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				paginated);
	}

}
