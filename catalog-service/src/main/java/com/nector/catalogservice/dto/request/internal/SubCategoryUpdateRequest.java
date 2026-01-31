package com.nector.catalogservice.dto.request.internal;

import java.util.UUID;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubCategoryUpdateRequest {

	    @Size(max = 150, message = "SubCategory name must be at most 150 characters")
	    private String subCategoryName;

	    private UUID categoryId;

	    @Size(max = 500, message = "Description must be at most 500 characters")
	    private String description;

	    private Integer displayOrder;
	    
	    private Boolean active;
}
