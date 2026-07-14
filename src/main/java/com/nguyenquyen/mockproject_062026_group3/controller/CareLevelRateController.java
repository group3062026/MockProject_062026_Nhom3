package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.common.SecurityUtils;
import com.nguyenquyen.mockproject_062026_group3.dto.CareLevelRateCreateRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.CareLevelRateCreateResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.CareLevelRateResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.CareLevelRateUpdateRequest;
import com.nguyenquyen.mockproject_062026_group3.service.CareLevelRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/care-level-rates")
public class CareLevelRateController {

    @Autowired
    private CareLevelRateService careLevelRateService;

    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCareLevelRates(
            @RequestParam(value = "facility_id", required = false) Long facilityId,
            @RequestParam(value = "care_level_id", required = false) Long careLevelId,
            @RequestParam(value = "active_only", required = false, defaultValue = "false") Boolean activeOnly) {
        securityUtils.checkRoles("Facility_Manager", "Accountant/Billing_Staff", "System_Administrator");
        List<CareLevelRateResponse> rates = careLevelRateService.getCareLevelRates(facilityId, careLevelId, activeOnly);
        
        Map<String, Object> meta = new HashMap<>();
        meta.put("total", rates.size());

        Map<String, Object> data = new HashMap<>();
        data.put("rates", rates);
        data.put("meta", meta);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CareLevelRateCreateResponse>> createCareLevelRate(
            @RequestBody CareLevelRateCreateRequest request) {
        securityUtils.checkRoles("Accountant/Billing_Staff", "System_Administrator");
        CareLevelRateCreateResponse createResponse = careLevelRateService.createCareLevelRate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createResponse));
    }

    @GetMapping("/current")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentCareLevelRates(
            @RequestParam("facility_id") Long facilityId) {
        securityUtils.checkRoles("Facility_Manager", "Accountant/Billing_Staff", "Admission_Staff");
        List<CareLevelRateResponse> rates = careLevelRateService.getCurrentCareLevelRates(facilityId);
        
        Map<String, Object> data = new HashMap<>();
        data.put("rates", rates);
        
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateCareLevelRate(
            @PathVariable("id") Long id,
            @RequestBody CareLevelRateUpdateRequest request) {
        securityUtils.checkRoles("Accountant/Billing_Staff", "System_Administrator");
        CareLevelRateResponse rate = careLevelRateService.updateCareLevelRate(id, request);
        
        Map<String, Object> data = new HashMap<>();
        data.put("rate", rate);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteCareLevelRate(@PathVariable("id") Long id) {
        securityUtils.checkRoles("System_Administrator");
        boolean deleted = careLevelRateService.deleteCareLevelRate(id);
        
        Map<String, Object> data = new HashMap<>();
        data.put("deleted", deleted);

        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
