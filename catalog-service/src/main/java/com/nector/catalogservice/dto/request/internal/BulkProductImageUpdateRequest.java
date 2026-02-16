package com.nector.catalogservice.dto.request.internal;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BulkProductImageUpdateRequest {
	
	private UUID productImageId;
	
    @Size(max = 50, message = "Image type must be at most 50 characters")
    private String imageType;

    @Size(max = 255, message = "Alt text must be at most 255 characters")
    private String altText;

    private Boolean primary = false;

    @Min(value = 0, message = "Display order cannot be negative")
    private Integer displayOrder;
    
    private Boolean active;
}
