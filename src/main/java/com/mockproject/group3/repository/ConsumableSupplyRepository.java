package com.mockproject.group3.repository;

import com.mockproject.group3.entity.ConsumableSupply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsumableSupplyRepository extends JpaRepository<ConsumableSupply, Long> {
    
    @Query("SELECT s FROM ConsumableSupply s " +
           "WHERE s.isDeleted = false " +
           "AND (:facilityId IS NULL OR s.facility.id = :facilityId) " +
           "AND (:categoryId IS NULL OR s.category.id = :categoryId) " +
           "AND (:status IS NULL OR s.status = :status) " +
           "AND (:search IS NULL OR LOWER(s.itemName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ConsumableSupply> searchSupplies(@Param("facilityId") Long facilityId,
                                          @Param("categoryId") Long categoryId,
                                          @Param("status") String status,
                                          @Param("search") String search,
                                          Pageable pageable);

    List<ConsumableSupply> findByFacilityIdAndStatusAndIsDeletedFalse(Long facilityId, String status);
}

