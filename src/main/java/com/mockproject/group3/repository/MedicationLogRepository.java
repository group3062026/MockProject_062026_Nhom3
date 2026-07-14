package com.mockproject.group3.repository;

import com.mockproject.group3.entity.MedicationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicationLogRepository extends JpaRepository<MedicationLog, Long> {
}

