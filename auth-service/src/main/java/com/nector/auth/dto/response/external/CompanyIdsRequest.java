package com.nector.auth.dto.response.external;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CompanyIdsRequest {

	@NotEmpty(message = "Company id is mandatory")
	private List<UUID> companyIds;
}