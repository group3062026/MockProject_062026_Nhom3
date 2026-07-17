package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.ResidentContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResidentContactRepository extends JpaRepository<ResidentContact, Long> {

    List<ResidentContact> findByContactId(Long contactId);

    boolean existsByContactIdAndIsGuarantorTrueAndResidentStatusAndResidentIsDeletedFalse(
            Long contactId, String residentStatus);
}

