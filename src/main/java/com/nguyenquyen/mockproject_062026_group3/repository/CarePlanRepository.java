package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.CarePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarePlanRepository extends JpaRepository<CarePlan, Long> {
    @Query("SELECT cp FROM CarePlan cp " +
            "JOIN FETCH cp.resident r " +
            "WHERE cp.status IN ('ACTIVE', 'REVIEW_DUE', 'NEEDS_UPDATE') AND cp.isDeleted = false")
    List<CarePlan> findAllDashboardPlans();
}
