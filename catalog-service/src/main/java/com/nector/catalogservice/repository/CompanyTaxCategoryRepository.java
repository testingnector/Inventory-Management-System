package com.nector.catalogservice.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nector.catalogservice.entity.CompanyTaxCategory;

@Repository
public interface CompanyTaxCategoryRepository extends JpaRepository<CompanyTaxCategory, UUID> {

    Optional<CompanyTaxCategory> findByCompanyIdAndTaxMasterIdAndEffectiveFromAndDeletedAtIsNull(
            UUID companyId, UUID taxMasterId, LocalDate effectiveFrom);

    List<CompanyTaxCategory> findByCompanyIdAndActiveAndDeletedAtIsNull(UUID companyId, Boolean active);

    List<CompanyTaxCategory> findByTaxMasterIdAndActiveAndDeletedAtIsNull(UUID taxMasterId, Boolean active);

    List<CompanyTaxCategory> findByCompanyIdAndDeletedAtIsNullOrderByEffectiveFromDesc(UUID companyId);

	Optional<CompanyTaxCategory> findByIdAndDeletedAtIsNull(UUID id);

	Page<CompanyTaxCategory> findByCompanyIdAndDeletedAtIsNull(UUID companyId, Pageable pageable);

	Page<CompanyTaxCategory> findByCompanyIdAndActiveAndDeletedAtIsNull(UUID companyId, Boolean active,
			Pageable pageable);

	Page<CompanyTaxCategory> findByDeletedAtIsNull(Pageable pageable);

	Page<CompanyTaxCategory> findByActiveAndDeletedAtIsNull(Boolean active, Pageable pageable);

	Optional<CompanyTaxCategory> findByCompanyIdAndTaxMasterIdAndDeletedAtIsNull(UUID companyId, UUID taxMasterId);
}
