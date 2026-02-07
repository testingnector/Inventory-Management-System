package com.nector.catalogservice.dto.response.internal;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonPropertyOrder({ "subCategoryId", "subCategoryCode", "subCategoryName", "description", "displayOrder", "active",
		"createdAt", "category" })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubCategoryCategoryResponse {

	private UUID subCategoryId;
	private String subCategoryCode;
	private String subCategoryName;
	private String description;
	private Integer displayOrder;
	private Boolean active;
	private LocalDateTime createdAt;

	private CategoryResponse category;
}
