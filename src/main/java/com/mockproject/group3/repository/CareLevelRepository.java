package com.mockproject.group3.repository;

import com.mockproject.group3.entity.CareLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CareLevelRepository extends JpaRepository<CareLevel, Long> {
    List<CareLevel> findAllByIsDeletedFalse();
}

