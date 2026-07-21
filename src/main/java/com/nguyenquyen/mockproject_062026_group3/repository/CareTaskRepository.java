package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.CareTask;



import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;


@Repository
public interface CareTaskRepository extends JpaRepository<CareTask, Long> {

    @Query("select t from CareTask t where t.scheduledTime < :now and t.status= :status")
    List<CareTask> findOverTime(@Param("now") OffsetDateTime now, @Param("status") String status);
    @Query("SELECT t FROM CareTask t " +
            "JOIN FETCH t.careIntervention ci " +
            "JOIN FETCH ci.carePlan cp " +
            "JOIN FETCH cp.resident r " +
            "WHERE t.assignedCna.id = :cnaId " +
            "AND t.scheduledTime >= :startTime " +
            "AND t.scheduledTime <= :endTime")
    List<CareTask> findTasksForShift(
            @Param("cnaId") Long cnaId,
            @Param("startTime") OffsetDateTime startTime,
            @Param("endTime") OffsetDateTime endTime
    );




  List<CareTask> findByCareInterventionCarePlanId(Long carePlanId);

}

