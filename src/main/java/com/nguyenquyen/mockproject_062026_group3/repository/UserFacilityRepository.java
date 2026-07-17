package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.UserFacility;
import com.nguyenquyen.mockproject_062026_group3.entity.key.UserFacilityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFacilityRepository extends JpaRepository<UserFacility, UserFacilityId> {
    List<UserFacility> findByUserId(Long userId);
    
    void deleteByUserId(Long userId);
}

