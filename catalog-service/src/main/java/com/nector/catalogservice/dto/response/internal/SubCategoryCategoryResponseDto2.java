package com.nector.catalogservice.dto.response.internal;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@JsonPropertyOrder({ "categoryId", "categoryCode", "categoryName", "description", "displayOrder", "active"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubCategoryCategoryResponseDto2 {

	private UUID categoryId;
	private String categoryCode;
	private String categoryName;
	private String description;
	private Integer displayOrder;
	private Boolean active;
}
