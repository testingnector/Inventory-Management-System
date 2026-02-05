package com.nector.catalogservice.dto.response.internal;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({"baseUomId", "uomCode", "uomName", "uomType", "active"})
@Data
public class BaseUomDetails {

	private UUID baseUomId;
    private String uomCode;
    private String uomName;
    private String uomType;
    private Boolean active;
}
