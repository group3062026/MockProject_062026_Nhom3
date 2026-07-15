package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.common.SecurityUtils;
import com.nguyenquyen.mockproject_062026_group3.dto.MedicationDtos;
import com.nguyenquyen.mockproject_062026_group3.service.MedicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/medication-orders")
public class MedicationOrderController {

    @Autowired
    private MedicationService medicationService;

    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMedicationOrders(
            @RequestParam(value = "resident_id", required = false) Long residentId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search) {
        securityUtils.checkRoles("Nurse", "DON", "System Administrator", "Doctor/Clinical Specialist");
        Map<String, Object> data = new HashMap<>();
        data.put("medicationOrders", medicationService.getMedicationOrders(residentId, status, search));
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMedicationOrderById(@PathVariable("id") Long id) {
        securityUtils.checkRoles("Nurse", "DON", "System Administrator", "Doctor/Clinical Specialist");
        Map<String, Object> data = new HashMap<>();
        data.put("medicationOrder", medicationService.getMedicationOrderById(id));
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createMedicationOrder(
            @RequestBody MedicationDtos.MedicationOrderCreateRequest request) {
        securityUtils.checkRoles("Nurse", "DON", "Doctor/Clinical Specialist");
        Map<String, Object> data = new HashMap<>();
        data.put("medicationOrder", medicationService.createMedicationOrder(request));
        ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .statusCode(201)
                .message("Medication order created successfully.")
                .data(data)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateMedicationOrder(
            @PathVariable("id") Long id,
            @RequestBody MedicationDtos.MedicationOrderUpdateRequest request) {
        securityUtils.checkRoles("Nurse", "DON", "Doctor/Clinical Specialist");
        Map<String, Object> data = new HashMap<>();
        data.put("medicationOrder", medicationService.updateMedicationOrder(id, request));
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .statusCode(200)
                .message("Medication order updated successfully.")
                .data(data)
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteMedicationOrder(@PathVariable("id") Long id) {
        securityUtils.checkRoles("DON", "System Administrator");
        Map<String, Object> data = new HashMap<>();
        data.put("medicationOrder", medicationService.deleteMedicationOrder(id));
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .statusCode(200)
                .message("Medication order deleted successfully.")
                .data(data)
                .build());
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyMedicationBeforeAdministration(
            @PathVariable("id") Long id,
            @RequestBody MedicationDtos.VerifyMedicationRequest request) {
        securityUtils.checkRoles("Nurse");
        Map<String, Object> data = new HashMap<>();
        data.put("verification", medicationService.verifyMedicationBeforeAdministration(id, request));
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .statusCode(200)
                .message("Medication verification completed.")
                .data(data)
                .build());
    }
}
