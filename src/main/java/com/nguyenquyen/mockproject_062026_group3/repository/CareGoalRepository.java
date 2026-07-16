package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.CareGoal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CareGoalRepository extends JpaRepository<CareGoal, Long> {
  List<CareGoal> findByCarePlanId(Long carePlanId);
}
