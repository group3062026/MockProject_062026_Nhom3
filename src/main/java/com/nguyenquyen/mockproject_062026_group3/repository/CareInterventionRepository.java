package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.CareIntervention;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CareInterventionRepository extends JpaRepository<CareIntervention, Long> {
  List<CareIntervention> findByCarePlanId(Long carePlanId);
}

