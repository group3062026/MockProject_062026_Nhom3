package com.mockproject.group3.repository;

import com.mockproject.group3.entity.CareGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CareGoalRepository extends JpaRepository<CareGoal, Long> {
}

