package com.mockproject.group3.repository;

import com.mockproject.group3.entity.StaffingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffingConfigRepository extends JpaRepository<StaffingConfig, Long> {
    Optional<StaffingConfig> findByFacilityId(Long facilityId);
}

