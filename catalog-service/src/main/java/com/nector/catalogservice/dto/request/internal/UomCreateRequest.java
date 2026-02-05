package com.nector.catalogservice.dto.request.internal;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UomCreateRequest {
	
    @NotBlank(message = "UOM code is required")
    @Size(max = 50, message = "UOM code can be at most 50 characters")
    private String uomCode;

    @NotBlank(message = "UOM name is required")
    @Size(max = 100, message = "UOM name can be at most 100 characters")
    private String uomName;

    @Size(max = 50, message = "UOM type can be at most 50 characters")
    private String uomType;

    private UUID baseUomId; 
    
}