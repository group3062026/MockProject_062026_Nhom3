package com.mockproject.group3.controller;

import com.mockproject.group3.common.ApiResponse;
import com.mockproject.group3.dto.admin.facility.FacilityResponse;
import com.mockproject.group3.dto.admin.facility.UpdateFacilityRequest;
import com.mockproject.group3.service.AdminFacilityService;
import com.mockproject.group3.common.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstants.API_PREFIX + "/admin/facility-settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('NHA_Admin')")
public class AdminFacilityController {

    private final AdminFacilityService adminFacilityService;

    @GetMapping
    public ApiResponse<FacilityResponse> getFacilityInfo() {
        return ApiResponse.success(adminFacilityService.getFacilityInfo());
    }

    @PutMapping
    public ApiResponse<FacilityResponse> updateFacilityInfo(@Valid @RequestBody UpdateFacilityRequest request) {
        return ApiResponse.success(adminFacilityService.updateFacilityInfo(request));
    }
}
