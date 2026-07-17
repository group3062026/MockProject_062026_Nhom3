package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.MedicationOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationOrderRepository extends JpaRepository<MedicationOrder, Long> {

    List<MedicationOrder> findByResidentIdAndStatus(Long residentId, String status);

    @Query("SELECT o FROM MedicationOrder o WHERE o.resident.id IN :residentIds AND o.status = :status AND o.isDeleted = false")
    List<MedicationOrder> findByResidentIdInAndStatus(@Param("residentIds") List<Long> residentIds,
                                                      @Param("status") String status);

    @Query("SELECT o FROM MedicationOrder o WHERE o.resident.id = :residentId AND o.status = :status AND o.isDeleted = false")
    Page<MedicationOrder> findByResidentIdAndStatusWithPagination(@Param("residentId") Long residentId,
                                                                  @Param("status") String status,
                                                                  Pageable pageable);

    @Query("SELECT o FROM MedicationOrder o WHERE o.resident.id IN :residentIds AND o.status = :status AND o.isDeleted = false")
    Page<MedicationOrder> findByResidentIdInAndStatusWithPagination(@Param("residentIds") List<Long> residentIds,
                                                                    @Param("status") String status,
                                                                    Pageable pageable);

    List<MedicationOrder> findByResidentIdAndDrugNameAndStatus(Long residentId, String drugName, String status);

    List<MedicationOrder> findAllById(Iterable<Long> ids);
}
