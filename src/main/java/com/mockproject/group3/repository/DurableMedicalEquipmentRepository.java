package com.mockproject.group3.repository;

import com.mockproject.group3.entity.DurableMedicalEquipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DurableMedicalEquipmentRepository extends JpaRepository<DurableMedicalEquipment, Long> {
    
    @Query("SELECT e FROM DurableMedicalEquipment e " +
           "WHERE e.isDeleted = false " +
           "AND (:facilityId IS NULL OR e.facility.id = :facilityId) " +
           "AND (:categoryId IS NULL OR e.category.id = :categoryId) " +
           "AND (:status IS NULL OR e.status = :status) " +
           "AND (:search IS NULL OR LOWER(e.itemName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(e.assetTag) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<DurableMedicalEquipment> searchEquipment(@Param("facilityId") Long facilityId,
                                                  @Param("categoryId") Long categoryId,
                                                  @Param("status") String status,
                                                  @Param("search") String search,
                                                  Pageable pageable);

    boolean existsByAssetTag(String assetTag);
}

