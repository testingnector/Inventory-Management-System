package com.nector.catalogservice.dto.request.internal;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompanyCategoryCreateRequest {

    @NotNull(message = "Company Id is mandatory")
    private UUID companyId;

    @NotEmpty(message = "At least one Category Id must be provided")
    private List<UUID> categoryIds;
}
