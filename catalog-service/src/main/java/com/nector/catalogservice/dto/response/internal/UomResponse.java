package com.nector.catalogservice.dto.response.internal;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"uomId", "uomCode", "uomName", "uomType", "active", "createdAt", "updatedAt", "baseUomDetails"})
@Data
public class UomResponse {

	private UUID uomId;
    private String uomCode;
    private String uomName;
    private String uomType;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BaseUomDetails baseUomDetails;
    
}
