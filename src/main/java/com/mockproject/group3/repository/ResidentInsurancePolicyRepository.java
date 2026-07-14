package com.mockproject.group3.repository;

import com.mockproject.group3.entity.ResidentInsurancePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResidentInsurancePolicyRepository extends JpaRepository<ResidentInsurancePolicy, Long> {
}

