package com.nguyenquyen.mockproject_062026_group3.repository;

import com.nguyenquyen.mockproject_062026_group3.entity.ResidentInsurancePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResidentInsurancePolicyRepository extends JpaRepository<ResidentInsurancePolicy, Long> {
    // Lấy tất cả bảo hiểm đang kích hoạt của bệnh nhân (không lấy những cái đã xóa)
    List<ResidentInsurancePolicy> findByResidentIdAndIsDeletedFalse(Long residentId);
}

