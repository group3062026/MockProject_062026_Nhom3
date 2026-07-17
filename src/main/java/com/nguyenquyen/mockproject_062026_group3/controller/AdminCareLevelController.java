package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.carelevel.CareLevelRateResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.carelevel.CareLevelResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.carelevel.CreateCareLevelRateRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.carelevel.UpdateCareLevelRateRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.carelevel.UpdateCareLevelRequest;
import com.nguyenquyen.mockproject_062026_group3.service.AdminCareLevelService;
import com.nguyenquyen.mockproject_062026_group3.common.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping(AppConstants.API_PREFIX + "/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('NHA_Admin')")
public class AdminCareLevelController {

    private final AdminCareLevelService adminCareLevelService;

    @GetMapping("/care-levels")
    public ApiResponse<List<CareLevelResponse>> getCareLevel() {
        return ApiResponse.success(adminCareLevelService.getCareLevel());
    }

    @PatchMapping("/care-levels/{careLevelId}")
    public ApiResponse<CareLevelResponse> updateCareLevelChange(@PathVariable Long careLevelId, @Valid @RequestBody UpdateCareLevelRequest request) {
        return ApiResponse.success(adminCareLevelService.updateCareLevelChange(careLevelId, request));
    }

    @GetMapping("/care-level-rates")
    public ApiResponse<List<CareLevelRateResponse>> getLOCRate(@RequestParam(required = false) Long careLevelId) {
        // Assume API takes careLevelId as param, else gets all or default
        return ApiResponse.success(adminCareLevelService.getLOCRate(careLevelId != null ? careLevelId : 1L));
    }

    @PostMapping("/care-level-rates")
    public ApiResponse<CareLevelRateResponse> createLOCRate(@Valid @RequestBody CreateCareLevelRateRequest request) {
        return ApiResponse.success(adminCareLevelService.createLOCRate(request));
    }

    @PutMapping("/care-level-rates/{rateId}")
    public ApiResponse<CareLevelRateResponse> updateLOCRate(@PathVariable Long rateId, @Valid @RequestBody UpdateCareLevelRateRequest request) {
        return ApiResponse.success(adminCareLevelService.updateLOCRate(rateId, request));
    }

    @DeleteMapping("/care-level-rates/{rateId}")
    public ApiResponse<Void> deleteLOCRate(@PathVariable Long rateId) {
        adminCareLevelService.deleteLOCRate(rateId);
        return ApiResponse.success(null);
    }

    @PostMapping("/care-level-rates/seed")
    public ApiResponse<List<CareLevelRateResponse>> seedSampleLOCRate() {
        return ApiResponse.success(adminCareLevelService.seedSampleLOCRate());
    }
}
