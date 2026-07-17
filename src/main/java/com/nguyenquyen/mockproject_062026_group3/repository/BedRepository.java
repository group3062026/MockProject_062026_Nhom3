package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.Bed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BedRepository extends JpaRepository<Bed, Long> {
    List<Bed> findByRoomId(Long roomId);
    
    boolean existsByRoomIdAndBedNumber(Long roomId, String bedNumber);
    
    boolean existsByRoomIdAndStatus(Long roomId, String status);
}

