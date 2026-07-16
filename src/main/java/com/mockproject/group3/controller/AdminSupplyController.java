package com.mockproject.group3.controller;

import com.mockproject.group3.common.ApiResponse;
import com.mockproject.group3.dto.admin.inventory.AdjustStockRequest;
import com.mockproject.group3.dto.admin.inventory.ConsumeStockRequest;
import com.mockproject.group3.dto.admin.inventory.CreateSupplyRequest;
import com.mockproject.group3.dto.admin.inventory.ReceiveStockRequest;
import com.mockproject.group3.dto.admin.inventory.SupplyResponse;
import com.mockproject.group3.dto.admin.inventory.UpdateSupplyRequest;
import com.mockproject.group3.dto.common.PageResponse;
import com.mockproject.group3.service.AdminSupplyService;
import com.mockproject.group3.common.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(AppConstants.API_PREFIX + "/inventory/supplies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('NHA_Admin')")
public class AdminSupplyController {

    private final AdminSupplyService adminSupplyService;

    @GetMapping
    public ApiResponse<PageResponse<SupplyResponse>> getSupplies(
            @RequestParam(required = false) Long facilityId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        return ApiResponse.success(adminSupplyService.getSupplies(facilityId, categoryId, status, search, pageable));
    }

    @PostMapping
    public ApiResponse<SupplyResponse> createSupply(@Valid @RequestBody CreateSupplyRequest request) {
        return ApiResponse.success(adminSupplyService.createSupply(request));
    }

    @GetMapping("/{supplyId}")
    public ApiResponse<SupplyResponse> getSupplyDetail(@PathVariable Long supplyId) {
        return ApiResponse.success(adminSupplyService.getSupplyDetail(supplyId));
    }

    @PutMapping("/{supplyId}")
    public ApiResponse<SupplyResponse> updateSupply(@PathVariable Long supplyId, @Valid @RequestBody UpdateSupplyRequest request) {
        return ApiResponse.success(adminSupplyService.updateSupply(supplyId, request));
    }

    @DeleteMapping("/{supplyId}")
    public ApiResponse<Void> deleteSupply(@PathVariable Long supplyId) {
        adminSupplyService.deleteSupply(supplyId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{supplyId}/receive")
    public ApiResponse<SupplyResponse> receiveStock(@PathVariable Long supplyId, @Valid @RequestBody ReceiveStockRequest request) {
        return ApiResponse.success(adminSupplyService.receiveStock(supplyId, request));
    }

    @PostMapping("/{supplyId}/consume")
    public ApiResponse<SupplyResponse> consumeStock(@PathVariable Long supplyId, @Valid @RequestBody ConsumeStockRequest request) {
        return ApiResponse.success(adminSupplyService.consumeStock(supplyId, request));
    }

    @PostMapping("/{supplyId}/adjust")
    public ApiResponse<SupplyResponse> adjustStock(@PathVariable Long supplyId, @Valid @RequestBody AdjustStockRequest request) {
        return ApiResponse.success(adminSupplyService.adjustStock(supplyId, request));
    }

    @GetMapping("/alerts/low-stock")
    public ApiResponse<List<SupplyResponse>> getLowStockAlerts(@RequestParam(required = false) Long facilityId) {
        return ApiResponse.success(adminSupplyService.getLowStockAlerts(facilityId));
    }
}
