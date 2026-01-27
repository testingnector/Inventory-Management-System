package com.nector.orgservice.dto.response.internal;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyResponse {

    private UUID id;
    private String companyCode;
    private String companyName;
    private String legalName;
    private String companyType;
    private String gstNumber;
    private String panNumber;
    private String email;
    private String phone;
    private String address;
    private String country;
    private String state;
    private String city;
    private String pincode;
    private Boolean isActive; 
    
    
}
