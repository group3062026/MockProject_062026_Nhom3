package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, Long> {

    @Query("SELECT r FROM Resident r WHERE " +
           "r.isDeleted = false AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:bedId IS NULL OR r.bed.id = :bedId) AND " +
           "(:search IS NULL OR LOWER(r.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(r.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(r.middleName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Resident> findResidentsFiltered(
            @Param("status") String status,
            @Param("bedId") Long bedId,
            @Param("search") String search,
            Pageable pageable);
    @Query("SELECT r FROM Resident r WHERE r.bed.room.facility.id = :facilityId AND r.status = :status AND r.isDeleted = false")
    List<Resident> findByFacilityIdAndStatus(@Param("facilityId") Long facilityId, @Param("status") String status);

}

