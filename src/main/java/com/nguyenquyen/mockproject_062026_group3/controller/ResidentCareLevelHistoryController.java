package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.common.SecurityUtils;
import com.nguyenquyen.mockproject_062026_group3.dto.CareLevelHistoryResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.CareLevelHistoryTransitionRequest;
import com.nguyenquyen.mockproject_062026_group3.service.ResidentCareLevelHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/residents/{resident_id}/care-level-history")
public class ResidentCareLevelHistoryController {

    @Autowired
    private ResidentCareLevelHistoryService historyService;

    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResidentCareLevelHistory(
            @PathVariable("resident_id") Long residentId) {
        securityUtils.checkRoles("Nurse", "CNA", "DON", "Doctor/Clinical_Specialist", "System_Administrator");

        List<CareLevelHistoryResponse> history = historyService.getCareLevelHistory(residentId);
        Map<String, Object> data = new HashMap<>();
        data.put("careLevelHistory", history);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> transitionResidentCareLevel(
            @PathVariable("resident_id") Long residentId,
            @RequestBody CareLevelHistoryTransitionRequest request) {
        securityUtils.checkRoles("Nurse", "DON", "Doctor/Clinical_Specialist", "System_Administrator");

        Map<String, Object> data = historyService.transitionCareLevel(residentId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(data));
    }
}
