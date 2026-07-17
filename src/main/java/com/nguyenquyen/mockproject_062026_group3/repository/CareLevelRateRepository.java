package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.CareLevelRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CareLevelRateRepository extends JpaRepository<CareLevelRate, Long> {

    List<CareLevelRate> findByCareLevelId(Long careLevelId);

    @Query("SELECT r FROM CareLevelRate r WHERE " +
           "(:facilityId IS NULL OR r.facility.id = :facilityId) AND " +
           "(:careLevelId IS NULL OR r.careLevel.id = :careLevelId) AND " +
           "(:activeOnly = false OR (r.effectiveFrom <= :currentDate AND (r.effectiveTo IS NULL OR r.effectiveTo >= :currentDate)))")
    List<CareLevelRate> findRatesFiltered(
            @Param("facilityId") Long facilityId,
            @Param("careLevelId") Long careLevelId,
            @Param("activeOnly") boolean activeOnly,
            @Param("currentDate") LocalDate currentDate);

    Optional<CareLevelRate> findByFacilityIdAndCareLevelIdAndEffectiveToIsNull(Long facilityId, Long careLevelId);

    List<CareLevelRate> findByFacilityIdAndEffectiveToIsNull(Long facilityId);
  Optional<CareLevelRate> findFirstByCareLevelIdOrderByEffectiveFromDesc(Long careLevelId);
}
