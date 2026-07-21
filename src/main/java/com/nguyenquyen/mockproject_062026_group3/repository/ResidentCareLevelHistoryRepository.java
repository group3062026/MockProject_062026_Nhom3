package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.ResidentCareLevelHistory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResidentCareLevelHistoryRepository extends JpaRepository<ResidentCareLevelHistory, Long> {
    boolean existsByCareLevelIdAndEndDateIsNullAndResidentStatus(Long careLevelId, String status);

    // Find the current active care level record for a resident
    Optional<ResidentCareLevelHistory> findByResidentIdAndEndDateIsNull(Long residentId);
    
    // Find history records for a resident sorted by start date descending
    List<ResidentCareLevelHistory> findAllByResidentIdOrderByStartDateDesc(Long residentId);

    Optional<ResidentCareLevelHistory> findFirstByResidentIdAndEndDateIsNullOrderByStartDateDesc(Long residentId);



    List<ResidentCareLevelHistory> findByResidentIdOrderByStartDateDesc(Long residentId);







    @Query("SELECT h.careLevel.levelCode, COUNT(h) FROM ResidentCareLevelHistory h " +
           "JOIN h.resident r JOIN r.bed b JOIN b.room rm " +
           "WHERE rm.facility.id = :facilityId AND h.endDate IS NULL AND r.status = 'ACTIVE' " +
           "GROUP BY h.careLevel.levelCode")
    List<Object[]> countActiveResidentsByCareLevelForFacility(@Param("facilityId") Long facilityId);
}
