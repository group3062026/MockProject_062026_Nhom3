package com.mockproject.group3.repository;

import com.mockproject.group3.entity.ConsumableSupply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsumableSupplyRepository extends JpaRepository<ConsumableSupply, Long> {
}

