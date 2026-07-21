package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.common.SecurityUtils;
import com.nguyenquyen.mockproject_062026_group3.dto.CareLevelActiveSummaryResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.CareLevelHistoryResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.CareLevelHistoryUpdateRequest;
import com.nguyenquyen.mockproject_062026_group3.service.ResidentCareLevelHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/care-level-history")
public class CareLevelHistoryController {

    @Autowired
    private ResidentCareLevelHistoryService historyService;

    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping("/active-summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCareLevelActiveSummary(
            @RequestParam("facility_id") Long facilityId) {
        securityUtils.checkRoles("Facility_Manager", "DON", "System_Administrator");

        List<CareLevelActiveSummaryResponse> summary = historyService.getActiveSummary(facilityId);
        Map<String, Object> data = new HashMap<>();
        data.put("summary", summary);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateCareLevelHistory(
            @PathVariable("id") Long id,
            @RequestBody CareLevelHistoryUpdateRequest request) {
        securityUtils.checkRoles("DON", "System_Administrator");

        CareLevelHistoryResponse history = historyService.updateCareLevelHistory(id, request);
        Map<String, Object> data = new HashMap<>();
        data.put("careLevelHistory", history);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteCareLevelHistory(@PathVariable("id") Long id) {
        // Mandatory retention data — deletion via API is fully blocked
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error(HttpStatus.METHOD_NOT_ALLOWED.value(), "Method Not Allowed"));
    }
}
