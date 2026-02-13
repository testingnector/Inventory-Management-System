package com.nector.catalogservice.mapper;

import java.util.UUID;

import com.nector.catalogservice.dto.request.internal.ProductImageCreateRequest;
import com.nector.catalogservice.entity.ProductImage;

public class ProductImageMapping {

	public static ProductImage toEntityWithCreation(ProductImageCreateRequest request, UUID createdBy) {
		if (request == null) {
			return null;
		}

		ProductImage image = new ProductImage();
		image.setProductId(request.getProductId());
		image.setProductVariantId(request.getProductVariantId());
		image.setImageUrl(request.getImageUrl());
		image.setImageType(request.getImageType());
		image.setAltText(request.getAltText());
		image.setPrimary(request.getPrimary() != null ? request.getPrimary() : false);
		image.setDisplayOrder(request.getDisplayOrder());
		image.setCreatedBy(createdBy);

		return image;
	}
}
