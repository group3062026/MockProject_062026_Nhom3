package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.MedicationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface MedicationLogRepository extends JpaRepository<MedicationLog, Long> {

    @Query("SELECT l FROM MedicationLog l WHERE l.order.id IN :orderIds AND DATE(l.loggedAt) = :date")
    List<MedicationLog> findByOrderIdInAndLoggedAtDate(@Param("orderIds") List<Long> orderIds,
                                                       @Param("date") LocalDate date);

    @Query("SELECT l FROM MedicationLog l WHERE l.order.id IN :orderIds AND l.loggedAt BETWEEN :start AND :end")
    List<MedicationLog> findByOrderIdInAndLoggedAtBetween(@Param("orderIds") List<Long> orderIds,
                                                          @Param("start") OffsetDateTime start,
                                                          @Param("end") OffsetDateTime end);

    List<MedicationLog> findByOrderIdInAndStatus(List<Long> orderIds, String status);

    @Query("SELECT l FROM MedicationLog l WHERE l.order.id = :orderId AND l.loggedAt >= :dateTime")
    List<MedicationLog> findByOrderIdAndLoggedAtAfter(@Param("orderId") Long orderId,
                                                      @Param("dateTime") OffsetDateTime dateTime);

    List<MedicationLog> findAllById(Iterable<Long> ids);
}