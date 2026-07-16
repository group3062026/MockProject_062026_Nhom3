package com.mockproject.group3.repository;

import com.mockproject.group3.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByFacilityIdAndIsDeletedFalse(Long facilityId);
    
    boolean existsByFacilityIdAndRoomNumberAndIsDeletedFalse(Long facilityId, String roomNumber);
}

