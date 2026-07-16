package com.mockproject.group3.service.impl;

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
import com.mockproject.group3.entity.DurableMedicalEquipment;
import com.mockproject.group3.entity.Facility;
import com.mockproject.group3.entity.InventoryCategory;
import com.mockproject.group3.entity.Resident;
import com.mockproject.group3.entity.User;
import com.mockproject.group3.repository.DurableMedicalEquipmentRepository;
import com.mockproject.group3.repository.FacilityRepository;
import com.mockproject.group3.repository.InventoryCategoryRepository;
import com.mockproject.group3.repository.ResidentRepository;
import com.mockproject.group3.repository.UserRepository;
import com.mockproject.group3.service.AdminEquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminEquipmentServiceImpl implements AdminEquipmentService {

    private final DurableMedicalEquipmentRepository equipmentRepository;
    private final InventoryCategoryRepository categoryRepository;
    private final FacilityRepository facilityRepository;
    private final ResidentRepository residentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll().stream()
                .map(c -> CategoryResponse.builder()
                        .id(c.getId())
                        .categoryName(c.getCategoryName())
                        .description(c.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        InventoryCategory category = InventoryCategory.builder()
                .categoryName(request.getCategoryName())
                .description(request.getDescription())
                .build();
        category = categoryRepository.save(category);
        return CategoryResponse.builder()
                .id(category.getId())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .build();
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, UpdateCategoryRequest request) {
        InventoryCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        if (request.getCategoryName() != null) category.setCategoryName(request.getCategoryName());
        if (request.getDescription() != null) category.setDescription(request.getDescription());
        
        category = categoryRepository.save(category);
        return CategoryResponse.builder()
                .id(category.getId())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .build();
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        // Simple delete, not handling constraints for MVP
        categoryRepository.deleteById(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EquipmentResponse> getEquipment(Long facilityId, Long categoryId, String status, String search, Pageable pageable) {
        Page<DurableMedicalEquipment> page = equipmentRepository.searchEquipment(facilityId, categoryId, status, search, pageable);
        return PageResponse.of(page.map(this::mapToResponse));
    }

    @Override
    @Transactional
    public EquipmentResponse createEquipment(CreateEquipmentRequest request) {
        if (equipmentRepository.existsByAssetTag(request.getAssetTag())) {
            throw new RuntimeException("Asset tag already exists");
        }

        Facility facility = facilityRepository.findById(request.getFacilityId())
                .orElseThrow(() -> new RuntimeException("Facility not found"));
        
        InventoryCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        DurableMedicalEquipment eq = DurableMedicalEquipment.builder()
                .itemName(request.getItemName())
                .category(category)
                .assetTag(request.getAssetTag())
                .facility(facility)
                .unitValue(request.getUnitValue())
                .status("AVAILABLE")
                .isDeleted(false)
                .build();

        eq = equipmentRepository.save(eq);
        return mapToResponse(eq);
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentResponse getEquipmentDetail(Long equipmentId) {
        DurableMedicalEquipment eq = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));
        return mapToResponse(eq);
    }

    @Override
    @Transactional
    public EquipmentResponse updateEquipment(Long equipmentId, UpdateEquipmentRequest request) {
        DurableMedicalEquipment eq = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        if (request.getItemName() != null) eq.setItemName(request.getItemName());
        if (request.getUnitValue() != null) eq.setUnitValue(request.getUnitValue());
        if (request.getCategoryId() != null) {
            InventoryCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            eq.setCategory(category);
        }

        eq = equipmentRepository.save(eq);
        return mapToResponse(eq);
    }

    @Override
    @Transactional
    public void deleteEquipment(Long equipmentId) {
        DurableMedicalEquipment eq = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));
        eq.setIsDeleted(true);
        equipmentRepository.save(eq);
    }

    @Override
    @Transactional
    public EquipmentResponse patchEquipmentStatus(Long equipmentId, PatchEquipmentStatusRequest request) {
        DurableMedicalEquipment eq = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        if ("IN_USE".equals(eq.getStatus()) && !request.getStatus().equals("IN_USE")) {
            // Business rule: need to unassign first, but simple implementation here
            eq.setAssignedToResident(null);
            eq.setAssignedToUser(null);
        }

        eq.setStatus(request.getStatus());
        eq = equipmentRepository.save(eq);
        return mapToResponse(eq);
    }

    @Override
    @Transactional
    public EquipmentResponse assignEquipment(Long equipmentId, AssignEquipmentRequest request) {
        DurableMedicalEquipment eq = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        if (!"AVAILABLE".equals(eq.getStatus())) {
            throw new RuntimeException("Equipment is not available for assignment");
        }

        if (request.getAssignedToResident() != null) {
            Resident resident = residentRepository.findById(request.getAssignedToResident())
                    .orElseThrow(() -> new RuntimeException("Resident not found"));
            eq.setAssignedToResident(resident);
        } else if (request.getAssignedToUser() != null) {
            User user = userRepository.findById(request.getAssignedToUser())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            eq.setAssignedToUser(user);
        } else {
            throw new RuntimeException("Must provide resident or user ID");
        }

        eq.setStatus("IN_USE");
        eq = equipmentRepository.save(eq);
        return mapToResponse(eq);
    }

    @Override
    @Transactional
    public EquipmentResponse unassignEquipment(Long equipmentId, UnassignEquipmentRequest request) {
        DurableMedicalEquipment eq = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        if (!"IN_USE".equals(eq.getStatus())) {
            throw new RuntimeException("Equipment is not in use");
        }

        eq.setAssignedToResident(null);
        eq.setAssignedToUser(null);
        eq.setStatus("AVAILABLE");
        
        eq = equipmentRepository.save(eq);
        return mapToResponse(eq);
    }

    private EquipmentResponse mapToResponse(DurableMedicalEquipment eq) {
        return EquipmentResponse.builder()
                .id(eq.getId())
                .itemName(eq.getItemName())
                .assetTag(eq.getAssetTag())
                .status(eq.getStatus())
                .facilityId(eq.getFacility() != null ? eq.getFacility().getId() : null)
                .categoryId(eq.getCategory() != null ? eq.getCategory().getId() : null)
                .unitValue(eq.getUnitValue())
                .assignedToResident(eq.getAssignedToResident() != null ? eq.getAssignedToResident().getId() : null)
                .assignedToUser(eq.getAssignedToUser() != null ? eq.getAssignedToUser().getId() : null)
                .build();
    }
}
