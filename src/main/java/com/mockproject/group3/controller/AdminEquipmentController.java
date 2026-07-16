package com.mockproject.group3.controller;

import com.mockproject.group3.common.ApiResponse;
import com.mockproject.group3.dto.admin.inventory.AssignEquipmentRequest;
import com.mockproject.group3.dto.admin.inventory.CategoryResponse;
import com.mockproject.group3.dto.admin.inventory.CreateCategoryRequest;
import com.mockproject.group3.dto.admin.inventory.CreateEquipmentRequest;
import com.mockproject.group3.dto.admin.inventory.EquipmentResponse;
import com.mockproject.group3.dto.admin.inventory.PatchEquipmentStatusRequest;
import com.mockproject.group3.dto.admin.inventory.UnassignEquipmentRequest;
import com.mockproject.group3.dto.admin.inventory.UpdateCategoryRequest;
import com.mockproject.group3.dto.admin.inventory.UpdateEquipmentRequest;
import com.mockproject.group3.dto.common.PageResponse;
import com.mockproject.group3.service.AdminEquipmentService;
import com.mockproject.group3.common.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(AppConstants.API_PREFIX + "/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('NHA_Admin')")
public class AdminEquipmentController {

    private final AdminEquipmentService adminEquipmentService;

    // Categories (AD-32 to AD-35 optional but implemented)
    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> getCategories() {
        return ApiResponse.success(adminEquipmentService.getCategories());
    }

    @PostMapping("/categories")
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return ApiResponse.success(adminEquipmentService.createCategory(request));
    }

    @PutMapping("/categories/{categoryId}")
    public ApiResponse<CategoryResponse> updateCategory(@PathVariable Long categoryId, @Valid @RequestBody UpdateCategoryRequest request) {
        return ApiResponse.success(adminEquipmentService.updateCategory(categoryId, request));
    }

    @DeleteMapping("/categories/{categoryId}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long categoryId) {
        adminEquipmentService.deleteCategory(categoryId);
        return ApiResponse.success(null);
    }

    // Equipment
    @GetMapping("/equipment")
    public ApiResponse<PageResponse<EquipmentResponse>> getEquipment(
            @RequestParam(required = false) Long facilityId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        return ApiResponse.success(adminEquipmentService.getEquipment(facilityId, categoryId, status, search, pageable));
    }

    @PostMapping("/equipment")
    public ApiResponse<EquipmentResponse> createEquipment(@Valid @RequestBody CreateEquipmentRequest request) {
        return ApiResponse.success(adminEquipmentService.createEquipment(request));
    }

    @GetMapping("/equipment/{equipmentId}")
    public ApiResponse<EquipmentResponse> getEquipmentDetail(@PathVariable Long equipmentId) {
        return ApiResponse.success(adminEquipmentService.getEquipmentDetail(equipmentId));
    }

    @PutMapping("/equipment/{equipmentId}")
    public ApiResponse<EquipmentResponse> updateEquipment(@PathVariable Long equipmentId, @Valid @RequestBody UpdateEquipmentRequest request) {
        return ApiResponse.success(adminEquipmentService.updateEquipment(equipmentId, request));
    }

    @DeleteMapping("/equipment/{equipmentId}")
    public ApiResponse<Void> deleteEquipment(@PathVariable Long equipmentId) {
        adminEquipmentService.deleteEquipment(equipmentId);
        return ApiResponse.success(null);
    }

    @PatchMapping("/equipment/{equipmentId}/status")
    public ApiResponse<EquipmentResponse> patchEquipmentStatus(@PathVariable Long equipmentId, @Valid @RequestBody PatchEquipmentStatusRequest request) {
        return ApiResponse.success(adminEquipmentService.patchEquipmentStatus(equipmentId, request));
    }

    @PostMapping("/equipment/{equipmentId}/assign")
    public ApiResponse<EquipmentResponse> assignEquipment(@PathVariable Long equipmentId, @Valid @RequestBody AssignEquipmentRequest request) {
        return ApiResponse.success(adminEquipmentService.assignEquipment(equipmentId, request));
    }

    @PostMapping("/equipment/{equipmentId}/unassign")
    public ApiResponse<EquipmentResponse> unassignEquipment(@PathVariable Long equipmentId, @Valid @RequestBody UnassignEquipmentRequest request) {
        return ApiResponse.success(adminEquipmentService.unassignEquipment(equipmentId, request));
    }
}
