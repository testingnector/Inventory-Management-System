package com.nector.catalogservice.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nector.catalogservice.client.OrgServiceClient;
import com.nector.catalogservice.dto.request.external.CompanyIdsRequestDto;
import com.nector.catalogservice.dto.request.internal.ProductVariantBulkUpdateRequest;
import com.nector.catalogservice.dto.request.internal.ProductVariantCreateRequest;
import com.nector.catalogservice.dto.request.internal.ProductVariantUpdateRequest;
import com.nector.catalogservice.dto.response.external.CompanyResponseExternalDto;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CompanyResponseInternalDto;
import com.nector.catalogservice.dto.response.internal.ProductResponse;
import com.nector.catalogservice.dto.response.internal.ProductResponseWithProductVariants;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponse;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponseWithCompanyAndUom;
import com.nector.catalogservice.dto.response.internal.UomResponse;
import com.nector.catalogservice.entity.Product;
import com.nector.catalogservice.entity.ProductVariant;
import com.nector.catalogservice.entity.Uom;
import com.nector.catalogservice.exception.DuplicateResourceException;
import com.nector.catalogservice.exception.ExternalServiceException;
import com.nector.catalogservice.exception.InactiveResourceException;
import com.nector.catalogservice.exception.ResourceNotFoundException;
import com.nector.catalogservice.mappers.ProductVariantsMapping;
import com.nector.catalogservice.repository.ProductRepository;
import com.nector.catalogservice.repository.ProductVariantRepository;
import com.nector.catalogservice.repository.UomRepository;
import com.nector.catalogservice.service.ProductVariantService;

import jakarta.validation.Valid;
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

		validateVariantBehaviour(request);

		productVariantRepository.findBySkuCodeAndCompanyId(request.getSkuCode(), request.getCompanyId())
				.ifPresent(v -> {
					throw new DuplicateResourceException("Variant SKU already exists for this company");
				});

		Product product = productRepository.findByIdAndDeletedAtIsNull(request.getProductId())
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));
		if (!product.getActive()) {
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
		if (!uom.getActive()) {
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

		ProductVariantResponse productVariantResponse = ProductVariantsMapping.mapToProductVariantResponse(saved,
				productResponse, companyResponseInternalDto, uomResponse);

		return new ApiResponse<>(true, "Product variant created successfully", HttpStatus.CREATED.name(),
				HttpStatus.CREATED.value(), productVariantResponse);
	}

	@Transactional
	@Override
	public ApiResponse<ProductVariantResponse> updateProductVariant(UUID variantId,
			@Valid ProductVariantUpdateRequest request, UUID updatedBy) {

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

			if (!uom.getActive()) {
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

		ProductVariantResponse productVariantResponse = ProductVariantsMapping.mapToProductVariantResponse(saved,
				productResponse, companyResponseInternalDto, uomResponse);

		return new ApiResponse<>(true, "Product variant updated successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), productVariantResponse);
	}

	@Transactional
	@Override
	public ApiResponse<Void> deleteProductVariant(UUID variantId, UUID deletedBy) {

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
	public ApiResponse<ProductVariantResponse> getVariantByVariantId(UUID variantId) {

		ProductVariant variant = productVariantRepository.findByIdAndDeletedAtIsNull(variantId)
				.orElseThrow(() -> new ResourceNotFoundException("Product variant not found"));

		if (!variant.getActive()) {
			throw new InactiveResourceException("Product variant is inactive");
		}

		Product product = productRepository.findByIdAndDeletedAtIsNull(variant.getProductId())
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		if (!product.getActive()) {
			throw new InactiveResourceException("Product is inactive");
		}

		var response = orgServiceClient.getCompanyBasic(variant.getCompanyId());

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

		Uom uom = uomRepository.findByIdAndDeletedAtIsNull(variant.getUomId()).filter(Uom::getActive)
				.orElseThrow(() -> new InactiveResourceException("UOM is inactive"));

		Uom baseUom = null;
		if (uom.getBaseUomId() != null) {
			baseUom = uomRepository.findByIdAndDeletedAtIsNull(uom.getBaseUomId()).filter(Uom::getActive).orElse(null);
		}

		UomResponse uomResponse = ProductVariantsMapping.mapToUomResponse(uom, baseUom);

		ProductVariantResponse productVariantResponse = ProductVariantsMapping.mapToProductVariantResponse(variant,
				productResponse, companyResponseInternalDto, uomResponse);

		return new ApiResponse<>(true, "Product variant fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), productVariantResponse);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<?> getVariantsByProductId(UUID productId) {

		Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		if (!product.getActive()) {
			throw new InactiveResourceException("Product is inactive");
		}

		List<ProductVariant> variants = productVariantRepository
				.findByProductIdAndDeletedAtIsNullAndActiveTrue(productId);

		if (variants.isEmpty()) {
			return new ApiResponse<>(true, "No variants found for the product", HttpStatus.OK.name(),
					HttpStatus.OK.value(), List.of());
		}

		Set<UUID> companyIds = variants.stream().map(ProductVariant::getCompanyId).collect(Collectors.toSet());
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
				.map(ProductVariantsMapping::mapToCompanyResponseInternalDto)
				.filter(CompanyResponseInternalDto::getActive).toList();

		Map<UUID, CompanyResponseInternalDto> companyMap = activeCompanies.stream()
				.collect(Collectors.toMap(CompanyResponseInternalDto::getCompanyId, c -> c));

		variants = variants.stream().filter(v -> companyMap.containsKey(v.getCompanyId())).toList();

		if (variants.isEmpty()) {
			return new ApiResponse<>(true, "No active variants found for the product", HttpStatus.OK.name(),
					HttpStatus.OK.value(), List.of());
		}

		Set<UUID> uomIds = variants.stream().map(ProductVariant::getUomId).collect(Collectors.toSet());
		List<Uom> activeUoms = uomRepository.findByIdInAndDeletedAtIsNull(uomIds).stream().filter(Uom::getActive)
				.toList();

		Map<UUID, Uom> uomMap = activeUoms.stream().collect(Collectors.toMap(Uom::getId, u -> u));

		variants = variants.stream().filter(v -> uomMap.containsKey(v.getUomId())).toList();

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

	@Transactional
	@Override
	public ApiResponse<List<ProductVariantResponse>> bulkUpdateProductVariants(ProductVariantBulkUpdateRequest request,
			UUID updatedBy) {

		List<ProductVariantResponse> updatedResponses = request.getVariants().stream().map(item -> {
			ProductVariant variant = productVariantRepository.findByIdAndDeletedAtIsNull(item.getVariantId())
					.orElseThrow(
							() -> new ResourceNotFoundException("Variant not found for ID: " + item.getVariantId()));

			if (!variant.getActive()) {
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

			if (variant.getMrp().compareTo(variant.getSellingPrice()) < 0) {
				throw new IllegalArgumentException(
						"Selling price cannot be greater than MRP for variant: " + variant.getSkuCode());
			}
			if (variant.getSellingPrice().compareTo(variant.getPurchasePrice()) < 0) {
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

			productVariantRepository.save(variant);

			Product product = productRepository.findByIdAndDeletedAtIsNull(variant.getProductId()).orElseThrow(
					() -> new ResourceNotFoundException("Product not found for variant: " + variant.getSkuCode()));

			CompanyResponseInternalDto company = ProductVariantsMapping.mapToCompanyResponseInternalDto(
					orgServiceClient.getCompanyBasic(variant.getCompanyId()).getBody().getData());

			Uom uom = uomRepository.findByIdAndDeletedAtIsNull(variant.getUomId()).orElse(null);
			Uom baseUom = uom != null && uom.getBaseUomId() != null
					? uomRepository.findByIdAndDeletedAtIsNull(uom.getBaseUomId()).orElse(null)
					: null;
			UomResponse uomResponse = ProductVariantsMapping.mapToUomResponse(uom, baseUom);
			ProductResponse productResponse = ProductVariantsMapping.mapToProductResponse(product);

			return ProductVariantsMapping.mapToProductVariantResponse(variant, productResponse, company, uomResponse);

		}).toList();

		return new ApiResponse<>(true, "Variants updated successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				updatedResponses);
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse<Boolean> isSkuAvailable(String skuCode, UUID companyId) {

		boolean exists = productVariantRepository.findBySkuCodeAndCompanyIdAndDeletedAtIsNull(skuCode, companyId)
				.isPresent();

		boolean available = !exists;

		return new ApiResponse<>(true, available ? "SKU is available" : "SKU already exists", HttpStatus.OK.name(),
				HttpStatus.OK.value(), available);
	}

	@Transactional
	@Override
	public ApiResponse<Void> bulkDeleteVariants(List<UUID> variantIds, UUID deletedBy) {

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
