package com.nector.catalogservice.dto.request.external;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyIdsRequestDto {

	@NotEmpty(message = "Company id is mandatory")
	private List<UUID> companyIds;
}
