package com.mockproject.group3.repository;

import com.mockproject.group3.entity.DurableMedicalEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DurableMedicalEquipmentRepository extends JpaRepository<DurableMedicalEquipment, Long> {
}

