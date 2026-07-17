package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.common.SecurityUtils;
import com.nguyenquyen.mockproject_062026_group3.dto.CareLevelResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.CareLevelUpdateRequest;
import com.nguyenquyen.mockproject_062026_group3.service.CareLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/care-levels")
public class CareLevelController {

    @Autowired
    private CareLevelService careLevelService;

    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCareLevels(
            @RequestParam(value = "include_deleted", required = false, defaultValue = "false") Boolean includeDeleted) {
        securityUtils.checkRoles(); // All authenticated
        List<CareLevelResponse> careLevels = careLevelService.getCareLevels(includeDeleted);
        Map<String, Object> data = new HashMap<>();
        data.put("careLevels", careLevels);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCareLevelById(@PathVariable("id") Long id) {
        securityUtils.checkRoles(); // All authenticated
        CareLevelResponse careLevel = careLevelService.getCareLevelById(id);
        Map<String, Object> data = new HashMap<>();
        data.put("careLevel", careLevel);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateCareLevelName(
            @PathVariable("id") Long id,
            @RequestBody CareLevelUpdateRequest request) {
        securityUtils.checkRoles("System_Administrator", "DON");
        CareLevelResponse careLevel = careLevelService.updateCareLevelName(id, request);
        Map<String, Object> data = new HashMap<>();
        data.put("careLevel", careLevel);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteCareLevel(@PathVariable("id") Long id) {
        securityUtils.checkRoles("System_Administrator");
        boolean isDeleted = careLevelService.deleteCareLevel(id);
        Map<String, Object> data = new HashMap<>();
        data.put("isDeleted", isDeleted);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
