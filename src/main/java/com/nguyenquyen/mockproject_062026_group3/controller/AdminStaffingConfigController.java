package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.staffing.StaffingConfigResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.staffing.UpdateStaffingConfigRequest;
import com.nguyenquyen.mockproject_062026_group3.service.AdminStaffingConfigService;
import com.nguyenquyen.mockproject_062026_group3.common.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstants.API_PREFIX + "/admin/staffing-ratio-config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('NHA_Admin')")
public class AdminStaffingConfigController {

    private final AdminStaffingConfigService adminStaffingConfigService;

    @GetMapping
    public ApiResponse<StaffingConfigResponse> getStaffingRatio() {
        return ApiResponse.success(adminStaffingConfigService.getStaffingRatio());
    }

    @PutMapping
    public ApiResponse<StaffingConfigResponse> updateStaffingRatio(@Valid @RequestBody UpdateStaffingConfigRequest request) {
        return ApiResponse.success(adminStaffingConfigService.updateStaffingRatio(request));
    }
}
