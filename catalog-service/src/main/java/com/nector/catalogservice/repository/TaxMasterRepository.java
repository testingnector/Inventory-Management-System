package com.nector.catalogservice.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Repository;

import com.nector.catalogservice.entity.CompanyTaxCategory;
import com.nector.catalogservice.entity.TaxMaster;

@Repository
public interface TaxMasterRepository extends JpaRepository<TaxMaster, UUID> {

    boolean existsByTaxCode(String taxCode);

	Optional<TaxMaster> findByIdAndDeletedAtIsNull(UUID id);

	List<TaxMaster> findByDeletedAtIsNull();

	Optional<TaxMaster> findByTaxCodeAndDeletedAtIsNull(String code);

	List<TaxMaster> findByActiveAndDeletedAtIsNull(boolean activeStatus);

	Page<TaxMaster> findByTaxTypeAndActiveAndDeletedAtIsNull(String taxType, Boolean active, Pageable pageable);

	Page<TaxMaster> findByTaxTypeAndDeletedAtIsNull(String taxType, Pageable pageable);

	Page<TaxMaster> findByActiveAndDeletedAtIsNull(Boolean active, Pageable pageable);

	Page<TaxMaster> findByDeletedAtIsNull(Pageable pageable);

	List<TaxMaster> findByIdInAndDeletedAtIsNull(Set<UUID> taxMasterIds);



}
