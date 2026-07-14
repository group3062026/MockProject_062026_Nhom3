package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.ResidentCareLevelHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResidentCareLevelHistoryRepository extends JpaRepository<ResidentCareLevelHistory, Long> {
    boolean existsByCareLevelIdAndEndDateIsNullAndResidentStatus(Long careLevelId, String status);
    
    // Find the current active care level record for a resident
    java.util.Optional<ResidentCareLevelHistory> findByResidentIdAndEndDateIsNull(Long residentId);
    
    // Find history records for a resident sorted by start date descending
    java.util.List<ResidentCareLevelHistory> findAllByResidentIdOrderByStartDateDesc(Long residentId);
}

