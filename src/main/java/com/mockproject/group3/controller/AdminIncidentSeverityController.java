package com.mockproject.group3.controller;

import com.mockproject.group3.common.ApiResponse;
import com.mockproject.group3.dto.admin.incident.CreateIncidentSeverityRequest;
import com.mockproject.group3.dto.admin.incident.CreateSlaConfigRequest;
import com.mockproject.group3.dto.admin.incident.IncidentSeverityResponse;
import com.mockproject.group3.dto.admin.incident.SlaConfigResponse;
import com.mockproject.group3.dto.admin.incident.UpdateIncidentSeverityRequest;
import com.mockproject.group3.dto.admin.incident.UpdateSlaConfigRequest;
import com.mockproject.group3.service.AdminIncidentSeverityService;
import com.mockproject.group3.common.AppConstants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(AppConstants.API_PREFIX + "/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('NHA_Admin')")
public class AdminIncidentSeverityController {

    private final AdminIncidentSeverityService adminIncidentSeverityService;

    @GetMapping("/incident-severity-levels")
    public ApiResponse<List<IncidentSeverityResponse>> getIncidentSeverityLevels() {
        return ApiResponse.success(adminIncidentSeverityService.getIncidentSeverityLevels());
    }

    @PostMapping("/incident-severity-levels")
    public ApiResponse<IncidentSeverityResponse> createSeverityLevel(@Valid @RequestBody CreateIncidentSeverityRequest request) {
        return ApiResponse.success(adminIncidentSeverityService.createSeverityLevel(request));
    }

    @PutMapping("/incident-severity-levels/{severityId}")
    public ApiResponse<IncidentSeverityResponse> updateSeverityLevel(@PathVariable Long severityId, @Valid @RequestBody UpdateIncidentSeverityRequest request) {
        return ApiResponse.success(adminIncidentSeverityService.updateSeverityLevel(severityId, request));
    }

    @GetMapping("/sla-configs")
    public ApiResponse<List<SlaConfigResponse>> getSLAConfig() {
        return ApiResponse.success(adminIncidentSeverityService.getSLAConfig());
    }

    @PostMapping("/sla-configs")
    public ApiResponse<SlaConfigResponse> createSLA(@Valid @RequestBody CreateSlaConfigRequest request) {
        return ApiResponse.success(adminIncidentSeverityService.createSLA(request));
    }

    @PutMapping("/sla-configs/{slaConfigId}")
    public ApiResponse<SlaConfigResponse> updateSLA(@PathVariable Long slaConfigId, @Valid @RequestBody UpdateSlaConfigRequest request) {
        return ApiResponse.success(adminIncidentSeverityService.updateSLA(slaConfigId, request));
    }
}
