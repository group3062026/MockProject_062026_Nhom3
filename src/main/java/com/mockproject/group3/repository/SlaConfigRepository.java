package com.mockproject.group3.repository;

import com.mockproject.group3.entity.SlaConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SlaConfigRepository extends JpaRepository<SlaConfig, Long> {
}

