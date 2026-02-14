package com.nector.catalogservice.service.impl;

import java.math.BigDecimal;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nector.catalogservice.client.OrgServiceClient;
import com.nector.catalogservice.dto.request.external.CompanyIdsRequestDto;
import com.nector.catalogservice.dto.request.internal.BulkVariantsStatusRequest;
import com.nector.catalogservice.dto.request.internal.ProductVariantBulkUpdateRequest;
import com.nector.catalogservice.dto.request.internal.ProductVariantCreateRequest;
import com.nector.catalogservice.dto.request.internal.ProductVariantPriceUpdateRequest;
import com.nector.catalogservice.dto.request.internal.ProductVariantUpdateRequest;
import com.nector.catalogservice.dto.response.external.CompanyResponseExternalDto;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CompanyProductVariantsResponse;
import com.nector.catalogservice.dto.response.internal.CompanyResponseInternalDto;
import com.nector.catalogservice.dto.response.internal.ProductResponse;
import com.nector.catalogservice.dto.response.internal.ProductResponseWithProductVariants;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponseWithCompanyAndUom;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponseWithProductAndUom;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponseWithProductCompanyUom;
import com.nector.catalogservice.dto.response.internal.UomResponse;
import com.nector.catalogservice.entity.Product;
import com.nector.catalogservice.entity.ProductVariant;
import com.nector.catalogservice.entity.Uom;
import com.nector.catalogservice.exception.DuplicateResourceException;
import com.nector.catalogservice.exception.ExternalServiceException;
import com.nector.catalogservice.exception.InactiveResourceException;
import com.nector.catalogservice.exception.ResourceNotFoundException;
import com.nector.catalogservice.mapper.ProductVariantsMapping;
import com.nector.catalogservice.repository.ProductRepository;
import com.nector.catalogservice.repository.ProductVariantRepository;
import com.nector.catalogservice.repository.UomRepository;
import com.nector.catalogservice.service.ProductVariantService;
import com.nector.catalogservice.specification.ProductVariantSpecification;

import jakarta.validation.Valid;
import jakarta.ws.rs.BadRequestException;
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
	public ApiResponse<ProductVariantResponseWithProductCompanyUom> createProductVariant(ProductVariantCreateRequest request,
			UUID createdBy) {

		if (request == null) {
			throw new IllegalArgumentException("ProductVariantCreateRequest cannot be null");
		}

		if (createdBy == null) {
			throw new IllegalArgumentException("CreatedBy cannot be null");
		}

		validateVariantBehaviour(request);

		productVariantRepository.findBySkuCodeAndCompanyId(request.getSkuCode(), request.getCompanyId())
				.ifPresent(v -> {
					throw new DuplicateResourceException("Variant SKU already exists for this company");
				});

		Product product = productRepository.findByIdAndDeletedAtIsNull(request.getProductId())
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		if (!Boolean.TRUE.equals(product.getActive())) {
			throw new InactiveResourceException("Product is inactive");
		}

		var response = orgServiceClient.getCompanyBasic(request.getCompanyId());

		if (response == null || response.getBody() == null || response.getBody().getData() == null) {
			throw new ExternalServiceException("Invalid response from Organization Service",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		CompanyResponseExternalDto companyResponse = response.getBody().getData();

		Uom uom = uomRepository.findByIdAndDeletedAtIsNull(request.getUomId())
				.orElseThrow(() -> new ResourceNotFoundException("UOM not found"));

		if (!Boolean.TRUE.equals(uom.getActive())) {
			throw new InactiveResourceException("UOM is inactive");
		}

		ProductVariant productVariant = ProductVariantsMapping.toEntityWithCreation(request, createdBy);

		validatePrices(productVariant);

		ProductVariant saved = productVariantRepository.save(productVariant);

		CompanyResponseInternalDto companyResponseInternalDto = ProductVariantsMapping
				.mapToCompanyResponseInternalDto(companyResponse);

		ProductResponse productResponse = ProductVariantsMapping.mapToProductResponse(product);

		Uom baseUom = null;
		if (uom.getBaseUomId() != null) {
			baseUom = uomRepository.findByIdAndDeletedAtIsNull(uom.getBaseUomId()).orElse(null);
		}

		UomResponse uomResponse = ProductVariantsMapping.mapToUomResponse(uom, baseUom);

		ProductVariantResponseWithProductCompanyUom productVariantResponse = ProductVariantsMapping.mapToProductVariantResponse(saved,
				productResponse, companyResponseInternalDto, uomResponse);

		return new ApiResponse<>(true, "Product variant created successfully", HttpStatus.CREATED.name(),
				HttpStatus.CREATED.value(), productVariantResponse);
	}

	@Transactional
	@Override
	public ApiResponse<ProductVariantResponseWithProductCompanyUom> updateProductVariant(UUID variantId,
			@Valid ProductVariantUpdateRequest request, UUID updatedBy) {

		if (request == null) {
			throw new IllegalArgumentException("ProductVariantUpdateRequest cannot be null");
		}

		if (updatedBy == null) {
			throw new IllegalArgumentException("updatedBy cannot be null");
		}

		ProductVariant variant = productVariantRepository.findById(variantId)
				.orElseThrow(() -> new ResourceNotFoundException("Product variant not found"));

		if (variant.getDeletedAt() != null) {
			throw new ResourceNotFoundException("Product variant not found");
		}

		if (request.getVariantName() != null)
			variant.setVariantName(request.getVariantName());
		if (request.getColor() != null)
			variant.setColor(request.getColor());
		if (request.getSize() != null)
			variant.setSize(request.getSize());
		if (request.getCustomAttributes() != null)
			variant.setCustomAttributes(request.getCustomAttributes());
		if (request.getMrp() != null)
			variant.setMrp(request.getMrp());
		if (request.getSellingPrice() != null)
			variant.setSellingPrice(request.getSellingPrice());
		if (request.getPurchasePrice() != null)
			variant.setPurchasePrice(request.getPurchasePrice());
		if (request.getSerialized() != null)
			variant.setSerialized(request.getSerialized());
		if (request.getBatchTracked() != null)
			variant.setBatchTracked(request.getBatchTracked());
		if (request.getExpiryTracked() != null)
			variant.setExpiryTracked(request.getExpiryTracked());
		if (request.getActive() != null)
			variant.setActive(request.getActive());

		validateVariantBehaviourForUpdate(variant);
		validatePrices(variant);

		if (request.getUomId() != null) {
			Uom uom = uomRepository.findByIdAndDeletedAtIsNull(request.getUomId())
					.orElseThrow(() -> new ResourceNotFoundException("UOM not found"));

			if (!Boolean.TRUE.equals(uom.getActive())) {
				throw new InactiveResourceException("UOM is inactive");
			}

			variant.setUomId(uom.getId());

			if (request.getConversionFactor() != null) {
				variant.setConversionFactor(request.getConversionFactor());
			}

		} else if (request.getConversionFactor() != null) {
			variant.setConversionFactor(request.getConversionFactor());
		}

		variant.setUpdatedBy(updatedBy);

		ProductVariant saved = productVariantRepository.save(variant);

		Product product = productRepository.findByIdAndDeletedAtIsNull(saved.getProductId())
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		var response = orgServiceClient.getCompanyBasic(saved.getCompanyId());
		if (response == null || response.getBody() == null || response.getBody().getData() == null) {
			throw new ExternalServiceException("Invalid response from Organization Service",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		CompanyResponseExternalDto companyResponse = response.getBody().getData();

		CompanyResponseInternalDto companyResponseInternalDto = ProductVariantsMapping
				.mapToCompanyResponseInternalDto(companyResponse);

		ProductResponse productResponse = ProductVariantsMapping.mapToProductResponse(product);

		Uom uom = uomRepository.findByIdAndDeletedAtIsNull(saved.getUomId()).orElse(null);

		Uom baseUom = null;
		if (uom != null && uom.getBaseUomId() != null) {
			baseUom = uomRepository.findByIdAndDeletedAtIsNull(uom.getBaseUomId()).orElse(null);
		}

		UomResponse uomResponse = ProductVariantsMapping.mapToUomResponse(uom, baseUom);

		ProductVariantResponseWithProductCompanyUom productVariantResponse = ProductVariantsMapping.mapToProductVariantResponse(saved,
				productResponse, companyResponseInternalDto, uomResponse);

		return new ApiResponse<>(true, "Product variant updated successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), productVariantResponse);
	}

	@Transactional
	@Override
	public ApiResponse<Void> deleteProductVariant(UUID variantId, UUID deletedBy) {

		if (variantId == null) {
			throw new IllegalArgumentException("variantId cannot be null");
		}

		if (deletedBy == null) {
			throw new IllegalArgumentException("deletedBy cannot be null");
		}

		ProductVariant variant = productVariantRepository.findById(variantId)
				.orElseThrow(() -> new ResourceNotFoundException("Product variant not found"));

		if (variant.getDeletedAt() != null) {
			throw new ResourceNotFoundException("Product variant already deleted");
		}

		variant.setDeletedAt(LocalDateTime.now());
		variant.setDeletedBy(deletedBy);
		variant.setActive(false);

		productVariantRepository.save(variant);

		return new ApiResponse<>(true, "Product variant deleted successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), null);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<ProductVariantResponseWithProductCompanyUom> getVariantByVariantId(UUID variantId) {

		if (variantId == null) {
			throw new IllegalArgumentException("variantId cannot be null");
		}

		ProductVariant variant = productVariantRepository.findByIdAndDeletedAtIsNull(variantId)
				.orElseThrow(() -> new ResourceNotFoundException("Product variant not found"));

		if (!variant.getActive()) {
			throw new InactiveResourceException("Product variant is inactive");
		}

		UUID productId = variant.getProductId();
		if (productId == null) {
			throw new InactiveResourceException("Product ID missing in variant");
		}

		Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		if (!product.getActive()) {
			throw new InactiveResourceException("Product is inactive");
		}

		UUID companyId = variant.getCompanyId();
		if (companyId == null) {
			throw new InactiveResourceException("Company ID missing in variant");
		}

		var response = orgServiceClient.getCompanyBasic(companyId);

		if (response == null || response.getBody() == null || response.getBody().getData() == null) {
			throw new ExternalServiceException("Invalid response from Organization Service",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		CompanyResponseExternalDto companyResponse = response.getBody().getData();
		if (!companyResponse.getActive()) {
			throw new InactiveResourceException("Company is inactive");
		}

		CompanyResponseInternalDto companyResponseInternalDto = ProductVariantsMapping
				.mapToCompanyResponseInternalDto(companyResponse);

		ProductResponse productResponse = ProductVariantsMapping.mapToProductResponse(product);

		UUID uomId = variant.getUomId();
		if (uomId == null) {
			throw new InactiveResourceException("UOM ID missing in variant");
		}

		Uom uom = uomRepository.findByIdAndDeletedAtIsNull(uomId).filter(Uom::getActive)
				.orElseThrow(() -> new InactiveResourceException("UOM is inactive"));

		Uom baseUom = null;
		if (uom.getBaseUomId() != null) {
			baseUom = uomRepository.findByIdAndDeletedAtIsNull(uom.getBaseUomId()).filter(Uom::getActive).orElse(null);
		}

		UomResponse uomResponse = ProductVariantsMapping.mapToUomResponse(uom, baseUom);

		ProductVariantResponseWithProductCompanyUom productVariantResponse = ProductVariantsMapping.mapToProductVariantResponse(variant,
				productResponse, companyResponseInternalDto, uomResponse);

		return new ApiResponse<>(true, "Product variant fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), productVariantResponse);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<?> getVariantsByProductId(UUID productId) {

		if (productId == null) {
			throw new IllegalArgumentException("productId cannot be null");
		}

		Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		if (!product.getActive()) {
			throw new InactiveResourceException("Product is inactive");
		}

		List<ProductVariant> variants = productVariantRepository
				.findByProductIdAndDeletedAtIsNullAndActiveTrue(productId);

		if (variants == null || variants.isEmpty()) {
			return new ApiResponse<>(true, "No variants found for the product", HttpStatus.OK.name(),
					HttpStatus.OK.value(), List.of());
		}

		variants = variants.stream().filter(v -> v != null && v.getCompanyId() != null).toList();

		Set<UUID> companyIds = variants.stream().map(ProductVariant::getCompanyId).collect(Collectors.toSet());
		if (companyIds.isEmpty()) {
			return new ApiResponse<>(true, "No valid companies found for the product variants", HttpStatus.OK.name(),
					HttpStatus.OK.value(), List.of());
		}

		CompanyIdsRequestDto companyIdsRequestDto = new CompanyIdsRequestDto();
		companyIdsRequestDto.setCompanyIds(new ArrayList<>(companyIds));

		ResponseEntity<ApiResponse<List<CompanyResponseExternalDto>>> companyResponseRaw = orgServiceClient
				.getCompaniesDetailsByCompanyIds(companyIdsRequestDto);

		if (companyResponseRaw == null || companyResponseRaw.getBody() == null
				|| companyResponseRaw.getBody().getData() == null) {
			throw new ExternalServiceException("Invalid response from Organization Service",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		List<CompanyResponseInternalDto> activeCompanies = companyResponseRaw.getBody().getData().stream()
				.filter(c -> c != null).map(ProductVariantsMapping::mapToCompanyResponseInternalDto)
				.filter(CompanyResponseInternalDto::getActive).toList();

		Map<UUID, CompanyResponseInternalDto> companyMap = activeCompanies.stream()
				.collect(Collectors.toMap(CompanyResponseInternalDto::getCompanyId, c -> c));

		variants = variants.stream().filter(v -> v.getCompanyId() != null && companyMap.containsKey(v.getCompanyId()))
				.toList();

		if (variants.isEmpty()) {
			return new ApiResponse<>(true, "No active variants found for the product", HttpStatus.OK.name(),
					HttpStatus.OK.value(), List.of());
		}

		variants = variants.stream().filter(v -> v.getUomId() != null).toList();

		Set<UUID> uomIds = variants.stream().map(ProductVariant::getUomId).collect(Collectors.toSet());
		List<Uom> activeUoms = uomRepository.findByIdInAndDeletedAtIsNull(uomIds).stream()
				.filter(u -> u != null && u.getActive()).toList();

		Map<UUID, Uom> uomMap = activeUoms.stream().collect(Collectors.toMap(Uom::getId, u -> u));

		variants = variants.stream().filter(v -> v.getUomId() != null && uomMap.containsKey(v.getUomId())).toList();

		if (variants.isEmpty()) {
			return new ApiResponse<>(true, "No variants with active UOM found for the product", HttpStatus.OK.name(),
					HttpStatus.OK.value(), List.of());
		}

		Set<UUID> baseUomIds = activeUoms.stream().map(Uom::getBaseUomId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		List<Uom> activeBaseUoms = uomRepository.findByIdInAndDeletedAtIsNull(baseUomIds).stream()
				.filter(Uom::getActive).toList();

		Map<UUID, Uom> baseUomMap = activeBaseUoms.stream().collect(Collectors.toMap(Uom::getId, u -> u));

		List<ProductVariantResponseWithCompanyAndUom> variantResponses = ProductVariantsMapping
				.mapVariantsCompanyAndUom(variants, companyMap, uomMap, baseUomMap);

		ProductResponseWithProductVariants response = ProductVariantsMapping
				.mapProductResponseWithProductVariants(product, variantResponses);

		return new ApiResponse<>(true, "Product variants fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), response);
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse<Boolean> isSkuAvailable(String skuCode, UUID companyId) {

		if (skuCode == null || skuCode.isBlank()) {
			throw new IllegalArgumentException("SKU code cannot be null or empty");
		}

		if (companyId == null) {
			throw new IllegalArgumentException("Company ID cannot be null");
		}

		boolean exists = productVariantRepository.findBySkuCodeAndCompanyIdAndDeletedAtIsNull(skuCode, companyId)
				.isPresent();

		boolean available = !exists;

		return new ApiResponse<>(true, available ? "SKU is available" : "SKU already exists", HttpStatus.OK.name(),
				HttpStatus.OK.value(), available);
	}

	@Transactional
	@Override
	public ApiResponse<List<ProductVariantResponseWithProductCompanyUom>> bulkUpdateProductVariants(ProductVariantBulkUpdateRequest request,
			UUID updatedBy) {

		if (request == null || request.getVariants() == null || request.getVariants().isEmpty()) {
			return new ApiResponse<>(true, "No variants to update", HttpStatus.OK.name(), HttpStatus.OK.value(),
					List.of());
		}

		List<ProductVariantResponseWithProductCompanyUom> updatedResponses = new ArrayList<>();

		for (var item : request.getVariants()) {
			if (item == null || item.getVariantId() == null) {
				continue;
			}

			ProductVariant variant = productVariantRepository.findByIdAndDeletedAtIsNull(item.getVariantId())
					.orElseThrow(
							() -> new ResourceNotFoundException("Variant not found for ID: " + item.getVariantId()));

			if (!Boolean.TRUE.equals(variant.getActive())) {
				throw new InactiveResourceException("Variant is inactive: " + variant.getSkuCode());
			}

			if (item.getVariantName() != null)
				variant.setVariantName(item.getVariantName());
			if (item.getColor() != null)
				variant.setColor(item.getColor());
			if (item.getSize() != null)
				variant.setSize(item.getSize());
			if (item.getCustomAttributes() != null)
				variant.setCustomAttributes(item.getCustomAttributes());
			if (item.getMrp() != null)
				variant.setMrp(item.getMrp());
			if (item.getSellingPrice() != null)
				variant.setSellingPrice(item.getSellingPrice());
			if (item.getPurchasePrice() != null)
				variant.setPurchasePrice(item.getPurchasePrice());
			if (item.getSerialized() != null)
				variant.setSerialized(item.getSerialized());
			if (item.getBatchTracked() != null)
				variant.setBatchTracked(item.getBatchTracked());
			if (item.getExpiryTracked() != null)
				variant.setExpiryTracked(item.getExpiryTracked());
			if (item.getActive() != null)
				variant.setActive(item.getActive());

			variant.setUpdatedBy(updatedBy);

			if (variant.getMrp() != null && variant.getSellingPrice() != null
					&& variant.getMrp().compareTo(variant.getSellingPrice()) < 0) {
				throw new IllegalArgumentException(
						"Selling price cannot be greater than MRP for variant: " + variant.getSkuCode());
			}
			if (variant.getSellingPrice() != null && variant.getPurchasePrice() != null
					&& variant.getSellingPrice().compareTo(variant.getPurchasePrice()) < 0) {
				throw new IllegalArgumentException(
						"Selling price cannot be less than purchase price for variant: " + variant.getSkuCode());
			}
			if (Boolean.TRUE.equals(variant.getExpiryTracked()) && !Boolean.TRUE.equals(variant.getBatchTracked())) {
				throw new IllegalArgumentException(
						"Expiry tracked variant must be batch tracked: " + variant.getSkuCode());
			}
			if (Boolean.TRUE.equals(variant.getSerialized()) && !Boolean.TRUE.equals(variant.getBatchTracked())) {
				throw new IllegalArgumentException("Serialized variant must be batch tracked: " + variant.getSkuCode());
			}

			ProductVariant saved = productVariantRepository.save(variant);

			Product product = productRepository.findByIdAndDeletedAtIsNull(saved.getProductId()).orElseThrow(
					() -> new ResourceNotFoundException("Product not found for variant: " + variant.getSkuCode()));

			CompanyResponseExternalDto companyResponse = null;
			try {
				var companyRaw = orgServiceClient.getCompanyBasic(variant.getCompanyId());
				if (companyRaw != null && companyRaw.getBody() != null && companyRaw.getBody().getData() != null) {
					companyResponse = companyRaw.getBody().getData();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			CompanyResponseInternalDto company = companyResponse != null
					? ProductVariantsMapping.mapToCompanyResponseInternalDto(companyResponse)
					: null;

			Uom uom = (variant.getUomId() != null)
					? uomRepository.findByIdAndDeletedAtIsNull(variant.getUomId()).orElse(null)
					: null;

			Uom baseUom = (uom != null && uom.getBaseUomId() != null)
					? uomRepository.findByIdAndDeletedAtIsNull(uom.getBaseUomId()).orElse(null)
					: null;

			UomResponse uomResponse = ProductVariantsMapping.mapToUomResponse(uom, baseUom);
			ProductResponse productResponse = ProductVariantsMapping.mapToProductResponse(product);

			ProductVariantResponseWithProductCompanyUom response = ProductVariantsMapping.mapToProductVariantResponse(saved, productResponse,
					company, uomResponse);

			updatedResponses.add(response);
		}

		return new ApiResponse<>(true, "Variants updated successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				updatedResponses);
	}

	@Transactional
	@Override
	public ApiResponse<Void> bulkDeleteVariants(List<UUID> variantIds, UUID deletedBy) {

		if (variantIds == null || variantIds.isEmpty()) {
			return new ApiResponse<>(true, "No variant IDs provided", HttpStatus.OK.name(), HttpStatus.OK.value(),
					null);
		}

		if (deletedBy == null) {
			throw new IllegalArgumentException("DeletedBy cannot be null");
		}

		List<ProductVariant> variants = productVariantRepository.findAllByIdInAndDeletedAtIsNull(variantIds);

		if (variants.isEmpty()) {
			return new ApiResponse<>(true, "No variants found to delete", HttpStatus.OK.name(), HttpStatus.OK.value(),
					null);
		}

		if (variantIds.size() != variants.size()) {
			throw new ResourceNotFoundException("Some variants are not found");
		}

		variants.forEach(v -> {
			v.setDeletedAt(LocalDateTime.now());
			v.setDeletedBy(deletedBy);
			v.setActive(false);
		});

		productVariantRepository.saveAll(variants);

		return new ApiResponse<>(true, "Variants deleted successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				null);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<ProductVariantResponseWithProductCompanyUom> getVariantBySkuAndCompanyId(String skuCode, UUID companyId) {

		if (skuCode == null || skuCode.isBlank()) {
			throw new IllegalArgumentException("SKU code cannot be null or empty");
		}
		if (companyId == null) {
			throw new IllegalArgumentException("Company ID cannot be null");
		}

		ProductVariant variant = productVariantRepository
				.findBySkuCodeAndCompanyIdAndDeletedAtIsNull(skuCode, companyId)
				.orElseThrow(() -> new ResourceNotFoundException("Product variant not found"));

		if (!Boolean.TRUE.equals(variant.getActive())) {
			throw new InactiveResourceException("Product variant is inactive");
		}

		Product product = productRepository.findByIdAndDeletedAtIsNull(variant.getProductId())
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		if (!Boolean.TRUE.equals(product.getActive())) {
			throw new InactiveResourceException("Product is inactive");
		}

		var response = orgServiceClient.getCompanyBasic(variant.getCompanyId());
		if (response == null || response.getBody() == null || response.getBody().getData() == null) {
			throw new ExternalServiceException("Invalid response from Organization Service",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		CompanyResponseExternalDto companyResponse = response.getBody().getData();
		if (!Boolean.TRUE.equals(companyResponse.getActive())) {
			throw new InactiveResourceException("Company is inactive");
		}

		CompanyResponseInternalDto companyResponseInternalDto = ProductVariantsMapping
				.mapToCompanyResponseInternalDto(companyResponse);

		ProductResponse productResponse = ProductVariantsMapping.mapToProductResponse(product);

		if (variant.getUomId() == null) {
			throw new InactiveResourceException("UOM is missing for variant");
		}

		Uom uom = uomRepository.findByIdAndDeletedAtIsNull(variant.getUomId()).filter(Uom::getActive)
				.orElseThrow(() -> new InactiveResourceException("UOM is inactive"));

		Uom baseUom = null;
		if (uom.getBaseUomId() != null) {
			baseUom = uomRepository.findByIdAndDeletedAtIsNull(uom.getBaseUomId()).filter(Uom::getActive).orElse(null);
		}

		UomResponse uomResponse = ProductVariantsMapping.mapToUomResponse(uom, baseUom);

		ProductVariantResponseWithProductCompanyUom productVariantResponse = ProductVariantsMapping.mapToProductVariantResponse(variant,
				productResponse, companyResponseInternalDto, uomResponse);

		return new ApiResponse<>(true, "Product variant fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), productVariantResponse);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<?> getVariantsByCompanyId(UUID companyId) {

		if (companyId == null) {
			throw new IllegalArgumentException("Company ID cannot be null");
		}

		var companyResponseRaw = orgServiceClient.getCompanyBasic(companyId);
		if (companyResponseRaw == null || companyResponseRaw.getBody() == null
				|| companyResponseRaw.getBody().getData() == null) {
			throw new ExternalServiceException("Invalid response from Organization Service",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		CompanyResponseExternalDto company = companyResponseRaw.getBody().getData();
		if (company == null || Boolean.FALSE.equals(company.getActive())) {
			throw new InactiveResourceException("Company is inactive");
		}

		List<ProductVariant> variants = Optional
				.ofNullable(productVariantRepository.findByCompanyIdAndActiveTrueAndDeletedAtIsNull(companyId))
				.orElse(Collections.emptyList());

		if (variants.isEmpty()) {
			return new ApiResponse<>(true, "No active variants found for the company", HttpStatus.OK.name(),
					HttpStatus.OK.value(), List.of());
		}

		List<UUID> productIds = variants.stream().map(ProductVariant::getProductId).filter(Objects::nonNull).toList();

		Map<UUID, Product> productMap = Optional
				.ofNullable(productRepository.findByIdInAndDeletedAtIsNullAndActiveTrue(productIds))
				.orElse(Collections.emptyList()).stream().filter(Objects::nonNull)
				.collect(Collectors.toMap(Product::getId, p -> p));

		variants = variants.stream()
				.filter(v -> v != null && v.getProductId() != null && productMap.containsKey(v.getProductId()))
				.toList();

		if (variants.isEmpty()) {
			return new ApiResponse<>(true, "No active variants with active products found", HttpStatus.OK.name(),
					HttpStatus.OK.value(), List.of());
		}

		Set<UUID> uomIds = variants.stream().map(ProductVariant::getUomId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Map<UUID, Uom> uomMap = Optional.ofNullable(uomRepository.findByIdInAndDeletedAtIsNull(uomIds))
				.orElse(Collections.emptyList()).stream().filter(u -> u != null && Boolean.TRUE.equals(u.getActive()))
				.collect(Collectors.toMap(Uom::getId, u -> u));

		variants = variants.stream().filter(v -> v.getUomId() != null && uomMap.containsKey(v.getUomId())).toList();

		Set<UUID> baseUomIds = uomMap.values().stream().map(Uom::getBaseUomId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Map<UUID, Uom> baseUomMap = Optional.ofNullable(uomRepository.findByIdInAndDeletedAtIsNull(baseUomIds))
				.orElse(Collections.emptyList()).stream().filter(u -> u != null && Boolean.TRUE.equals(u.getActive()))
				.collect(Collectors.toMap(Uom::getId, u -> u));

		CompanyResponseInternalDto companyInternal = ProductVariantsMapping.mapToCompanyResponseInternalDto(company);

		List<ProductVariantResponseWithProductAndUom> responseList = variants.stream().map(v -> {
			if (v == null)
				return null;

			Product product = productMap.get(v.getProductId());
			Uom uom = uomMap.get(v.getUomId());
			Uom baseUom = (uom != null && uom.getBaseUomId() != null) ? baseUomMap.get(uom.getBaseUomId()) : null;

			ProductResponse productResponse = product != null ? ProductVariantsMapping.mapToProductResponse(product)
					: null;
			UomResponse uomResponse = uom != null ? ProductVariantsMapping.mapToUomResponse(uom, baseUom) : null;

			return ProductVariantsMapping.mapToProductVariantResponseWithProductAndUom(v, productResponse, uomResponse);

		}).filter(Objects::nonNull).toList();

		CompanyProductVariantsResponse response = ProductVariantsMapping.mapVariantsWithCompany(companyInternal,
				responseList);

		return new ApiResponse<>(true, "Active variants fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), response);
	}

	@Transactional
	@Override
	public ApiResponse<ProductVariantResponseWithProductCompanyUom> updateVariantPrice(UUID variantId,
			ProductVariantPriceUpdateRequest request, UUID updatedBy) {

		if (variantId == null) {
			throw new IllegalArgumentException("Variant ID cannot be null");
		}

		if (request == null) {
			throw new IllegalArgumentException("Price update request cannot be null");
		}

		ProductVariant variant = productVariantRepository.findByIdAndDeletedAtIsNull(variantId)
				.orElseThrow(() -> new ResourceNotFoundException("Product variant not found"));

		if (!Boolean.TRUE.equals(variant.getActive())) {
			throw new InactiveResourceException("Product variant is inactive");
		}

		if (request.getMrp() != null) {
			variant.setMrp(request.getMrp());
		}
		if (request.getSellingPrice() != null) {
			variant.setSellingPrice(request.getSellingPrice());
		}
		if (request.getPurchasePrice() != null) {
			variant.setPurchasePrice(request.getPurchasePrice());
		}

		variant.setUpdatedBy(updatedBy);

		validatePrices(variant);

		ProductVariant saved = productVariantRepository.save(variant);

		return getVariantByVariantId(saved.getId());
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<Long> countVariants(UUID companyId, UUID productId, Boolean active) {

		long count;

		if (companyId != null && productId != null && active != null) {
			count = productVariantRepository.countByCompanyIdAndProductIdAndActiveAndDeletedAtIsNull(companyId,
					productId, active);

		} else if (companyId != null && productId != null) {
			count = productVariantRepository.countByCompanyIdAndProductIdAndDeletedAtIsNull(companyId, productId);

		} else if (companyId != null && active != null) {
			count = productVariantRepository.countByCompanyIdAndActiveAndDeletedAtIsNull(companyId, active);

		} else if (productId != null && active != null) {
			count = productVariantRepository.countByProductIdAndActiveAndDeletedAtIsNull(productId, active);

		} else if (companyId != null) {
			count = productVariantRepository.countByCompanyIdAndDeletedAtIsNull(companyId);

		} else if (productId != null) {
			count = productVariantRepository.countByProductIdAndDeletedAtIsNull(productId);

		} else if (active != null) {
			count = productVariantRepository.countByActiveAndDeletedAtIsNull(active);

		} else {
			count = productVariantRepository.countByDeletedAtIsNull();
		}

		return new ApiResponse<>(true, "Variant count fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), count);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<Page<ProductVariantResponseWithProductCompanyUom>> getAllVariants(UUID companyId, UUID productId, Boolean active,
			Boolean serialized, Boolean batchTracked, String search, BigDecimal minPrice, BigDecimal maxPrice, int page,
			int size, String sortBy, String sortDir, boolean includeInactiveCompanies, boolean includeInactiveProducts,
			boolean includeInactiveUoms) {

		String sortField = sortBy != null ? sortBy : "createdAt";
		String sortDirection = sortDir != null ? sortDir : "asc";

		Sort sort = sortDirection.equalsIgnoreCase("desc") ? Sort.by(sortField).descending()
				: Sort.by(sortField).ascending();
		Pageable pageable = PageRequest.of(page, size, sort);

		Specification<ProductVariant> spec = ProductVariantSpecification.filterVariants(companyId, productId, active,
				serialized, batchTracked, search, minPrice, maxPrice);

		Page<ProductVariant> variantPage = productVariantRepository.findAll(spec, pageable);
		List<ProductVariant> variants = variantPage.getContent();

		if (variants.isEmpty()) {
			return new ApiResponse<>(true, "No variants found", HttpStatus.OK.name(), HttpStatus.OK.value(),
					Page.empty());
		}

		Set<UUID> companyIds = variants.stream().map(ProductVariant::getCompanyId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		List<CompanyResponseExternalDto> companiesExternal = Optional
				.ofNullable(orgServiceClient
						.getCompaniesDetailsByCompanyIds(new CompanyIdsRequestDto(new ArrayList<>(companyIds))))
				.map(r -> r.getBody()).map(ApiResponse::getData).orElse(Collections.emptyList());

		Map<UUID, CompanyResponseInternalDto> companyMap = companiesExternal.stream()
				.map(ProductVariantsMapping::mapToCompanyResponseInternalDto).filter(Objects::nonNull)
				.filter(c -> includeInactiveCompanies || Boolean.TRUE.equals(c.getActive()))
				.collect(Collectors.toMap(CompanyResponseInternalDto::getCompanyId, c -> c));

		List<UUID> productIds = variants.stream().map(ProductVariant::getProductId).filter(Objects::nonNull).toList();

		Map<UUID, Product> productMap = Optional.ofNullable(productRepository.findByIdInAndDeletedAtIsNull(productIds))
				.orElse(Collections.emptyList()).stream()
				.filter(p -> includeInactiveProducts || Boolean.TRUE.equals(p.getActive()))
				.collect(Collectors.toMap(Product::getId, p -> p));

		Set<UUID> uomIds = variants.stream().map(ProductVariant::getUomId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Map<UUID, Uom> uomMap = Optional.ofNullable(uomRepository.findByIdInAndDeletedAtIsNull(uomIds))
				.orElse(Collections.emptyList()).stream().filter(u -> u != null)
				.filter(u -> includeInactiveUoms || Boolean.TRUE.equals(u.getActive()))
				.collect(Collectors.toMap(Uom::getId, u -> u));

		Set<UUID> baseUomIds = uomMap.values().stream().map(Uom::getBaseUomId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Map<UUID, Uom> baseUomMap = Optional.ofNullable(uomRepository.findByIdInAndDeletedAtIsNull(baseUomIds))
				.orElse(Collections.emptyList()).stream().filter(u -> u != null)
				.filter(u -> includeInactiveUoms || Boolean.TRUE.equals(u.getActive()))
				.collect(Collectors.toMap(Uom::getId, u -> u));

		variants = variants.stream().filter(v -> v.getCompanyId() != null && companyMap.containsKey(v.getCompanyId()))
				.filter(v -> v.getProductId() != null && productMap.containsKey(v.getProductId()))
				.filter(v -> v.getUomId() != null && uomMap.containsKey(v.getUomId())).toList();

		List<ProductVariantResponseWithProductCompanyUom> variantResponses = variants.stream().map(v -> {
			Product product = productMap.get(v.getProductId());
			CompanyResponseInternalDto company = companyMap.get(v.getCompanyId());
			Uom uom = uomMap.get(v.getUomId());
			Uom baseUom = (uom != null && uom.getBaseUomId() != null) ? baseUomMap.get(uom.getBaseUomId()) : null;

			ProductResponse productResponse = product != null ? ProductVariantsMapping.mapToProductResponse(product)
					: null;
			UomResponse uomResponse = uom != null ? ProductVariantsMapping.mapToUomResponse(uom, baseUom) : null;

			return ProductVariantsMapping.mapToProductVariantResponse(v, productResponse, company, uomResponse);
		}).filter(Objects::nonNull).toList();

		Page<ProductVariantResponseWithProductCompanyUom> responsePage = new PageImpl<>(variantResponses, pageable,
				variantPage.getTotalElements());

		return new ApiResponse<>(true, "Variants fetched successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				responsePage);
	}

	@Transactional
	@Override
	public ApiResponse<CompanyProductVariantsResponse> bulkUpdateProductStatusByCompany(UUID companyId,
			BulkVariantsStatusRequest request, boolean activeStatus, UUID updatedBy) {

		if (request.getVariantIds() == null || request.getVariantIds().isEmpty()) {
			throw new BadRequestException("Variant IDs cannot be empty");
		}

		var cResponse = orgServiceClient.getCompanyBasic(companyId);

		if (cResponse == null || cResponse.getBody() == null || cResponse.getBody().getData() == null) {
			throw new ResourceNotFoundException("Company not found");
		}

		CompanyResponseExternalDto companyExternal = cResponse.getBody().getData();
		if (companyExternal == null || Boolean.FALSE.equals(companyExternal.getActive())) {
			throw new InactiveResourceException("Company is inactive");
		}

		List<ProductVariant> variants = productVariantRepository.findByCompanyIdAndIdInAndDeletedAtIsNull(companyId,
				request.getVariantIds());

		if (variants.isEmpty()) {
			throw new ResourceNotFoundException("No variants found for this company");
		}

		if (variants.size() != request.getVariantIds().size()) {
			throw new ResourceNotFoundException("Some variants do not belong to this company or are deleted");
		}

		variants.forEach(v -> {
			if (!Boolean.valueOf(activeStatus).equals(v.getActive())) {
				v.setActive(activeStatus);
				v.setUpdatedBy(updatedBy);
			}
		});

		List<ProductVariant> savedVariants = productVariantRepository.saveAll(variants);

		List<UUID> productIds = savedVariants.stream().map(ProductVariant::getProductId).filter(Objects::nonNull)
				.toList();

		Map<UUID, Product> productMap = Optional.ofNullable(productRepository.findByIdInAndDeletedAtIsNull(productIds))
				.orElse(Collections.emptyList()).stream().filter(Objects::nonNull)
				.collect(Collectors.toMap(Product::getId, p -> p));

		Set<UUID> uomIds = savedVariants.stream().map(ProductVariant::getUomId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Map<UUID, Uom> uomMap = Optional.ofNullable(uomRepository.findByIdInAndDeletedAtIsNull(uomIds))
				.orElse(Collections.emptyList()).stream().collect(Collectors.toMap(Uom::getId, u -> u));

		Set<UUID> baseUomIds = uomMap.values().stream().map(Uom::getBaseUomId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Map<UUID, Uom> baseUomMap = Optional.ofNullable(uomRepository.findByIdInAndDeletedAtIsNull(baseUomIds))
				.orElse(Collections.emptyList()).stream().collect(Collectors.toMap(Uom::getId, u -> u));

		List<ProductVariantResponseWithProductAndUom> responseList = variants.stream().map(v -> {
			if (v == null)
				return null;

			Product product = productMap.get(v.getProductId());
			Uom uom = uomMap.get(v.getUomId());
			Uom baseUom = (uom != null && uom.getBaseUomId() != null) ? baseUomMap.get(uom.getBaseUomId()) : null;

			ProductResponse productResponse = product != null ? ProductVariantsMapping.mapToProductResponse(product)
					: null;
			UomResponse uomResponse = uom != null ? ProductVariantsMapping.mapToUomResponse(uom, baseUom) : null;

			return ProductVariantsMapping.mapToProductVariantResponseWithProductAndUom(v, productResponse, uomResponse);

		}).filter(Objects::nonNull).toList();

		CompanyProductVariantsResponse responseData = new CompanyProductVariantsResponse(companyExternal.getCompanyId(),
				companyExternal.getCompanyCode(), companyExternal.getCompanyName(), companyExternal.getActive(),
				responseList);

		String message = activeStatus ? "Variants activated successfully" : "Variants deactivated successfully";

		return new ApiResponse<>(true, message, HttpStatus.OK.name(), HttpStatus.OK.value(), responseData);
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse<CompanyProductVariantsResponse> getVariantsByCompanyAndStatus(UUID companyId,
			boolean activeStatus) {

		var cResponse = orgServiceClient.getCompanyBasic(companyId);

		if (cResponse == null || cResponse.getBody() == null || cResponse.getBody().getData() == null) {
			throw new ResourceNotFoundException("Company not found");
		}

		CompanyResponseExternalDto companyResponse = cResponse.getBody().getData();
		if (companyResponse == null || Boolean.FALSE.equals(companyResponse.getActive())) {
			throw new InactiveResourceException("Company is inactive");
		}

		List<ProductVariant> variants = activeStatus
				? productVariantRepository.findByCompanyIdAndDeletedAtIsNullAndActiveTrue(companyId)
				: productVariantRepository.findByCompanyIdAndDeletedAtIsNullAndActiveFalse(companyId);

		if (variants.isEmpty()) {

			CompanyProductVariantsResponse emptyResponse = new CompanyProductVariantsResponse(
					companyResponse.getCompanyId(), companyResponse.getCompanyCode(), companyResponse.getCompanyName(),
					companyResponse.getActive(), Collections.emptyList());

			String message = activeStatus ? "No active variants found for this company"
					: "No inactive variants found for this company";

			return new ApiResponse<>(true, message, HttpStatus.OK.name(), HttpStatus.OK.value(), emptyResponse);
		}

		List<UUID> productIds = variants.stream().map(ProductVariant::getProductId).filter(Objects::nonNull).toList();

		Map<UUID, Product> productMap = productRepository.findByIdInAndDeletedAtIsNull(productIds).stream()
				.collect(Collectors.toMap(Product::getId, p -> p));

		Set<UUID> uomIds = variants.stream().map(ProductVariant::getUomId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Map<UUID, Uom> uomMap = uomRepository.findByIdInAndDeletedAtIsNull(uomIds).stream()
				.collect(Collectors.toMap(Uom::getId, u -> u));

		Set<UUID> baseUomIds = uomMap.values().stream().map(Uom::getBaseUomId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Map<UUID, Uom> baseUomMap = uomRepository.findByIdInAndDeletedAtIsNull(baseUomIds).stream()
				.collect(Collectors.toMap(Uom::getId, u -> u));

		List<ProductVariantResponseWithProductAndUom> responseList = variants.stream().map(v -> {

			Product product = productMap.get(v.getProductId());
			Uom uom = uomMap.get(v.getUomId());
			Uom baseUom = (uom != null && uom.getBaseUomId() != null) ? baseUomMap.get(uom.getBaseUomId()) : null;

			ProductResponse productResponse = product != null ? ProductVariantsMapping.mapToProductResponse(product)
					: null;

			UomResponse uomResponse = uom != null ? ProductVariantsMapping.mapToUomResponse(uom, baseUom) : null;

			return ProductVariantsMapping.mapToProductVariantResponseWithProductAndUom(v, productResponse, uomResponse);
		}).toList();

		CompanyProductVariantsResponse response = new CompanyProductVariantsResponse(companyResponse.getCompanyId(),
				companyResponse.getCompanyCode(), companyResponse.getCompanyName(), companyResponse.getActive(),
				responseList);

		String message = activeStatus ? "Active variants fetched successfully" : "Inactive variants fetched successfully";
		return new ApiResponse<>(true, message, HttpStatus.OK.name(), HttpStatus.OK.value(),
				response);
	}

//	=========================================================================================
	private void validateVariantBehaviour(ProductVariantCreateRequest req) {
		if (Boolean.TRUE.equals(req.getExpiryTracked()) && !Boolean.TRUE.equals(req.getBatchTracked())) {
			throw new IllegalArgumentException("Expiry tracked variant must be batch tracked");
		}

		if (Boolean.TRUE.equals(req.getSerialized()) && !Boolean.TRUE.equals(req.getBatchTracked())) {
			throw new IllegalArgumentException("Serialized variant must be batch tracked");
		}
	}

	private void validateVariantBehaviourForUpdate(ProductVariant variant) {

		if (Boolean.TRUE.equals(variant.getExpiryTracked()) && !Boolean.TRUE.equals(variant.getBatchTracked())) {
			throw new IllegalArgumentException("Expiry tracked variant must be batch tracked");
		}

		if (Boolean.TRUE.equals(variant.getSerialized()) && !Boolean.TRUE.equals(variant.getBatchTracked())) {
			throw new IllegalArgumentException("Serialized variant must be batch tracked");
		}
	}

	private void validatePrices(ProductVariant variant) {

		if (variant.getSellingPrice().compareTo(variant.getMrp()) > 0) {
			throw new IllegalArgumentException("Selling price cannot be greater than MRP");
		}

		if (variant.getPurchasePrice().compareTo(variant.getSellingPrice()) > 0) {
			throw new IllegalArgumentException("Purchase price cannot be greater than selling price");
		}
	}

}
