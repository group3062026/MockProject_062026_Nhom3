package com.mockproject.group3.repository;

import com.mockproject.group3.entity.CareLevelRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CareLevelRateRepository extends JpaRepository<CareLevelRate, Long> {
}

