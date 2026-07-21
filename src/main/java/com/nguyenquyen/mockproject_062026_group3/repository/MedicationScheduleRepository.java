package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.MedicationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationScheduleRepository extends JpaRepository<MedicationSchedule, Long> {


    @Query("SELECT s FROM MedicationSchedule s WHERE s.order.id = :orderId AND s.isActive = true")
    List<MedicationSchedule> findByOrderIdAndIsActive(@Param("orderId") Long orderId);

    @Query("SELECT s FROM MedicationSchedule s WHERE s.order.id IN :orderIds AND s.isActive = true")
    List<MedicationSchedule> findByOrderIdInAndIsActiveTrue(@Param("orderIds") List<Long> orderIds);

    List<MedicationSchedule> findAllById(Iterable<Long> ids);
}