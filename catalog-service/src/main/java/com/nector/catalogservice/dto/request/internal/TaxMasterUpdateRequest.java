package com.nector.catalogservice.dto.request.internal;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaxMasterUpdateRequest {

    @Size(max = 50, message = "Tax name must be at most 40 characters")
    private String taxName;

    @Size(max = 60, message = "Tax type must be at most 60 characters")
    @Pattern(regexp = "GST|VAT|SALES_TAX", message = "taxType must be GST, VAT, or SALES_TAX")
    private String taxType;

    private Boolean compoundTax;

    @Size(max = 100, message = "Tax description must be at most 100 characters")
    private String description;
    
    private Boolean active;

}
