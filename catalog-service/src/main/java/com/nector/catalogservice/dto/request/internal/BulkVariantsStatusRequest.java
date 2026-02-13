package com.nector.catalogservice.dto.request.internal;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class BulkVariantsStatusRequest {

	@NotEmpty(message = "Variant Ids are mandatory")
	private List<UUID> variantIds;
}
