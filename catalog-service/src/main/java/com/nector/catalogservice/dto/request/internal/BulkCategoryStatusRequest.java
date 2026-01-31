package com.nector.catalogservice.dto.request.internal;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class BulkCategoryStatusRequest {

    @NotEmpty(message = "Category ids cannot be empty")
    private List<UUID> categoryIds;
}

