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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nector.catalogservice.client.OrgServiceClient;
import com.nector.catalogservice.config.FeignConfig;
import com.nector.catalogservice.controller.SubCategoryController;
import com.nector.catalogservice.dto.request.external.CompanyIdsRequestDto;
import com.nector.catalogservice.dto.request.internal.BulkProductImageUpdateRequest;
import com.nector.catalogservice.dto.request.internal.ProductImageCreateRequest;
import com.nector.catalogservice.dto.request.internal.ProductImageUpdateRequest;
import com.nector.catalogservice.dto.response.external.CompanyResponseExternalDto;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CompanyResponseInternalDto;
import com.nector.catalogservice.dto.response.internal.ProductImageResponse;
import com.nector.catalogservice.dto.response.internal.ProductImageResponseWithCompanyProductProductVariant;
import com.nector.catalogservice.dto.response.internal.ProductResponse;
import com.nector.catalogservice.dto.response.internal.ProductResponseWithCompanyImagesVariants;
import com.nector.catalogservice.dto.response.internal.ProductResponseWithCompanyPrimaryImageVariants;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponse;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponseWithCompanyImagesProduct;
import com.nector.catalogservice.dto.response.internal.ProductVariantResponseWithCompanyPrimaryImageProduct;
import com.nector.catalogservice.entity.Product;
import com.nector.catalogservice.entity.ProductImage;
import com.nector.catalogservice.entity.ProductVariant;
import com.nector.catalogservice.exception.ExternalServiceException;
import com.nector.catalogservice.exception.InactiveResourceException;
import com.nector.catalogservice.exception.ResourceNotFoundException;
import com.nector.catalogservice.mapper.ProductImageMapping;
import com.nector.catalogservice.mapper.ProductVariantsMapping;
import com.nector.catalogservice.repository.ProductImageRepository;
import com.nector.catalogservice.repository.ProductRepository;
import com.nector.catalogservice.repository.ProductVariantRepository;
import com.nector.catalogservice.service.LocalFileService;
import com.nector.catalogservice.service.ProductImageService;
import com.nector.catalogservice.specification.ProductImageSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

	private final SubCategoryController subCategoryController;

	private final FeignConfig feignConfig;

	private final ProductImageRepository productImageRepository;
	private final ProductRepository productRepository;
	private final ProductVariantRepository productVariantRepository;
	private final OrgServiceClient orgServiceClient;
	private final LocalFileService localFileService;

	@Override
	@Transactional
	public ApiResponse<ProductImageResponseWithCompanyProductProductVariant> saveAndUploadProductImage(
			ProductImageCreateRequest request, UUID createdBy) {

		if ((request.getProductId() == null && request.getProductVariantId() == null)
				|| (request.getProductId() != null && request.getProductVariantId() != null)) {
			throw new IllegalArgumentException("Exactly one of productId or productVariantId must be provided");
		}

		Product product = null;
		ProductVariant productVariant = null;

		if (request.getProductId() != null) {
			product = productRepository.findByIdAndDeletedAtIsNull(request.getProductId())
					.orElseThrow(() -> new ResourceNotFoundException("Product not found"));
			if (!product.getActive()) {
				throw new InactiveResourceException("Product is inactive");
			}
		} else {
			productVariant = productVariantRepository.findByIdAndDeletedAtIsNull(request.getProductVariantId())
					.orElseThrow(() -> new ResourceNotFoundException("Product variant not found"));
			if (!productVariant.getActive()) {
				throw new InactiveResourceException("Product variant is inactive");
			}
		}

		if (product != null && !product.getCompanyId().equals(request.getCompanyId())) {
			throw new IllegalArgumentException("Product does not belong to the given company");
		}
		if (productVariant != null && !productVariant.getCompanyId().equals(request.getCompanyId())) {
			throw new IllegalArgumentException("Product variant does not belong to the given company");
		}

		var response = orgServiceClient.getCompanyBasic(request.getCompanyId());
		if (response == null || response.getBody() == null || response.getBody().getData() == null) {
			throw new ExternalServiceException("Invalid response from Organization Service",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		CompanyResponseExternalDto companyResponse = response.getBody().getData();

		String fileHash = localFileService.generateFileHash(request.getFile());
		if (product != null) {
			productImageRepository.findByProductIdAndFileHashAndDeletedAtIsNull(product.getId(), fileHash)
					.ifPresent(existing -> {
						throw new IllegalArgumentException("This product image already exists.");
					});
		} else if (productVariant != null) {
			productImageRepository.findByProductVariantIdAndFileHashAndDeletedAtIsNull(productVariant.getId(), fileHash)
					.ifPresent(existing -> {
						throw new IllegalArgumentException("This variant image already exists.");
					});
		}

		String filePath = localFileService.uploadFile(request.getFile());

		ProductImage image = ProductImageMapping.toEntityWithCreation(request, createdBy);
		image.setImageUrl(filePath);
		image.setFileHash(fileHash);

		Boolean isPrimary = request.getPrimary();
		boolean primaryExists = false;

		if (product != null) {
			primaryExists = productImageRepository.findByProductIdAndPrimaryTrueAndDeletedAtIsNull(product.getId())
					.isPresent();
		} else if (productVariant != null) {
			primaryExists = productImageRepository
					.findByProductVariantIdAndPrimaryTrueAndDeletedAtIsNull(productVariant.getId()).isPresent();
		}

		if (isPrimary == null) {
			isPrimary = !primaryExists;
		}

		image.setPrimary(isPrimary);

		if (Boolean.TRUE.equals(isPrimary)) {
			if (product != null) {
				productImageRepository.findByProductIdAndPrimaryTrueAndDeletedAtIsNull(product.getId())
						.ifPresent(existing -> {
							existing.setPrimary(false);
							productImageRepository.save(existing);
						});
			} else if (productVariant != null) {
				productImageRepository.findByProductVariantIdAndPrimaryTrueAndDeletedAtIsNull(productVariant.getId())
						.ifPresent(existing -> {
							existing.setPrimary(false);
							productImageRepository.save(existing);
						});
			}
		}

		ProductImage savedImage = productImageRepository.save(image);

		ProductImageResponseWithCompanyProductProductVariant finalResponse = ProductImageMapping
				.mapToProductImageResponseWithCompanyProductProductVariant(savedImage, companyResponse, product,
						productVariant);

		return new ApiResponse<>(true, "Product image created successfully", HttpStatus.CREATED.name(),
				HttpStatus.CREATED.value(), finalResponse);
	}

	@Override
	@Transactional
	public ApiResponse<ProductImageResponseWithCompanyProductProductVariant> updateProductImage(UUID productImageId,
			UUID companyId, ProductImageUpdateRequest request, UUID updatedBy) {

		ProductImage image = productImageRepository.findByIdAndDeletedAtIsNull(productImageId)
				.orElseThrow(() -> new ResourceNotFoundException("Product image not found"));

		if (!companyId.equals(image.getCompanyId())) {
			throw new IllegalArgumentException("This image does not belong to the given company");
		}

		if (request.getFile() != null && !request.getFile().isEmpty()) {
			String newFileHash = localFileService.generateFileHash(request.getFile());
			if (!newFileHash.equals(image.getFileHash())) {
				Optional<ProductImage> duplicate;
				if (image.getProductId() != null) {
					duplicate = productImageRepository
							.findByProductIdAndFileHashAndDeletedAtIsNull(image.getProductId(), newFileHash);
				} else {
					duplicate = productImageRepository.findByProductVariantIdAndFileHashAndDeletedAtIsNull(
							image.getProductVariantId(), newFileHash);
				}
				if (duplicate.isPresent() && !duplicate.get().getId().equals(productImageId)) {
					throw new IllegalArgumentException("Same image already exists.");
				}
				localFileService.deleteFile(image.getImageUrl());
				image.setImageUrl(localFileService.uploadFile(request.getFile()));
				image.setFileHash(newFileHash);
			}
		}

		if (request.getImageType() != null)
			image.setImageType(request.getImageType());
		if (request.getAltText() != null)
			image.setAltText(request.getAltText());
		if (request.getDisplayOrder() != null)
			image.setDisplayOrder(request.getDisplayOrder());
		if (request.getActive() != null)
			image.setActive(request.getActive());

		if (Boolean.TRUE.equals(request.getPrimary())) {
			if (image.getProductId() != null) {
				productImageRepository.findByProductIdAndPrimaryTrueAndDeletedAtIsNull(image.getProductId())
						.ifPresent(existing -> {
							if (!existing.getId().equals(productImageId)) {
								existing.setPrimary(false);
								productImageRepository.save(existing);
							}
						});
			}
			if (image.getProductVariantId() != null) {
				productImageRepository
						.findByProductVariantIdAndPrimaryTrueAndDeletedAtIsNull(image.getProductVariantId())
						.ifPresent(existing -> {
							if (!existing.getId().equals(productImageId)) {
								existing.setPrimary(false);
								productImageRepository.save(existing);
							}
						});
			}
			image.setPrimary(true);
		} else if (Boolean.FALSE.equals(request.getPrimary())) {
			image.setPrimary(false);
		}

		image.setUpdatedBy(updatedBy);
		ProductImage savedImage = productImageRepository.save(image);

		var response = orgServiceClient.getCompanyBasic(companyId);
		CompanyResponseExternalDto companyResponse = response.getBody().getData();
		Product product = image.getProductId() != null ? productRepository.findById(image.getProductId()).orElse(null)
				: null;
		ProductVariant variant = image.getProductVariantId() != null
				? productVariantRepository.findById(image.getProductVariantId()).orElse(null)
				: null;

		ProductImageResponseWithCompanyProductProductVariant finalResponse = ProductImageMapping
				.mapToProductImageResponseWithCompanyProductProductVariant(savedImage, companyResponse, product,
						variant);

		return new ApiResponse<>(true, "Product image updated successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), finalResponse);
	}

	@Override
	@Transactional
	public ApiResponse<Void> deleteProductImage(UUID productImageId, UUID companyId, UUID deletedBy) {

		ProductImage image = productImageRepository.findByIdAndDeletedAtIsNull(productImageId)
				.orElseThrow(() -> new ResourceNotFoundException("Product image not found"));

		if (!companyId.equals(image.getCompanyId())) {
			throw new IllegalArgumentException("This image does not belong to the given company");
		}

		image.setDeletedAt(LocalDateTime.now());
		image.setDeletedBy(deletedBy);
		image.setActive(false);
		productImageRepository.save(image);

		return new ApiResponse<>(true, "Product image deleted successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), null);
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse<ProductImageResponseWithCompanyProductProductVariant> getByProductImageId(UUID productImageId,
			UUID companyId) {

		ProductImage image = productImageRepository.findByIdAndDeletedAtIsNull(productImageId)
				.orElseThrow(() -> new ResourceNotFoundException("Product image not found"));

		if (!companyId.equals(image.getCompanyId())) {
			throw new IllegalArgumentException("This image does not belong to the given company");
		}

		if (!image.getActive()) {
			throw new InactiveResourceException("Product image is inactive");
		}

		var response = orgServiceClient.getCompanyBasic(companyId);
		CompanyResponseExternalDto companyResponse = response.getBody().getData();

		Product product = image.getProductId() != null
				? productRepository.findByIdAndDeletedAtIsNull(image.getProductId()).orElse(null)
				: null;
		ProductVariant variant = image.getProductVariantId() != null
				? productVariantRepository.findById(image.getProductVariantId()).orElse(null)
				: null;

		ProductImageResponseWithCompanyProductProductVariant finalResponse = ProductImageMapping
				.mapToProductImageResponseWithCompanyProductProductVariant(image, companyResponse, product, variant);

		return new ApiResponse<>(true, "Product image fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), finalResponse);
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse<?> getByProductOrVariantId(UUID productId, UUID productVariantId) {

		if ((productId == null && productVariantId == null) || (productId != null && productVariantId != null)) {
			throw new IllegalArgumentException("Exactly one of productId or productVariantId must be provided");
		}

		if (productId != null) {

			Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
					.orElseThrow(() -> new ResourceNotFoundException("Product is not found"));

			if (!Boolean.TRUE.equals(product.getActive())) {
				throw new InactiveResourceException("Product is inactive");
			}

			List<ProductImage> images = Optional
					.ofNullable(productImageRepository.findByProductIdAndDeletedAtIsNullAndActiveTrue(productId))
					.orElse(Collections.emptyList());

			List<ProductVariant> variants = Optional
					.ofNullable(productVariantRepository.findByProductIdAndDeletedAtIsNullAndActiveTrue(productId))
					.orElse(Collections.emptyList()).stream().filter(Objects::nonNull).toList();

			if (product.getCompanyId() == null) {
				throw new IllegalStateException("Company ID is null for product");
			}

			var response = orgServiceClient.getCompanyBasic(product.getCompanyId());

			if (response == null || response.getBody() == null || response.getBody().getData() == null) {
				throw new ExternalServiceException("Invalid response from Organization Service",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}

			CompanyResponseExternalDto companyResponse = response.getBody().getData();

			List<ProductImageResponse> productImages = images.stream()
					.map(ProductImageMapping::mapToProductImageResponse).toList();

			List<ProductVariantResponse> variantResponses = variants.stream()
					.map(ProductVariantsMapping::mapToProductVariantResponse).toList();

			CompanyResponseInternalDto company = ProductImageMapping.mapToCompanyResponseInternalDto(companyResponse);

			ProductResponseWithCompanyImagesVariants finalResponse = ProductImageMapping
					.mapToProductResponseWithCompanyImages(product, variantResponses, company, productImages);

			return new ApiResponse<>(true, "Product images fetched successfully", HttpStatus.OK.name(),
					HttpStatus.OK.value(), finalResponse);
		}

		ProductVariant productVariant = productVariantRepository.findByIdAndDeletedAtIsNull(productVariantId)
				.orElseThrow(() -> new ResourceNotFoundException("Product variant is not found"));

		if (!Boolean.TRUE.equals(productVariant.getActive())) {
			throw new InactiveResourceException("Product variant is inactive");
		}

		List<ProductImage> variantImages = Optional
				.ofNullable(
						productImageRepository.findByProductVariantIdAndDeletedAtIsNullAndActiveTrue(productVariantId))
				.orElse(Collections.emptyList());

		Product product = productRepository.findByIdAndDeletedAtIsNull(productVariant.getProductId())
				.orElseThrow(() -> new ResourceNotFoundException("Associated product not found"));

		if (!Boolean.TRUE.equals(product.getActive())) {
			throw new InactiveResourceException("Associated product is inactive with variants");
		}

		if (productVariant.getCompanyId() == null) {
			throw new IllegalStateException("Company ID is null for product variant");
		}

		var response = orgServiceClient.getCompanyBasic(productVariant.getCompanyId());

		if (response == null || response.getBody() == null || response.getBody().getData() == null) {
			throw new ExternalServiceException("Invalid response from Organization Service",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

		CompanyResponseExternalDto companyResponse = response.getBody().getData();

		ProductResponse productResponse = ProductVariantsMapping.mapToProductResponse(product);

		CompanyResponseInternalDto company = ProductImageMapping.mapToCompanyResponseInternalDto(companyResponse);

		List<ProductImageResponse> variantImagesList = variantImages.stream()
				.map(ProductImageMapping::mapToProductImageResponse).toList();

		ProductVariantResponseWithCompanyImagesProduct finalResponse = ProductImageMapping
				.mapToProductVariantResponseWithCompanyImages(productVariant, productResponse, company,
						variantImagesList);

		return new ApiResponse<>(true, "Product images fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), finalResponse);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<ProductResponseWithCompanyImagesVariants> getByCompanyIdAndProductId(UUID companyId,
			UUID productId) {

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
		
		List<ProductImage> images = Optional
				.ofNullable(productImageRepository.findByProductIdAndDeletedAtIsNullAndActiveTrue(productId))
				.orElse(Collections.emptyList());

		CompanyResponseInternalDto company = ProductImageMapping.mapToCompanyResponseInternalDto(companyResponse);

		List<ProductImageResponse> productImages = images.stream().map(ProductImageMapping::mapToProductImageResponse)
				.toList();

		List<ProductVariantResponse> variantResponses = variants.stream()
				.map(ProductVariantsMapping::mapToProductVariantResponse).toList();

		ProductResponseWithCompanyImagesVariants finalResponse = ProductImageMapping
				.mapToProductResponseWithCompanyImages(product, variantResponses, company, productImages);

		return new ApiResponse<>(true, "Product images fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), finalResponse);

	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<ProductVariantResponseWithCompanyImagesProduct> getByCompanyIdAndVariantId(UUID companyId,
			UUID variantId) {

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
				.orElseThrow(() -> new ResourceNotFoundException("Product variant not found"));

		if (!companyId.equals(variant.getCompanyId())) {
			throw new IllegalArgumentException("Variant does not belong to given company");
		}

		if (!Boolean.TRUE.equals(variant.getActive())) {
			throw new InactiveResourceException("Product variant is inactive");
		}

		Product product = productRepository.findByIdAndDeletedAtIsNull(variant.getProductId())
				.orElseThrow(() -> new ResourceNotFoundException("Associated product not found"));

		if (!Boolean.TRUE.equals(product.getActive())) {
			throw new InactiveResourceException("Associated product is inactive");
		}

		List<ProductImage> variantImages = Optional
				.ofNullable(productImageRepository.findByProductVariantIdAndDeletedAtIsNullAndActiveTrue(variantId))
				.orElse(Collections.emptyList());

		List<ProductImageResponse> variantImagesList = variantImages.stream()
				.map(ProductImageMapping::mapToProductImageResponse).toList();

		CompanyResponseInternalDto company = ProductImageMapping.mapToCompanyResponseInternalDto(companyResponse);

		ProductResponse productResponse = ProductVariantsMapping.mapToProductResponse(product);

		ProductVariantResponseWithCompanyImagesProduct finalResponse = ProductImageMapping
				.mapToProductVariantResponseWithCompanyImages(variant, productResponse, company, variantImagesList);

		return new ApiResponse<>(true, "Product variant images fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), finalResponse);
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<?> getPrimaryImageByCompanyAndProductOrVariant(UUID companyId, UUID productId,
			UUID productVariantId) {

		if (companyId == null || (productId == null && productVariantId == null)
				|| (productId != null && productVariantId != null)) {
			throw new IllegalArgumentException(
					"CompanyId and exactly one of productId or productVariantId must be provided");
		}

		var response = orgServiceClient.getCompanyBasic(companyId);
		if (response == null || response.getBody() == null || response.getBody().getData() == null) {
			throw new ExternalServiceException("Invalid response from Organization Service",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		CompanyResponseExternalDto companyResponse = response.getBody().getData();
		if (!Boolean.TRUE.equals(companyResponse.getActive())) {
			throw new InactiveResourceException("Company is inactive");
		}

		CompanyResponseInternalDto company = ProductImageMapping.mapToCompanyResponseInternalDto(companyResponse);

		if (productId != null) {
			Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
					.orElseThrow(() -> new ResourceNotFoundException("Product not found"));
			if (!Boolean.TRUE.equals(product.getActive()) || !companyId.equals(product.getCompanyId())) {
				throw new IllegalArgumentException("Product is inactive or does not belong to the company");
			}

			ProductImage primaryImage = productImageRepository
					.findByProductIdAndPrimaryTrueAndDeletedAtIsNull(productId)
					.orElseThrow(() -> new ResourceNotFoundException("Primary image not found for product"));

			List<ProductVariant> variants = Optional
					.ofNullable(productVariantRepository.findByProductIdAndDeletedAtIsNullAndActiveTrue(productId))
					.orElse(Collections.emptyList()).stream().filter(Objects::nonNull).toList();

			List<ProductVariantResponse> variantResponses = variants.stream()
					.map(ProductVariantsMapping::mapToProductVariantResponse).toList();

			ProductResponseWithCompanyPrimaryImageVariants finalResponse = ProductImageMapping
					.mapToProductResponseWithCompanyPrimaryImageVariants(product, variantResponses, company,
							ProductImageMapping.mapToProductImageResponse(primaryImage));

			return new ApiResponse<>(true, "Primary product image fetched successfully", HttpStatus.OK.name(),
					HttpStatus.OK.value(), finalResponse);

		} else {
			ProductVariant variant = productVariantRepository.findByIdAndDeletedAtIsNull(productVariantId)
					.orElseThrow(() -> new ResourceNotFoundException("Product variant not found"));
			if (!Boolean.TRUE.equals(variant.getActive()) || !companyId.equals(variant.getCompanyId())) {
				throw new IllegalArgumentException("Variant is inactive or does not belong to the company");
			}

			Product product = productRepository.findByIdAndDeletedAtIsNull(variant.getProductId())
					.orElseThrow(() -> new ResourceNotFoundException("Associated product not found"));
			if (!Boolean.TRUE.equals(product.getActive())) {
				throw new InactiveResourceException("Associated product is inactive");
			}

			ProductImage primaryImage = productImageRepository
					.findByProductVariantIdAndPrimaryTrueAndDeletedAtIsNull(productVariantId)
					.orElseThrow(() -> new ResourceNotFoundException("Primary image not found for variant"));

			ProductResponse productResponse = ProductVariantsMapping.mapToProductResponse(product);
			ProductVariantResponseWithCompanyPrimaryImageProduct finalResponse = ProductImageMapping
					.mapToProductVariantResponseWithCompanyPrimaryImageProduct(variant, productResponse, company,
							ProductImageMapping.mapToProductImageResponse(primaryImage));

			return new ApiResponse<>(true, "Primary variant image fetched successfully", HttpStatus.OK.name(),
					HttpStatus.OK.value(), finalResponse);
		}
	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<Page<ProductImageResponseWithCompanyProductProductVariant>> searchProductImages(UUID companyId,
			UUID productId, UUID variantId, Boolean active, Boolean primary, String imageType, String altText,
			boolean includeInactiveCompanies, boolean includeInactiveProducts, boolean includeInactiveVariants,
			int page, int size, String sortBy, String sortDir) {

		String sortField = sortBy != null ? sortBy : "createdAt";
		Sort sort = sortDir != null && sortDir.equalsIgnoreCase("desc") ? Sort.by(sortField).descending()
				: Sort.by(sortField).ascending();
		Pageable pageable = PageRequest.of(page, size, sort);

		Specification<ProductImage> spec = ProductImageSpecification.filterProductImages(companyId, productId,
				variantId, active, primary, imageType, altText);

		Page<ProductImage> imagePage = productImageRepository.findAll(spec, pageable);
		List<ProductImage> images = imagePage.getContent();

		if (images.isEmpty()) {
			return new ApiResponse<>(true, "No product images found", HttpStatus.OK.name(), HttpStatus.OK.value(),
					Page.empty());
		}

		Set<UUID> companyIds = images.stream().map(ProductImage::getCompanyId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		List<CompanyResponseExternalDto> companiesExternal = Optional
				.ofNullable(orgServiceClient
						.getCompaniesDetailsByCompanyIds(new CompanyIdsRequestDto(new ArrayList<>(companyIds))))
				.map(r -> r.getBody()).map(ApiResponse::getData).orElse(Collections.emptyList());

		Map<UUID, CompanyResponseExternalDto> companyMap = companiesExternal.stream().filter(Objects::nonNull)
				.filter(c -> includeInactiveCompanies || Boolean.TRUE.equals(c.getActive()))
				.collect(Collectors.toMap(CompanyResponseExternalDto::getCompanyId, c -> c));

		Set<UUID> productIds = images.stream().map(ProductImage::getProductId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Map<UUID, Product> productMap = Optional
				.ofNullable(productRepository.findByIdInAndDeletedAtIsNull(new ArrayList<>(productIds)))
				.orElse(Collections.emptyList()).stream()
				.filter(p -> includeInactiveProducts || Boolean.TRUE.equals(p.getActive()))
				.collect(Collectors.toMap(Product::getId, p -> p));

		Set<UUID> variantIds = images.stream().map(ProductImage::getProductVariantId).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		Map<UUID, ProductVariant> variantMap = Optional
				.ofNullable(productVariantRepository.findByIdInAndDeletedAtIsNull(new ArrayList<>(variantIds)))
				.orElse(Collections.emptyList()).stream()
				.filter(v -> includeInactiveVariants || Boolean.TRUE.equals(v.getActive()))
				.collect(Collectors.toMap(ProductVariant::getId, v -> v));

		images = images.stream().filter(img -> img.getCompanyId() != null && companyMap.containsKey(img.getCompanyId()))
				.filter(img -> img.getProductId() == null || productMap.containsKey(img.getProductId()))
				.filter(img -> img.getProductVariantId() == null || variantMap.containsKey(img.getProductVariantId()))
				.toList();

		List<ProductImageResponseWithCompanyProductProductVariant> imageResponses = images.stream().map(img -> {
			CompanyResponseExternalDto company = companyMap.get(img.getCompanyId());

			Product product = img.getProductId() != null ? productMap.get(img.getProductId()) : null;
			ProductVariant variant = img.getProductVariantId() != null ? variantMap.get(img.getProductVariantId())
					: null;

			return ProductImageMapping.mapToProductImageResponseWithCompanyProductProductVariant(img, company, product,
					variant);
		}).filter(Objects::nonNull).toList();

		Page<ProductImageResponseWithCompanyProductProductVariant> responsePage = new PageImpl<>(imageResponses,
				pageable, imagePage.getTotalElements());

		return new ApiResponse<>(true, "Product images fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), responsePage);
	}

	@Override
	@Transactional
	public ApiResponse<List<ProductImageResponseWithCompanyProductProductVariant>> bulkUploadProductImages(
			List<ProductImageCreateRequest> requests, UUID createdBy) {

		if (requests == null || requests.isEmpty()) {
			throw new IllegalArgumentException("Request list cannot be empty");
		}

		List<ProductImageResponseWithCompanyProductProductVariant> responses = new ArrayList<>();

		UUID productId = requests.get(0).getProductId();
		UUID variantId = requests.get(0).getProductVariantId();

		boolean primaryExists = false;

		if (productId != null) {
			primaryExists = productImageRepository.findByProductIdAndPrimaryTrueAndDeletedAtIsNull(productId)
					.isPresent();
		} else if (variantId != null) {
			primaryExists = productImageRepository.findByProductVariantIdAndPrimaryTrueAndDeletedAtIsNull(variantId)
					.isPresent();
		}

		boolean firstImageAssigned = false;

		for (ProductImageCreateRequest req : requests) {

			if (!primaryExists && !firstImageAssigned && req.getPrimary() == null) {
				req.setPrimary(true);
				firstImageAssigned = true;
			} else if (req.getPrimary() == null) {
				req.setPrimary(false);
			}

			responses.add(saveAndUploadProductImage(req, createdBy).getData());
		}

		return new ApiResponse<>(true, "Bulk upload completed successfully", HttpStatus.CREATED.name(),
				HttpStatus.CREATED.value(), responses);
	}

	@Override
	@Transactional
	public ApiResponse<List<ProductImageResponseWithCompanyProductProductVariant>> bulkUpdateProductImages(
			UUID companyId, List<BulkProductImageUpdateRequest> requests, UUID updatedBy) {

		if (requests == null || requests.isEmpty()) {
			throw new IllegalArgumentException("Request list cannot be empty");
		}

		List<ProductImageResponseWithCompanyProductProductVariant> responses = new ArrayList<>();

		var companyResponse = orgServiceClient.getCompanyBasic(companyId).getBody().getData();

		for (BulkProductImageUpdateRequest req : requests) {

			ProductImage image = productImageRepository.findByIdAndDeletedAtIsNull(req.getProductImageId())
					.orElseThrow(() -> new ResourceNotFoundException("Product image not found"));

			if (!companyId.equals(image.getCompanyId())) {
				throw new IllegalArgumentException("Product image does not belong to the given company");
			}

			if (req.getAltText() != null)
				image.setAltText(req.getAltText());
			if (req.getDisplayOrder() != null)
				image.setDisplayOrder(req.getDisplayOrder());
			if (req.getActive() != null)
				image.setActive(req.getActive());
			if (req.getImageType() != null)
				image.setImageType(req.getImageType());
			;

			if (Boolean.TRUE.equals(req.getPrimary())) {
				if (image.getProductId() != null) {
					productImageRepository.findByProductIdAndPrimaryTrueAndDeletedAtIsNull(image.getProductId())
							.ifPresent(existing -> {
								existing.setPrimary(false);
								productImageRepository.save(existing);
							});
				} else if (image.getProductVariantId() != null) {
					productImageRepository
							.findByProductVariantIdAndPrimaryTrueAndDeletedAtIsNull(image.getProductVariantId())
							.ifPresent(existing -> {
								existing.setPrimary(false);
								productImageRepository.save(existing);
							});
				}
				image.setPrimary(true);
			}

			ProductImage updatedImage = productImageRepository.save(image);

			Product product = image.getProductId() != null
					? productRepository.findById(image.getProductId()).orElse(null)
					: null;
			ProductVariant variant = image.getProductVariantId() != null
					? productVariantRepository.findById(image.getProductVariantId()).orElse(null)
					: null;

			responses.add(ProductImageMapping.mapToProductImageResponseWithCompanyProductProductVariant(updatedImage,
					companyResponse, product, variant));
		}

		return new ApiResponse<>(true, "Bulk update completed successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), responses);
	}

}
