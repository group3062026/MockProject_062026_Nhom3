package com.mockproject.group3.service;

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
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminEquipmentService {
    
    List<CategoryResponse> getCategories();
    CategoryResponse createCategory(CreateCategoryRequest request);
    CategoryResponse updateCategory(Long categoryId, UpdateCategoryRequest request);
    void deleteCategory(Long categoryId);
    
    PageResponse<EquipmentResponse> getEquipment(Long facilityId, Long categoryId, String status, String search, Pageable pageable);
    EquipmentResponse createEquipment(CreateEquipmentRequest request);
    EquipmentResponse getEquipmentDetail(Long equipmentId);
    EquipmentResponse updateEquipment(Long equipmentId, UpdateEquipmentRequest request);
    void deleteEquipment(Long equipmentId);
    EquipmentResponse patchEquipmentStatus(Long equipmentId, PatchEquipmentStatusRequest request);
    EquipmentResponse assignEquipment(Long equipmentId, AssignEquipmentRequest request);
    EquipmentResponse unassignEquipment(Long equipmentId, UnassignEquipmentRequest request);
}
