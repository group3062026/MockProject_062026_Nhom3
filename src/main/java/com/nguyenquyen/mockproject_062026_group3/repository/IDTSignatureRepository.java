package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.IDTSignature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDTSignatureRepository extends JpaRepository<IDTSignature, Long> {

    List<IDTSignature> findByCarePlanId(Long carePlanId);

    boolean existsByCarePlanIdAndUserId(Long carePlanId, Long userId);
}
