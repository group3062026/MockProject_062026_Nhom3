package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.IncidentTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncidentTimelineRepository extends JpaRepository<IncidentTimeline, Long> {
}

