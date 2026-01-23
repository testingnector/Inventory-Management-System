package com.nector.orgservice.dto.request.external;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CompanyIdsRequest {

	@NotEmpty(message = "Company id is mandatory")
	private List<UUID> companyIds;
}
