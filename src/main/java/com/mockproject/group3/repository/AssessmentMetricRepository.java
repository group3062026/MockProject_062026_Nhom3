package com.mockproject.group3.repository;

import com.mockproject.group3.entity.AssessmentMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssessmentMetricRepository extends JpaRepository<AssessmentMetric, Long> {
}

