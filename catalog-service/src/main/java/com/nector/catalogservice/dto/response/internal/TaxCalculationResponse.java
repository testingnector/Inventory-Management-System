package com.nector.catalogservice.dto.response.internal;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaxCalculationResponse {
    private Double baseAmount;
    private Double totalTax;
    private Double totalAmount;
    private List<TaxCalculationItem> taxComponents;

//    public TaxCalculationResponse(Double baseAmount, Double totalTax, Double totalAmount, List<TaxCalculationItem> taxComponents) {
//        this.baseAmount = baseAmount;
//        this.totalTax = totalTax;
//        this.totalAmount = totalAmount;
//        this.taxComponents = taxComponents;
//    }
}