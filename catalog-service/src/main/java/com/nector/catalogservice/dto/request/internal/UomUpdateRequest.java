package com.nector.catalogservice.dto.request.internal;

import java.util.UUID;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UomUpdateRequest {

    @Size(max = 100, message = "UOM name can be at most 100 characters")
    private String uomName;

    @Size(max = 50, message = "UOM type can be at most 50 characters")
    private String uomType;

    private UUID baseUomId;
    
    private Boolean active;
    
}
