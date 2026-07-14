package com.mockproject.group3.repository;

import com.mockproject.group3.entity.IncidentTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncidentTimelineRepository extends JpaRepository<IncidentTimeline, Long> {
}

