package com.mockproject.group3.service;

import com.mockproject.group3.dto.admin.inventory.AdjustStockRequest;
import com.mockproject.group3.dto.admin.inventory.ConsumeStockRequest;
import com.mockproject.group3.dto.admin.inventory.CreateSupplyRequest;
import com.mockproject.group3.dto.admin.inventory.ReceiveStockRequest;
import com.mockproject.group3.dto.admin.inventory.SupplyResponse;
import com.mockproject.group3.dto.admin.inventory.UpdateSupplyRequest;
import com.mockproject.group3.dto.common.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminSupplyService {
    
    PageResponse<SupplyResponse> getSupplies(Long facilityId, Long categoryId, String status, String search, Pageable pageable);
    SupplyResponse createSupply(CreateSupplyRequest request);
    SupplyResponse getSupplyDetail(Long supplyId);
    SupplyResponse updateSupply(Long supplyId, UpdateSupplyRequest request);
    void deleteSupply(Long supplyId);
    
    SupplyResponse receiveStock(Long supplyId, ReceiveStockRequest request);
    SupplyResponse consumeStock(Long supplyId, ConsumeStockRequest request);
    SupplyResponse adjustStock(Long supplyId, AdjustStockRequest request);
    
    List<SupplyResponse> getLowStockAlerts(Long facilityId);
}
