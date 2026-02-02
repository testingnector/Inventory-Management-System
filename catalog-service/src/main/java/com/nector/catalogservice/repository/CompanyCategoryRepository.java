package com.nector.catalogservice.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nector.catalogservice.entity.CompanyCategory;

@Repository
public interface CompanyCategoryRepository extends JpaRepository<CompanyCategory, UUID> {

    Optional<CompanyCategory> findByCompanyIdAndCategoryIdAndDeletedAtIsNull(UUID companyId, UUID categoryId);

    List<CompanyCategory> findByCompanyIdAndDeletedAtIsNull(UUID companyId);

	List<CompanyCategory> findAllByCompanyIdAndCategoryIdInAndDeletedAtIsNull(UUID companyId, List<UUID> categoryIds);

	Optional<CompanyCategory> findByIdAndDeletedAtIsNull(UUID id);

	List<CompanyCategory> findByCompanyIdAndDeletedAtIsNullAndActiveTrue(UUID companyId);

	List<CompanyCategory> findByCompanyIdAndDeletedAtIsNullAndActiveFalse(UUID companyId);

	List<CompanyCategory> findByIdInAndDeletedAtIsNull(List<UUID> companyCategoryIds);

}

