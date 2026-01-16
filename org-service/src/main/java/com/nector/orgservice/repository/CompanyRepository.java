package com.nector.orgservice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nector.orgservice.entity.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {

	boolean existsByCompanyCode(String companyCode);

	boolean existsByPanNumber(String panNumber);

	boolean existsByGstNumber(String gstNumber);

}
