package com.nector.catalogservice.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nector.catalogservice.entity.CompanyTaxCategory;
import com.nector.catalogservice.entity.TaxComponent;
import com.nector.catalogservice.enums.TaxComponentType;

@Repository
public interface TaxComponentRepository extends JpaRepository<TaxComponent, UUID>{

	Optional<TaxComponent> findByCompanyTaxCategoryIdAndComponentType(UUID companyTaxCategoryId, TaxComponentType componentType);

	Optional<TaxComponent> findByIdAndDeletedAtIsNull(UUID taxComponentId);

	List<TaxComponent> findAllByCompanyTaxCategoryIdAndDeletedAtIsNull(UUID companyTaxCategoryId);

	Page<TaxComponent> findAllByDeletedAtIsNull(PageRequest pageRequest);

	Page<TaxComponent> findAllByActiveAndDeletedAtIsNull(Boolean active, PageRequest pageRequest);

	List<TaxComponent> findAllByCompanyTaxCategoryIdAndActiveTrueAndDeletedAtIsNull(UUID id);

	List<TaxComponent> findByActiveAndDeletedAtIsNull(boolean activeStatus);

	List<TaxComponent> findByCompanyTaxCategoryIdInAndDeletedAtIsNull(List<UUID> companyTaxCategoryIds);


}
