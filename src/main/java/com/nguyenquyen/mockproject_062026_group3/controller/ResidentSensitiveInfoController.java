package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.common.SecurityUtils;
import com.nguyenquyen.mockproject_062026_group3.dto.ResidentSensitiveInfoCreateRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.ResidentSensitiveInfoResponse;
import com.nguyenquyen.mockproject_062026_group3.service.ResidentSensitiveInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/residents/{resident_id}/sensitive-info")
public class ResidentSensitiveInfoController {

    @Autowired
    private ResidentSensitiveInfoService sensitiveInfoService;

    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<ResidentSensitiveInfoResponse>> getResidentSensitiveInfo(
            @PathVariable("resident_id") Long residentId,
            @RequestParam(value = "reveal", required = false, defaultValue = "false") Boolean reveal,
            @RequestHeader(value = "X-Access-Reason", required = false) String accessReason) {
        
        securityUtils.checkRoles("System_Administrator", "Accountant/Billing_Staff", "DON");
        
        ResidentSensitiveInfoResponse response = sensitiveInfoService.getSensitiveInfo(residentId, reveal, accessReason);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createResidentSensitiveInfo(
            @PathVariable("resident_id") Long residentId,
            @RequestBody ResidentSensitiveInfoCreateRequest request) {
        
        securityUtils.checkRoles("System_Administrator", "Admission_Staff");
        
        Map<String, Object> response = sensitiveInfoService.createSensitiveInfo(residentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateResidentSensitiveInfo(
            @PathVariable("resident_id") Long residentId,
            @RequestBody ResidentSensitiveInfoCreateRequest request) {
        
        securityUtils.checkRoles("System_Administrator", "Accountant/Billing_Staff");
        
        Map<String, Object> response = sensitiveInfoService.updateSensitiveInfo(residentId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteResidentSensitiveInfo(
            @PathVariable("resident_id") Long residentId) {
        
        securityUtils.checkRoles("System_Administrator");
        
        boolean deleted = sensitiveInfoService.deleteSensitiveInfo(residentId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("deleted", deleted);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
