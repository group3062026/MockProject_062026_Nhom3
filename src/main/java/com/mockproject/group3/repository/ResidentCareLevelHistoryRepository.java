package com.mockproject.group3.repository;

import com.mockproject.group3.entity.ResidentCareLevelHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResidentCareLevelHistoryRepository extends JpaRepository<ResidentCareLevelHistory, Long> {

    List<ResidentCareLevelHistory> findByResidentIdOrderByStartDateDesc(Long residentId);

    Optional<ResidentCareLevelHistory> findByResidentIdAndEndDateIsNull(Long residentId);

    @Query("SELECT h.careLevel.levelCode, COUNT(h) FROM ResidentCareLevelHistory h " +
           "JOIN h.resident r JOIN r.bed b JOIN b.room rm " +
           "WHERE rm.facility.id = :facilityId AND h.endDate IS NULL AND r.status = 'ACTIVE' " +
           "GROUP BY h.careLevel.levelCode")
    List<Object[]> countActiveResidentsByCareLevelForFacility(@Param("facilityId") Long facilityId);
}

