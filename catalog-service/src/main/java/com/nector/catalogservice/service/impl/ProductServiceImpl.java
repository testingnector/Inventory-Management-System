package com.nector.catalogservice.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nector.catalogservice.client.OrgServiceClient;
import com.nector.catalogservice.dto.request.internal.BulkProductStatusRequest;
import com.nector.catalogservice.dto.request.internal.ProductCreateRequest;
import com.nector.catalogservice.dto.request.internal.ProductUpdateRequest;
import com.nector.catalogservice.dto.response.external.CompanyResponseExternalDto;
import com.nector.catalogservice.dto.response.internal.ApiResponse;
import com.nector.catalogservice.dto.response.internal.CategoryResponse;
import com.nector.catalogservice.dto.response.internal.CompanyProductsResponse;
import com.nector.catalogservice.dto.response.internal.CompanyResponseInternalDto;
import com.nector.catalogservice.dto.response.internal.ProductAggregateResponse;
import com.nector.catalogservice.dto.response.internal.ProductCategorySubCategoryResponse;
import com.nector.catalogservice.dto.response.internal.SubCategoryResponse;
import com.nector.catalogservice.entity.Category;
import com.nector.catalogservice.entity.Product;
import com.nector.catalogservice.entity.SubCategory;
import com.nector.catalogservice.exception.ActiveResourceException;
import com.nector.catalogservice.exception.DuplicateResourceException;
import com.nector.catalogservice.exception.InactiveResourceException;
import com.nector.catalogservice.exception.OrgServiceException;
import com.nector.catalogservice.exception.ResourceNotFoundException;
import com.nector.catalogservice.repository.CategoryRepository;
import com.nector.catalogservice.repository.ProductRepository;
import com.nector.catalogservice.repository.SubCategoryRepository;
import com.nector.catalogservice.service.ProductService;

import feign.FeignException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;
	private final OrgServiceClient orgServiceClient;
	private final CategoryRepository categoryRepository;
	private final SubCategoryRepository subCategoryRepository;

	@Transactional
	@Override
	public ApiResponse<ProductAggregateResponse> createProduct(ProductCreateRequest request, UUID createdBy) {

		Optional<Product> existingOpt = productRepository.findByProductCode(request.getProductCode());

		if (existingOpt.isPresent()) {
			Product existing = existingOpt.get();
			if (!existing.getActive() && existing.getDeletedAt() == null) {
				throw new InactiveResourceException("Product already exists but is inactive");
			} else {
				throw new DuplicateResourceException("Product code already exists!");
			}
		}

		Category category = categoryRepository.findByIdAndDeletedAtIsNull(request.getCategoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Target Category not found or deleted"));
		if (!category.getActive()) {
			throw new InactiveResourceException("Target Category is inactive");
		}

		SubCategory subCategory = null;
		if (request.getSubCategoryId() != null) {
			subCategory = subCategoryRepository.findByIdAndDeletedAtIsNull(request.getSubCategoryId())
					.orElseThrow(() -> new ResourceNotFoundException("Target Sub-Category not found or deleted"));
			if (!subCategory.getActive()) {
				throw new InactiveResourceException("Target Sub-Category is inactive");
			}
		}

		CompanyResponseExternalDto companyResponse;
		try {
			companyResponse = orgServiceClient.getCompanyBasic(request.getCompanyId()).getBody().getData();
		} catch (FeignException e) {
			HttpStatus status = HttpStatus.resolve(e.status());
			String message = (status == HttpStatus.NOT_FOUND) ? "Company not found!"
					: "Error while communicating with Organization Service";
			throw new OrgServiceException(message, status, e);
		}

		validateProductBehaviour(request);

		Product product = new Product();

		product.setProductCode(request.getProductCode());
		product.setProductName(request.getProductName());
		product.setCompanyId(request.getCompanyId());
		product.setCategoryId(request.getCategoryId());
		product.setSubCategoryId(request.getSubCategoryId());
		product.setDescription(request.getDescription());
		product.setBrandName(request.getBrandName());
		product.setModelNumber(request.getModelNumber());

		if (request.getVariantBased() != null)
			product.setVariantBased(request.getVariantBased());
		if (request.getSerialized() != null)
			product.setSerialized(request.getSerialized());
		if (request.getBatchTracked() != null)
			product.setBatchTracked(request.getBatchTracked());
		if (request.getExpiryTracked() != null)
			product.setExpiryTracked(request.getExpiryTracked());

		product.setCreatedBy(createdBy);

		Product saved = productRepository.save(product);

		ProductAggregateResponse response = buildProductAggregateResponse(saved, category, subCategory,
				companyResponse);

		return new ApiResponse<>(true, "Product created successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				response);

	}

	@Transactional
	@Override
	public ApiResponse<ProductAggregateResponse> updateProduct(UUID productId, ProductUpdateRequest request,
			UUID updatedBy) {

		Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		Category category = request.getCategoryId() != null
				? categoryRepository.findByIdAndDeletedAtIsNull(request.getCategoryId()).orElseThrow(
						() -> new ResourceNotFoundException("Category not found"))
				: null;

		if (category != null && !category.getActive()) {
			throw new InactiveResourceException("Category is inactive");
		}

		SubCategory subCategory = null;
		if (request.getSubCategoryId() != null) {
			subCategory = subCategoryRepository.findByIdAndDeletedAtIsNull(request.getSubCategoryId())
					.orElseThrow(() -> new ResourceNotFoundException("SubCategory not found"));
			if (!subCategory.getActive()) {
				throw new InactiveResourceException("SubCategory is inactive");
			}
		}

		if (request.getProductName() != null)
			product.setProductName(request.getProductName());
		if (request.getDescription() != null)
			product.setDescription(request.getDescription());
		if (request.getBrandName() != null)
			product.setBrandName(request.getBrandName());
		if (request.getModelNumber() != null)
			product.setModelNumber(request.getModelNumber());
		if (request.getCategoryId() != null)
			product.setCategoryId(request.getCategoryId());
		if (request.getSubCategoryId() != null)
			product.setSubCategoryId(request.getSubCategoryId());
		if (request.getVariantBased() != null)
			product.setVariantBased(request.getVariantBased());
		if (request.getSerialized() != null)
			product.setSerialized(request.getSerialized());
		if (request.getBatchTracked() != null)
			product.setBatchTracked(request.getBatchTracked());
		if (request.getExpiryTracked() != null)
			product.setExpiryTracked(request.getExpiryTracked());

		if (request.getActive() != null && request.getActive().equals(product.getActive())) {
			if (request.getActive()) {
				throw new ActiveResourceException("Product is already active");
			} else {
				throw new InactiveResourceException("Product is already inactive");
			}
		}
		if (request.getActive() != null)
			product.setActive(request.getActive());

		product.setUpdatedBy(updatedBy);

		validateProductBehaviour(product);

		Product saved = productRepository.save(product);

		CompanyResponseExternalDto companyResponse;
		try {
			companyResponse = orgServiceClient.getCompanyBasic(saved.getCompanyId()).getBody().getData();
		} catch (FeignException e) {
			HttpStatus status = HttpStatus.resolve(e.status());
			String message = (status == HttpStatus.NOT_FOUND) ? "Company not found!"
					: "Error while communicating with Organization Service";
			throw new OrgServiceException(message, status, e);
		}

		if (category == null) {
			category = categoryRepository.findById(saved.getCategoryId()).orElse(null);
		}
		if (subCategory == null && saved.getSubCategoryId() != null) {
			subCategory = subCategoryRepository.findById(saved.getSubCategoryId()).orElse(null);
		}

		ProductAggregateResponse response = buildProductAggregateResponse(saved, category, subCategory,
				companyResponse);

		return new ApiResponse<>(true, "Product updated successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				response);
	}

	@Transactional
	@Override
	public ApiResponse<List<Object>> deleteProduct(UUID productId, UUID deletedBy) {

		Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		product.setDeletedAt(LocalDateTime.now());
		product.setDeletedBy(deletedBy);
		product.setActive(false);

		productRepository.save(product);

		return new ApiResponse<>(true, "Product deleted successfully", HttpStatus.OK.name(), HttpStatus.OK.value(),
				Collections.emptyList());

	}

	@Transactional(readOnly = true)
	@Override
	public ApiResponse<ProductAggregateResponse> getProductByProductId(UUID productId) {

		Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		if (!product.getActive()) {
			throw new InactiveResourceException("Product is inactive");
		}

		CompanyResponseExternalDto companyResponse;
		try {
			companyResponse = orgServiceClient.getCompanyBasic(product.getCompanyId()).getBody().getData();
		} catch (FeignException e) {
			HttpStatus status = HttpStatus.resolve(e.status());
			String message = (status == HttpStatus.NOT_FOUND) ? "Company not found!"
					: "Error while communicating with Organization Service";
			throw new OrgServiceException(message, status, e);
		}

		Category category = categoryRepository.findByIdAndDeletedAtIsNull(product.getCategoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Category not found"));

		SubCategory subCategory = null;
		if (product.getSubCategoryId() != null) {
			subCategory = subCategoryRepository.findByIdAndDeletedAtIsNull(product.getSubCategoryId())
					.orElseThrow(() -> new ResourceNotFoundException("SubCategory not found"));
		}

		ProductAggregateResponse response = buildProductAggregateResponse(product, category, subCategory,
				companyResponse);

		return new ApiResponse<>(true, "Product details fetch successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), response);

	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse<CompanyProductsResponse> getAllActiveProductsByCompanyId(UUID companyId) {

		CompanyResponseExternalDto companyResponse;
		try {
			companyResponse = orgServiceClient.getCompanyBasic(companyId).getBody().getData();
		} catch (FeignException e) {
			HttpStatus status = HttpStatus.resolve(e.status());
			String message = (status == HttpStatus.NOT_FOUND) ? "Company not found!"
					: "Error while communicating with Organization Service";
			throw new OrgServiceException(message, status, e);
		}

		List<Product> products = productRepository.findByCompanyIdAndDeletedAtIsNullAndActiveTrue(companyId);

		Set<UUID> categoryIds = new HashSet<>();
		Set<UUID> subCategoryIds = new HashSet<>();

		for (Product product : products) {
			categoryIds.add(product.getCategoryId());

			if (product.getSubCategoryId() != null) {
				subCategoryIds.add(product.getSubCategoryId());
			}
		}

		Map<UUID, Category> categoryMap = new HashMap<>();
		if (!categoryIds.isEmpty()) {
			List<Category> categories = categoryRepository.findByIdInAndDeletedAtIsNull(new ArrayList<>(categoryIds));
			for (Category category : categories) {
				categoryMap.put(category.getId(), category);
			}
		}

		Map<UUID, SubCategory> subCategoryMap = new HashMap<>();
		if (!subCategoryIds.isEmpty()) {
			List<SubCategory> subCategories = subCategoryRepository
					.findByIdInAndDeletedAtIsNull(new ArrayList<>(subCategoryIds));
			for (SubCategory subCategory : subCategories) {
				subCategoryMap.put(subCategory.getId(), subCategory);
			}
		}

		CompanyProductsResponse cpdr = new CompanyProductsResponse();
		cpdr.setCompanyId(companyResponse.getCompanyId());
		cpdr.setCompanyCode(companyResponse.getCompanyCode());
		cpdr.setCompanyName(companyResponse.getCompanyName());
		cpdr.setActive(companyResponse.getActive());

		List<ProductCategorySubCategoryResponse> productResponses = new ArrayList<>();

		for (Product product : products) {

			Category category = categoryMap.get(product.getCategoryId());

			SubCategory subCategory = null;
			if (product.getSubCategoryId() != null) {
				subCategory = subCategoryMap.get(product.getSubCategoryId());
			}

			ProductCategorySubCategoryResponse response = buildProductDetailsByCompanyResponse(product, category,
					subCategory);

			productResponses.add(response);
		}

		cpdr.setProducts(productResponses);

		String message = productResponses.isEmpty() ? "No Active product found for this company"
				: "Product details fetched successfully";

		return new ApiResponse<>(true, message, HttpStatus.OK.name(), HttpStatus.OK.value(), cpdr);
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse<CompanyProductsResponse> getAllInactiveProductsByCompanyId(UUID companyId) {

		CompanyResponseExternalDto companyResponse;
		try {
			companyResponse = orgServiceClient.getCompanyBasic(companyId).getBody().getData();
		} catch (FeignException e) {
			HttpStatus status = HttpStatus.resolve(e.status());
			String message = (status == HttpStatus.NOT_FOUND) ? "Company not found!"
					: "Error while communicating with Organization Service";
			throw new OrgServiceException(message, status, e);
		}

		List<Product> products = productRepository.findByCompanyIdAndDeletedAtIsNullAndActiveFalse(companyId);

		Set<UUID> categoryIds = new HashSet<>();
		Set<UUID> subCategoryIds = new HashSet<>();

		for (Product product : products) {
			categoryIds.add(product.getCategoryId());

			if (product.getSubCategoryId() != null) {
				subCategoryIds.add(product.getSubCategoryId());
			}
		}

		Map<UUID, Category> categoryMap = new HashMap<>();
		if (!categoryIds.isEmpty()) {
			List<Category> categories = categoryRepository.findByIdInAndDeletedAtIsNull(new ArrayList<>(categoryIds));
			for (Category category : categories) {
				categoryMap.put(category.getId(), category);
			}
		}

		Map<UUID, SubCategory> subCategoryMap = new HashMap<>();
		if (!subCategoryIds.isEmpty()) {
			List<SubCategory> subCategories = subCategoryRepository
					.findByIdInAndDeletedAtIsNull(new ArrayList<>(subCategoryIds));
			for (SubCategory subCategory : subCategories) {
				subCategoryMap.put(subCategory.getId(), subCategory);
			}
		}

		CompanyProductsResponse cpdr = new CompanyProductsResponse();
		cpdr.setCompanyId(companyResponse.getCompanyId());
		cpdr.setCompanyCode(companyResponse.getCompanyCode());
		cpdr.setCompanyName(companyResponse.getCompanyName());
		cpdr.setActive(companyResponse.getActive());

		List<ProductCategorySubCategoryResponse> productResponses = new ArrayList<>();

		for (Product product : products) {

			Category category = categoryMap.get(product.getCategoryId());

			SubCategory subCategory = null;
			if (product.getSubCategoryId() != null) {
				subCategory = subCategoryMap.get(product.getSubCategoryId());
			}

			ProductCategorySubCategoryResponse response = buildProductDetailsByCompanyResponse(product, category,
					subCategory);

			productResponses.add(response);
		}

		cpdr.setProducts(productResponses);

		String message = productResponses.isEmpty() ? "No Inactive product found for this company"
				: "Product details fetched successfully";

		return new ApiResponse<>(true, message, HttpStatus.OK.name(), HttpStatus.OK.value(), cpdr);
	}

	@Transactional
	@Override
	public ApiResponse<CompanyProductsResponse> bulkUpdateProductStatusByCompany(UUID companyId,
			@Valid BulkProductStatusRequest request, boolean activeStatus, UUID updatedBy) {

		CompanyResponseExternalDto companyResponse;
		try {
			companyResponse = orgServiceClient.getCompanyBasic(companyId).getBody().getData();
		} catch (FeignException e) {
			HttpStatus status = HttpStatus.resolve(e.status());
			String message = (status == HttpStatus.NOT_FOUND) ? "Company not found!"
					: "Error while communicating with Organization Service";
			throw new OrgServiceException(message, status, e);
		}

		List<Product> products = productRepository.findByIdInAndDeletedAtIsNull(request.getProductIds());

		if (products.isEmpty()) {
			throw new ResourceNotFoundException("No Products found for this company");
		}

		if (request.getProductIds().size() != products.size()) {
			throw new ResourceNotFoundException("Some Products do not belong to this Company or are deleted");
		}

		products.forEach(p -> {
			if (!p.getActive().equals(activeStatus)) {
				p.setActive(activeStatus);
				p.setUpdatedBy(updatedBy);
			}
		});

		List<Product> savedAllProducts = productRepository.saveAll(products);
//		---------

		Set<UUID> categoryIds = new HashSet<>();
		Set<UUID> subCategoryIds = new HashSet<>();

		for (Product product : savedAllProducts) {
			categoryIds.add(product.getCategoryId());

			if (product.getSubCategoryId() != null) {
				subCategoryIds.add(product.getSubCategoryId());
			}
		}

		Map<UUID, Category> categoryMap = new HashMap<>();
		if (!categoryIds.isEmpty()) {
			List<Category> categories = categoryRepository.findByIdInAndDeletedAtIsNull(new ArrayList<>(categoryIds));
			for (Category category : categories) {
				categoryMap.put(category.getId(), category);
			}
		}

		Map<UUID, SubCategory> subCategoryMap = new HashMap<>();
		if (!subCategoryIds.isEmpty()) {
			List<SubCategory> subCategories = subCategoryRepository
					.findByIdInAndDeletedAtIsNull(new ArrayList<>(subCategoryIds));
			for (SubCategory subCategory : subCategories) {
				subCategoryMap.put(subCategory.getId(), subCategory);
			}
		}

		CompanyProductsResponse cpdr = new CompanyProductsResponse();
		cpdr.setCompanyId(companyResponse.getCompanyId());
		cpdr.setCompanyCode(companyResponse.getCompanyCode());
		cpdr.setCompanyName(companyResponse.getCompanyName());
		cpdr.setActive(companyResponse.getActive());

		List<ProductCategorySubCategoryResponse> productResponses = new ArrayList<>();

		for (Product product : savedAllProducts) {

			Category category = categoryMap.get(product.getCategoryId());

			SubCategory subCategory = null;
			if (product.getSubCategoryId() != null) {
				subCategory = subCategoryMap.get(product.getSubCategoryId());
			}

			ProductCategorySubCategoryResponse response = buildProductDetailsByCompanyResponse(product, category,
					subCategory);

			productResponses.add(response);
		}

		cpdr.setProducts(productResponses);

		String message = activeStatus ? "Products activated successfully" : "Products deactivated successfully";

		return new ApiResponse<>(true, message, HttpStatus.OK.name(), HttpStatus.OK.value(), cpdr);
	}

	@Override
	@Transactional(readOnly = true)
	public ApiResponse<ProductAggregateResponse> getProductByProductCode(String productCode) {

		Product product = productRepository.findByProductCodeAndDeletedAtIsNull(productCode)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		if (!product.getActive()) {
			throw new InactiveResourceException("Product is inactive");
		}

		CompanyResponseExternalDto companyResponse;
		try {
			companyResponse = orgServiceClient.getCompanyBasic(product.getCompanyId()).getBody().getData();
		} catch (FeignException e) {
			HttpStatus status = HttpStatus.resolve(e.status());
			String message = (status == HttpStatus.NOT_FOUND) ? "Company not found!"
					: "Error while communicating with Organization Service";
			throw new OrgServiceException(message, status, e);
		}

		Category category = categoryRepository.findByIdAndDeletedAtIsNull(product.getCategoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Category not found"));

		SubCategory subCategory = null;
		if (product.getSubCategoryId() != null) {
			subCategory = subCategoryRepository.findByIdAndDeletedAtIsNull(product.getSubCategoryId())
					.orElseThrow(() -> new ResourceNotFoundException("SubCategory not found"));
		}

		ProductAggregateResponse response = buildProductAggregateResponse(product, category, subCategory,
				companyResponse);

		return new ApiResponse<>(true, "Product details fetched successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), response);
	}

	@Transactional
	@Override
	public ApiResponse<List<Object>> bulkDeletionOfProductsByCompanyId(UUID companyId, @Valid BulkProductStatusRequest request, UUID deletedBy) {

		CompanyResponseExternalDto companyResponse;
		try {
			companyResponse = orgServiceClient.getCompanyBasic(companyId).getBody().getData();
		} catch (FeignException e) {
			HttpStatus status = HttpStatus.resolve(e.status());
			String message = (status == HttpStatus.NOT_FOUND) ? "Company not found!"
					: "Error while communicating with Organization Service";
			throw new OrgServiceException(message, status, e);
		}
		
		List<Product> products = productRepository.findByIdInAndCompanyIdAndDeletedAtIsNull(request.getProductIds(), companyId);

		if (products.isEmpty()) {
			throw new ResourceNotFoundException("Products not found");
		}
		
		if (products.size() != request.getProductIds().size()) {
			throw new ResourceNotFoundException("Some Products do not belong to this Company or are already deleted");
		}
		
		products.forEach(p -> {
			p.setDeletedAt(LocalDateTime.now());
			p.setDeletedBy(deletedBy);
			p.setActive(false);
		});
		
		productRepository.saveAll(products);
		
		return new ApiResponse<>(true, "Products deleted successfully", HttpStatus.OK.name(),
				HttpStatus.OK.value(), Collections.emptyList());
	}	
	
	
	
	
	
	
	
	
//	============================HELPER METHOD==================================	

//	 ===== Business Rules ===== 
	private void validateProductBehaviour(ProductCreateRequest req) {

		if (Boolean.TRUE.equals(req.getExpiryTracked()) && !Boolean.TRUE.equals(req.getBatchTracked())) {
			throw new IllegalArgumentException("Expiry tracked product must be batch tracked");
		}

		if (Boolean.TRUE.equals(req.getSerialized()) && !Boolean.TRUE.equals(req.getBatchTracked())) {
			throw new IllegalArgumentException("Serialized product must be batch tracked");
		}

		if (Boolean.TRUE.equals(req.getVariantBased()) && Boolean.TRUE.equals(req.getSerialized())) {
			throw new IllegalArgumentException("Variant based product cannot be serialized at product level");
		}
	}

	private void validateProductBehaviour(Product product) {

		if (Boolean.TRUE.equals(product.getExpiryTracked()) && !Boolean.TRUE.equals(product.getBatchTracked())) {
			throw new IllegalArgumentException("Expiry tracked product must be batch tracked");
		}

		if (Boolean.TRUE.equals(product.getSerialized()) && !Boolean.TRUE.equals(product.getBatchTracked())) {
			throw new IllegalArgumentException("Serialized product must be batch tracked");
		}

		if (Boolean.TRUE.equals(product.getVariantBased()) && Boolean.TRUE.equals(product.getSerialized())) {
			throw new IllegalArgumentException("Variant based product cannot be serialized at product level");
		}
	}

	private ProductAggregateResponse buildProductAggregateResponse(Product product, Category category,
			SubCategory subCategory, CompanyResponseExternalDto companyResponse) {

		ProductAggregateResponse response = new ProductAggregateResponse();
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
		response.setCreatedAt(product.getCreatedAt());
		response.setUpdatedAt(product.getUpdatedAt());

		CompanyResponseInternalDto companyDto = new CompanyResponseInternalDto();
		companyDto.setCompanyId(companyResponse.getCompanyId());
		companyDto.setCompanyCode(companyResponse.getCompanyCode());
		companyDto.setCompanyName(companyResponse.getCompanyName());
		companyDto.setActive(companyResponse.getActive());
		response.setCompany(companyDto);

		CategoryResponse categoryDto = new CategoryResponse();
		categoryDto.setCategoryId(category.getId());
		categoryDto.setCategoryCode(category.getCategoryCode());
		categoryDto.setCategoryName(category.getCategoryName());
		categoryDto.setDescription(category.getDescription());
		categoryDto.setDisplayOrder(category.getDisplayOrder());
		categoryDto.setActive(category.getActive());
		categoryDto.setCreatedAt(category.getCreatedAt());
		response.setCategory(categoryDto);

		SubCategoryResponse subCategoryDto = new SubCategoryResponse();
		subCategoryDto.setSubCategoryId(subCategory.getId());
		subCategoryDto.setSubCategoryCode(subCategory.getSubCategoryCode());
		subCategoryDto.setSubCategoryName(subCategory.getSubCategoryName());
		subCategoryDto.setDescription(subCategory.getDescription());
		subCategoryDto.setDisplayOrder(subCategory.getDisplayOrder());
		subCategoryDto.setActive(subCategory.getActive());
		subCategoryDto.setCreatedAt(subCategory.getCreatedAt());
		response.setSubCategory(subCategoryDto);

		return response;
	}

	private ProductCategorySubCategoryResponse buildProductDetailsByCompanyResponse(Product product, Category category,
			SubCategory subCategory) {

		ProductCategorySubCategoryResponse response = new ProductCategorySubCategoryResponse();
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
		response.setCreatedAt(product.getCreatedAt());
		response.setUpdatedAt(product.getUpdatedAt());

		if (category != null) {
			CategoryResponse categoryDto = new CategoryResponse();
			categoryDto.setCategoryId(category.getId());
			categoryDto.setCategoryCode(category.getCategoryCode());
			categoryDto.setCategoryName(category.getCategoryName());
			categoryDto.setDescription(category.getDescription());
			categoryDto.setDisplayOrder(category.getDisplayOrder());
			categoryDto.setActive(category.getActive());
			categoryDto.setCreatedAt(category.getCreatedAt());
			response.setCategory(categoryDto);
		}

		if (subCategory != null) {
			SubCategoryResponse subCategoryDto = new SubCategoryResponse();
			subCategoryDto.setSubCategoryId(subCategory.getId());
			subCategoryDto.setSubCategoryCode(subCategory.getSubCategoryCode());
			subCategoryDto.setSubCategoryName(subCategory.getSubCategoryName());
			subCategoryDto.setDescription(subCategory.getDescription());
			subCategoryDto.setDisplayOrder(subCategory.getDisplayOrder());
			subCategoryDto.setActive(subCategory.getActive());
			subCategoryDto.setCreatedAt(subCategory.getCreatedAt());
			response.setSubCategory(subCategoryDto);
		}

		return response;
	}


}
