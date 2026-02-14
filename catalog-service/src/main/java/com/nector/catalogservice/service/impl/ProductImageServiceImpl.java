package com.nector.catalogservice.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nector.catalogservice.client.OrgServiceClient;
import com.nector.catalogservice.dto.request.external.CompanyIdsRequestDto;
import com.nector.catalogservice.dto.request.internal.ProductImageCreateRequest;
import com.nector.catalogservice.dto.request.internal.ProductImageUpdateRequest;
import com.nector.catalogservice.dto.response.external.CompanyResponseExternalDto;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CompanyResponseInternalDto;
import com.nector.catalogservice.dto.response.internal.ProductImageResponse;
import com.nector.catalogservice.dto.response.internal.ProductImageResponseWithCompanyProductProductVariant;
import com.nector.catalogservice.dto.response.internal.ProductResponseWithCompanyImages;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

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
		if (request.getProductId() != null) {
			product = productRepository.findByIdAndDeletedAtIsNull(request.getProductId())
					.orElseThrow(() -> new ResourceNotFoundException("Product not found"));
			if (!product.getActive()) {
				throw new InactiveResourceException("Product is inactive");
			}
		}

		ProductVariant productVariant = null;
		if (request.getProductVariantId() != null) {
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
		if (request.getProductId() != null) {
			productImageRepository.findByProductIdAndFileHashAndDeletedAtIsNull(request.getProductId(), fileHash)
					.ifPresent(existing -> {
						throw new IllegalArgumentException("This product image already exists.");
					});
		}
		if (request.getProductVariantId() != null) {
			productImageRepository
					.findByProductVariantIdAndFileHashAndDeletedAtIsNull(request.getProductVariantId(), fileHash)
					.ifPresent(existing -> {
						throw new IllegalArgumentException("This variant image already exists.");
					});
		}

		String filePath = localFileService.uploadFile(request.getFile());

		ProductImage image = ProductImageMapping.toEntityWithCreation(request, createdBy);
		image.setImageUrl(filePath);
		image.setFileHash(fileHash);

		if (Boolean.TRUE.equals(request.getPrimary())) {
			if (request.getProductId() != null) {
				productImageRepository.findByProductIdAndPrimaryTrueAndDeletedAtIsNull(request.getProductId())
						.ifPresent(existing -> {
							existing.setPrimary(false);
							productImageRepository.save(existing);
						});
			} else if (request.getProductVariantId() != null) {
				productImageRepository
						.findByProductVariantIdAndPrimaryTrueAndDeletedAtIsNull(request.getProductVariantId())
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
			ProductImageUpdateRequest request, UUID updatedBy) {

		ProductImage image = productImageRepository.findByIdAndDeletedAtIsNull(productImageId)
				.orElseThrow(() -> new ResourceNotFoundException("Product image not found"));

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

				String newFilePath = localFileService.uploadFile(request.getFile());

				image.setImageUrl(newFilePath);
				image.setFileHash(newFileHash);
			}

		}

		if (request.getImageType() != null) {
			image.setImageType(request.getImageType());
		}

		if (request.getAltText() != null) {
			image.setAltText(request.getAltText());
		}

		if (request.getDisplayOrder() != null) {
			image.setDisplayOrder(request.getDisplayOrder());
		}

		if (request.getActive() != null) {
			image.setActive(request.getActive());
		}

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
		}

		if (Boolean.FALSE.equals(request.getPrimary()))
			image.setPrimary(false);

		image.setUpdatedBy(updatedBy);

		ProductImage savedImage = productImageRepository.save(image);

		var response = orgServiceClient.getCompanyBasic(image.getCompanyId());
		if (response == null || response.getBody() == null || response.getBody().getData() == null) {
			throw new ExternalServiceException("Invalid response from Organization Service",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		CompanyResponseExternalDto companyResponse = response.getBody().getData();

		Product product = null;
		ProductVariant variant = null;

		if (image.getProductId() != null) {
			product = productRepository.findById(image.getProductId()).orElse(null);
		}

		if (image.getProductVariantId() != null) {
			variant = productVariantRepository.findById(image.getProductVariantId()).orElse(null);
		}

		ProductImageResponseWithCompanyProductProductVariant finalResponse = ProductImageMapping
				.mapToProductImageResponseWithCompanyProductProductVariant(savedImage, companyResponse, product,
						variant);

		return new ApiResponse<>(true, "Product image updated successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), finalResponse);
	}

	@Override
	@Transactional
	public ApiResponse<Void> deleteProductImage(UUID id, UUID deletedBy) {

		ProductImage image = productImageRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product image not found"));

		image.setDeletedAt(LocalDateTime.now());
		image.setDeletedBy(deletedBy);
		image.setActive(false);

		productImageRepository.save(image);

		return new ApiResponse<>(true, "Product image deleted successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), null);
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse<ProductImageResponseWithCompanyProductProductVariant> getByProductImageId(UUID id) {

		ProductImage image = productImageRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product image not found"));

		if (!image.getActive()) {
			throw new InactiveResourceException("Product image is inactive");
		}

		var response = orgServiceClient.getCompanyBasic(image.getCompanyId());
		if (response == null || response.getBody() == null || response.getBody().getData() == null) {
			throw new ExternalServiceException("Invalid response from Organization Service",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		CompanyResponseExternalDto companyResponse = response.getBody().getData();

		if (!companyResponse.getActive()) {
			throw new InactiveResourceException("Associated company is inactive for this product image.");
		}

		Product product = null;
		ProductVariant variant = null;

		if (image.getProductId() != null) {
			product = productRepository.findByIdAndDeletedAtIsNull(image.getProductId()).orElse(null);
			if (product != null && !product.getActive()) {
				throw new InactiveResourceException("Associated product is inactive for this product image.");
			}
		}

		if (image.getProductVariantId() != null) {
			variant = productVariantRepository.findById(image.getProductVariantId()).orElse(null);
			if (variant != null && !variant.getActive()) {
				throw new InactiveResourceException("Associated product variant is inactive for this product image.");
			}
		}

		var finalResponse = ProductImageMapping.mapToProductImageResponseWithCompanyProductProductVariant(image,
				companyResponse, product, variant);

		return new ApiResponse<>(true, "Product image fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), finalResponse);
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse<List<ProductImageResponseWithCompanyProductProductVariant>> getByProductOrVariantId(
			UUID productId, UUID productVariantId) {

		if ((productId == null && productVariantId == null) || (productId != null && productVariantId != null)) {
			throw new IllegalArgumentException("Exactly one of productId or productVariantId must be provided");
		}

		if (productId != null) {

			Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
					.orElseThrow(() -> new ResourceNotFoundException("Product is not found"));
			if (!product.getActive()) {
				throw new InactiveResourceException("Product is inactive");
			}
			
			List<ProductImage> images = productImageRepository
					.findByProductIdAndDeletedAtIsNullAndActiveTrue(productId);
			Set<UUID> variantIds = images.stream().map(ProductImage::getProductVariantId).filter(Objects::nonNull)
					.collect(Collectors.toSet());

			var response = orgServiceClient.getCompanyBasic(product.getCompanyId());
			if (response == null || response.getBody() == null || response.getBody().getData() == null) {
				throw new ExternalServiceException("Invalid response from Organization Service",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
			CompanyResponseExternalDto companyResponse = response.getBody().getData();
			
			CompanyResponseInternalDto company = ProductImageMapping.mapToCompanyResponseInternalDto(companyResponse);

			Map<UUID, ProductVariant> variants = productVariantRepository
					.findByIdInAndDeletedAtIsNull(new ArrayList<>(variantIds)).stream()
					.filter(ProductVariant::getActive).collect(Collectors.toMap(ProductVariant::getId, v -> v));
			
			List<ProductImageResponse> productImages = images.stream().map(i -> {
				 return ProductImageMapping.mapToProductImageResponse(i);
			}).toList();
			
			ProductResponseWithCompanyImages finalResponse = ProductImageMapping.mapToProductResponseWithCompanyImages(product, company, productImages);

			return new ApiResponse<>(true, "Product images fetched successfully", HttpStatus.OK.name(),
					HttpStatus.OK.value(), finalResponse);
			
		} else {

			ProductVariant productVariant = productVariantRepository.findByIdAndDeletedAtIsNull(productVariantId)
					.orElseThrow(() -> new ResourceNotFoundException("Product variant is not found"));
			if (!productVariant.getActive()) {
				throw new InactiveResourceException("Product variant is inactive");
			}
			
			List<ProductImage> images = productImageRepository
					.findByProductVariantIdAndDeletedAtIsNullAndActiveTrue(productVariantId);
			Set<UUID> companyIds = images.stream().map(ProductImage::getCompanyId).collect(Collectors.toSet());
			Set<UUID> productIds = images.stream().map(ProductImage::getProductId).filter(Objects::nonNull)
					.collect(Collectors.toSet());

			CompanyIdsRequestDto companyIdsRequestDto = new CompanyIdsRequestDto();
			companyIdsRequestDto.setCompanyIds(new ArrayList<>(companyIds));
			ResponseEntity<ApiResponse<List<CompanyResponseExternalDto>>> companyResponseRaw = orgServiceClient
					.getCompaniesDetailsByCompanyIds(companyIdsRequestDto);

			if (companyResponseRaw == null || companyResponseRaw.getBody() == null
					|| companyResponseRaw.getBody().getData() == null) {
				throw new ExternalServiceException("Invalid response from Organization Service",
						HttpStatus.INTERNAL_SERVER_ERROR);
			}

			Map<UUID, CompanyResponseInternalDto> companyMap = companyResponseRaw.getBody().getData().stream()
					.filter(c -> c != null).map(ProductVariantsMapping::mapToCompanyResponseInternalDto)
					.filter(CompanyResponseInternalDto::getActive)
					.collect(Collectors.toMap(CompanyResponseInternalDto::getCompanyId, c -> c));

			Map<UUID, Product> products = productRepository.findByIdInAndDeletedAtIsNull(new ArrayList<>(productIds))
					.stream().filter(Product::getActive).collect(Collectors.toMap(Product::getId, p -> p));

		}

	}

}
