package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {
    @Query("SELECT sa FROM ShiftAssignment sa JOIN FETCH sa.shift " +
            "WHERE sa.user.id = :userId AND sa.workDate = :date AND sa.status = 'SCHEDULED'")
    Optional<ShiftAssignment> findConfirmedShiftForUser(@Param("userId") Long userId, @Param("date") LocalDate date);
}
