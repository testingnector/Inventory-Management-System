package com.nector.catalogservice.dto.request.internal;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BulkDeleteVariantsRequest {

    @NotEmpty(message = "Variant IDs cannot be empty")
    private List<UUID> variantIds;

}
