package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.PhiAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface PhiAccessLogRepository extends JpaRepository<PhiAccessLog, Long> {

    @Query("SELECT p FROM PhiAccessLog p WHERE p.recordId = :recordId AND p.accessedAt BETWEEN :start AND :end")
    List<PhiAccessLog> findByRecordIdAndAccessedAtBetween(
            @Param("recordId") String recordId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end);

    @Query("SELECT p FROM PhiAccessLog p WHERE p.recordId = :recordId AND p.accessType = :accessType AND p.accessedAt BETWEEN :start AND :end")
    List<PhiAccessLog> findByRecordIdAndAccessTypeAndAccessedAtBetween(
            @Param("recordId") String recordId,
            @Param("accessType") String accessType,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end);

    @Query("SELECT p FROM PhiAccessLog p WHERE p.accessedAt BETWEEN :start AND :end")
    List<PhiAccessLog> findByAccessedAtBetween(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end);
}