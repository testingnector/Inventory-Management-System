package com.nector.catalogservice.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nector.catalogservice.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

	Optional<Category> findByIdAndDeletedAtIsNull(UUID categoryId);

	Optional<Category> findByIdAndDeletedAtIsNullAndActiveTrue(UUID categoryId);

	Optional<Category> findByCategoryCodeAndDeletedAtIsNullAndActiveTrue(String categoryCode);

	List<Category> findByCompanyIdAndDeletedAtIsNullAndActiveTrue(UUID companyId);

	Optional<Category> findByCategoryCodeAndDeletedAtIsNull(String categoryCode);

	List<Category> findByCompanyIdAndDeletedAtIsNullAndActiveFalse(UUID companyId);

	List<Category> findByIdInAndDeletedAtIsNull(List<UUID> categoryIds);



}

