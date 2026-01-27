package com.nector.orgservice.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nector.orgservice.entity.Branch;
import com.nector.orgservice.entity.Company;

@Repository
public interface BranchRepository extends JpaRepository<Branch, UUID> {

    boolean existsByBranchCode(String branchCode);

    List<Branch> findByCompanyId(UUID companyId);

	Optional<Branch> findByIdAndDeletedAtIsNullAndActiveTrue(UUID id);

	List<Branch> findByCompanyIdAndDeletedAtIsNull(UUID companyId);

	List<Branch> findByCompanyIdAndDeletedAtIsNullAndActiveTrue(UUID companyId);
}

