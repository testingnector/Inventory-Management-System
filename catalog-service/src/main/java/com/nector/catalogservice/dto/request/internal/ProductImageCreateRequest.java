package com.nector.catalogservice.dto.request.internal;

import java.util.UUID;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductImageCreateRequest {

    private UUID productId;

    private UUID productVariantId;

    @NotBlank(message = "Image URL is required")
    @Size(max = 500, message = "Image URL must be at most 500 characters")
    private String imageUrl;

    @Size(max = 50, message = "Image type must be at most 50 characters")
    private String imageType;

    @Size(max = 255, message = "Alt text must be at most 255 characters")
    private String altText;

    @NotNull(message = "Primary flag cannot be null")
    private Boolean primary = false;

    @Min(value = 0, message = "Display order cannot be negative")
    private Integer displayOrder;
}
