package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.CareLevelRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CareLevelRateRepository extends JpaRepository<CareLevelRate, Long> {
    List<CareLevelRate> findByCareLevelId(Long careLevelId);
}

