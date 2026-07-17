package com.nguyenquyen.mockproject_062026_group3.service.impl;

import com.nguyenquyen.mockproject_062026_group3.dto.admin.inventory.AdjustStockRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.inventory.ConsumeStockRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.inventory.CreateSupplyRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.inventory.ReceiveStockRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.inventory.SupplyResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.inventory.UpdateSupplyRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.common.PageResponse;
import com.nguyenquyen.mockproject_062026_group3.entity.ConsumableSupply;
import com.nguyenquyen.mockproject_062026_group3.entity.Facility;
import com.nguyenquyen.mockproject_062026_group3.entity.InventoryCategory;
import com.nguyenquyen.mockproject_062026_group3.repository.ConsumableSupplyRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.FacilityRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.InventoryCategoryRepository;
import com.nguyenquyen.mockproject_062026_group3.service.AdminSupplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminSupplyServiceImpl implements AdminSupplyService {

    private final ConsumableSupplyRepository supplyRepository;
    private final InventoryCategoryRepository categoryRepository;
    private final FacilityRepository facilityRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SupplyResponse> getSupplies(Long facilityId, Long categoryId, String status, String search, Pageable pageable) {
        Page<ConsumableSupply> page = supplyRepository.searchSupplies(facilityId, categoryId, status, search, pageable);
        return PageResponse.of(page.map(this::mapToResponse));
    }

    @Override
    @Transactional
    public SupplyResponse createSupply(CreateSupplyRequest request) {
        Facility facility = facilityRepository.findById(request.getFacilityId())
                .orElseThrow(() -> new RuntimeException("Facility not found"));
        
        InventoryCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        ConsumableSupply supply = ConsumableSupply.builder()
                .itemName(request.getItemName())
                .category(category)
                .facility(facility)
                .stockOnHand(request.getInitialStock() != null ? request.getInitialStock() : 0)
                .reorderThreshold(request.getReorderThreshold())
                .unitCost(request.getUnitCost())
                .privatePayRate(request.getPrivatePayRate())
                .status("ACTIVE")
                .isDeleted(false)
                .build();

        supply = supplyRepository.save(supply);
        return mapToResponse(supply);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplyResponse getSupplyDetail(Long supplyId) {
        ConsumableSupply supply = supplyRepository.findById(supplyId)
                .orElseThrow(() -> new RuntimeException("Supply not found"));
        return mapToResponse(supply);
    }

    @Override
    @Transactional
    public SupplyResponse updateSupply(Long supplyId, UpdateSupplyRequest request) {
        ConsumableSupply supply = supplyRepository.findById(supplyId)
                .orElseThrow(() -> new RuntimeException("Supply not found"));

        if (request.getItemName() != null) supply.setItemName(request.getItemName());
        if (request.getReorderThreshold() != null) supply.setReorderThreshold(request.getReorderThreshold());
        if (request.getUnitCost() != null) supply.setUnitCost(request.getUnitCost());
        if (request.getPrivatePayRate() != null) supply.setPrivatePayRate(request.getPrivatePayRate());

        if (request.getCategoryId() != null) {
            InventoryCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            supply.setCategory(category);
        }

        supply = supplyRepository.save(supply);
        return mapToResponse(supply);
    }

    @Override
    @Transactional
    public void deleteSupply(Long supplyId) {
        ConsumableSupply supply = supplyRepository.findById(supplyId)
                .orElseThrow(() -> new RuntimeException("Supply not found"));
        supply.setIsDeleted(true);
        supplyRepository.save(supply);
    }

    @Override
    @Transactional
    public SupplyResponse receiveStock(Long supplyId, ReceiveStockRequest request) {
        ConsumableSupply supply = supplyRepository.findById(supplyId)
                .orElseThrow(() -> new RuntimeException("Supply not found"));
        
        supply.setStockOnHand(supply.getStockOnHand() + request.getQuantity());
        // Normally log transaction here
        
        supply = supplyRepository.save(supply);
        return mapToResponse(supply);
    }

    @Override
    @Transactional
    public SupplyResponse consumeStock(Long supplyId, ConsumeStockRequest request) {
        ConsumableSupply supply = supplyRepository.findById(supplyId)
                .orElseThrow(() -> new RuntimeException("Supply not found"));

        if (supply.getStockOnHand() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock");
        }

        supply.setStockOnHand(supply.getStockOnHand() - request.getQuantity());
        // Normally log transaction / billing link here
        
        supply = supplyRepository.save(supply);
        return mapToResponse(supply);
    }

    @Override
    @Transactional
    public SupplyResponse adjustStock(Long supplyId, AdjustStockRequest request) {
        ConsumableSupply supply = supplyRepository.findById(supplyId)
                .orElseThrow(() -> new RuntimeException("Supply not found"));

        supply.setStockOnHand(request.getNewStockOnHand());
        // Normally log cycle count transaction here
        
        supply = supplyRepository.save(supply);
        return mapToResponse(supply);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplyResponse> getLowStockAlerts(Long facilityId) {
        // Simple mock for low stock: we don't have a specific low-stock flag in entity, but we have threshold.
        // For MVP, we can fetch all and filter, or just use a query if it existed.
        // Let's filter in memory since it's a mock.
        List<ConsumableSupply> supplies = supplyRepository.findByFacilityIdAndStatusAndIsDeletedFalse(facilityId, "ACTIVE");
        return supplies.stream()
                .filter(s -> s.getStockOnHand() <= s.getReorderThreshold())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private SupplyResponse mapToResponse(ConsumableSupply supply) {
        return SupplyResponse.builder()
                .id(supply.getId())
                .itemName(supply.getItemName())
                .stockOnHand(supply.getStockOnHand())
                .total(supply.getStockOnHand()) // Equivalent mapping for API doc 'total'
                .reorderThreshold(supply.getReorderThreshold())
                .unitCost(supply.getUnitCost())
                .privatePayRate(supply.getPrivatePayRate())
                .status(supply.getStatus())
                .build();
    }
}
