package com.mockproject.group3.repository;

import com.mockproject.group3.entity.AssessmentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssessmentDetailRepository extends JpaRepository<AssessmentDetail, Long> {
}

