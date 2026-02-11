package com.nector.orgservice.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nector.orgservice.entity.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {

	boolean existsByCompanyCode(String companyCode);

	boolean existsByPanNumber(String panNumber);

	boolean existsByGstNumber(String gstNumber);

	Optional<Company> findByIdAndDeletedAtIsNullAndActiveTrue(UUID companyId);

	List<Company> findByIdInAndDeletedAtIsNullAndActiveTrue(List<UUID> companyIds);

	Optional<Company> findByIdAndDeletedAtIsNull(UUID companyId);

}
