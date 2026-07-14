package com.mockproject.group3.repository;

import com.mockproject.group3.entity.UserFacility;
import com.mockproject.group3.entity.key.UserFacilityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFacilityRepository extends JpaRepository<UserFacility, UserFacilityId> {
}

