package com.mockproject.group3.repository;

import com.mockproject.group3.entity.CareTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CareTaskRepository extends JpaRepository<CareTask, Long> {
}

