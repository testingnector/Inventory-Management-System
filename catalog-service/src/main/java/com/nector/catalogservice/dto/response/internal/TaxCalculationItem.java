package com.nector.catalogservice.dto.response.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaxCalculationItem {
    private String componentType;
    private Double componentRate;
    private Double taxAmount;

//    public TaxCalculationItem(String componentType, Double componentRate, Double taxAmount) {
//        this.componentType = componentType;
//        this.componentRate = componentRate;
//        this.taxAmount = taxAmount;
//    }
}