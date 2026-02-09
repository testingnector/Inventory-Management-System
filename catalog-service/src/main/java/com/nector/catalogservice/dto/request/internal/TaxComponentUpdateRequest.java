package com.nector.catalogservice.dto.request.internal;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class TaxComponentUpdateRequest {

    @PositiveOrZero
    private Double componentRate;
    
    private Boolean active;
}
