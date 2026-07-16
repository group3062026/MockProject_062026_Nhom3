package com.mockproject.group3.repository;

import com.mockproject.group3.entity.IncidentSeverity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IncidentSeverityRepository extends JpaRepository<IncidentSeverity, Long> {
    Optional<IncidentSeverity> findByLevelName(String levelName);
}

