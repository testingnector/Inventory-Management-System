package com.nector.auth.dto.request.external;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CompanyIdsRequestDto {

	@NotEmpty(message = "Company id is mandatory")
	private List<UUID> companyIds;
}