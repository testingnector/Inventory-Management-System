package com.nector.orgservice.dto.response.internal;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonPropertyOrder({"branchId", "branchCode", "branchName", "city", "address", "active", "headOffice", "createdAt"})
@Data
public class CompanyBranchResponseDto2 {

    private UUID branchId;
    private String branchCode;
    private String branchName;
    private String city;
    private String address;
    private Boolean active;
    private Boolean headOffice;
    
	private LocalDateTime createdAt;
        
}
