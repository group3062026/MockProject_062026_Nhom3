package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.CareLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CareLevelRepository extends JpaRepository<CareLevel, Long> {
    List<CareLevel> findAllByIsDeletedFalse();

    List<CareLevel> findAllByIsDeleted(Boolean isDeleted);
}
