package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

  List<AuditLog> findByIdOrderByPerformedAtDesc(Long entityId);



    @Query("SELECT a FROM AuditLog a WHERE a.tableName = :tableName AND a.recordId = :recordId AND a.performedAt BETWEEN :start AND :end")
    Page<AuditLog> findByTableNameAndRecordIdAndPerformedAtBetween(
            @Param("tableName") String tableName,
            @Param("recordId") String recordId,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.tableName = :tableName AND a.recordId IN :recordIds AND a.performedAt BETWEEN :start AND :end")
    Page<AuditLog> findByTableNameAndRecordIdInAndPerformedAtBetween(
            @Param("tableName") String tableName,
            @Param("recordIds") List<String> recordIds,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            Pageable pageable);


    @Query("SELECT a FROM AuditLog a WHERE a.tableName = :tableName AND a.performedAt BETWEEN :start AND :end")
    Page<AuditLog> findByTableNameAndPerformedAtBetween(
            @Param("tableName") String tableName,
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            Pageable pageable);
}