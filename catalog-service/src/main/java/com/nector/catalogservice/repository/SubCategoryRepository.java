package com.nector.catalogservice.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nector.catalogservice.entity.Category;
import com.nector.catalogservice.entity.SubCategory;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, UUID>{

	Optional<SubCategory> findBySubCategoryCode(String subCategoryCode);

	Optional<SubCategory> findByIdAndDeletedAtIsNull(UUID subCategoryId);

	Optional<SubCategory> findByIdAndDeletedAtIsNullAndActiveTrue(UUID subCategoryId);

	List<SubCategory> findByCategoryIdAndDeletedAtIsNullAndActiveTrue(UUID id);

	List<SubCategory> findByCategoryIdAndDeletedAtIsNullAndActiveFalse(UUID id);

	
}
