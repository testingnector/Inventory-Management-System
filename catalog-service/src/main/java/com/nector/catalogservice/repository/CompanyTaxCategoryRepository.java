package com.nector.catalogservice.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.nector.catalogservice.entity.CompanyTaxCategory;

@Repository
public interface CompanyTaxCategoryRepository extends JpaRepository<CompanyTaxCategory, UUID> {

	@Query("""
			    SELECT COUNT(c) > 0
			    FROM CompanyTaxCategory c
			    WHERE c.companyId = :companyId
			      AND c.taxMasterId = :taxMasterId
			      AND c.deletedAt IS NULL
			      AND c.active = true
			      AND c.effectiveFrom <= :effectiveFrom
			      AND (
			            c.effectiveTo IS NULL
			            OR c.effectiveTo >= :effectiveFrom
			          )
			""")
	boolean existsCurrentTax(UUID companyId, UUID taxMasterId, LocalDate effectiveFrom);

	@Query("""
			    SELECT COUNT(c) > 0
			    FROM CompanyTaxCategory c
			    WHERE c.companyId = :companyId
			      AND c.taxMasterId = :taxMasterId
			      AND c.deletedAt IS NULL
			      AND c.active = true
			      AND c.id <> :id
			      AND c.effectiveFrom <= :today
			      AND (c.effectiveTo IS NULL OR c.effectiveTo >= :today)
			""")
	boolean existsAnotherCurrentActive(UUID companyId, UUID taxMasterId, UUID id, LocalDate today);

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

	@Query("""
			SELECT c
			FROM CompanyTaxCategory c
			WHERE c.companyId = :companyId
				AND c.taxMasterId = :taxMasterId
				AND c.deletedAt IS NULL
				AND c.active = true
				AND c.effectiveFrom <= :today
				AND (c.effectiveTo IS NULL OR c.effectiveTo >= :today)
			ORDER BY c.effectiveFrom DESC
			""")
	Optional<CompanyTaxCategory> findCurrentTaxCategory(UUID companyId, UUID taxMasterId, LocalDate today);

	@Query("""
			    SELECT c
			    FROM CompanyTaxCategory c
			    WHERE c.companyId = :companyId
			      AND c.taxMasterId = :taxMasterId
			      AND c.deletedAt IS NULL
			      AND (c.effectiveTo < :today OR c.active = false)
			    ORDER BY c.effectiveFrom DESC
			""")
	List<CompanyTaxCategory> findHistoryByCompanyAndTax(UUID companyId, UUID taxMasterId, LocalDate today);

	@Query("""
			    SELECT c
			    FROM CompanyTaxCategory c
			    WHERE c.companyId = :companyId
			      AND c.deletedAt IS NULL
			      AND c.active = true
			      AND c.effectiveFrom <= :today
			      AND (c.effectiveTo IS NULL OR c.effectiveTo >= :today)
			    ORDER BY c.effectiveFrom DESC
			""")
	List<CompanyTaxCategory> findAllCurrentByCompanyId(UUID companyId, LocalDate today);

	@Query("""
			    SELECT c
			    FROM CompanyTaxCategory c
			    WHERE c.companyId = :companyId
			      AND c.deletedAt IS NULL
			      AND (c.effectiveTo < :today OR c.active = false)
			    ORDER BY c.effectiveFrom DESC
			""")
	List<CompanyTaxCategory> findHistoryByCompanyId(UUID companyId, LocalDate today);

}
