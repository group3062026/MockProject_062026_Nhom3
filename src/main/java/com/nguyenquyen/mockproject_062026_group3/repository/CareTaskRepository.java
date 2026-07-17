package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.CareTask;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CareTaskRepository extends JpaRepository<CareTask, Long> {
  List<CareTask> findByCareInterventionCarePlanId(Long carePlanId);
}

