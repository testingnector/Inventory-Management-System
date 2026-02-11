package com.nector.catalogservice.repository;

import com.nector.catalogservice.entity.Uom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UomRepository extends JpaRepository<Uom, UUID> {

    boolean existsByUomCode(String uomCode);

	Optional<Uom> findByIdAndDeletedAtIsNull(UUID baseUomId);

	Optional<Uom> findByUomCodeAndDeletedAtIsNull(String uomCode);

	List<Uom> findByDeletedAtIsNullAndActiveTrue();

	List<Uom> findByDeletedAtIsNullAndActiveFalse();

	List<Uom> findByActiveAndDeletedAtIsNull(boolean activeStatus);

	List<Uom> findByDeletedAtIsNull();

	List<Uom> findByBaseUomIdIsNullAndDeletedAtIsNullAndActiveTrue();

	List<Uom> findByBaseUomIdAndDeletedAtIsNullAndActiveTrue(UUID baseUomId);

	List<Uom> findByIdInAndDeletedAtIsNull(Set<UUID> uomIds);
}
