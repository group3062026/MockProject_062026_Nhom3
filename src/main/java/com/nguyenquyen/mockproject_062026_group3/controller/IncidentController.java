package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.common.SecurityUtils;
import com.nguyenquyen.mockproject_062026_group3.dto.request.CreateTimelineRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.UnlockIncidentRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.response.IncidentDetailDTO;
import com.nguyenquyen.mockproject_062026_group3.service.IncidentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// SC_043 - M7-US-03b (incident-detail-unlocked: DON actions)
// SC_044 - M7-US-03  (incident-detail-nurse: read-only view)
@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
@Slf4j
public class IncidentController {

    private final IncidentService incidentService;
    private final SecurityUtils securityUtils;

    /**
     * SC_044 - Nurse/CNA/DON/Admin xem chi tiết sự cố
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncidentDetailDTO>> getIncidentDetail(
            @PathVariable Long id) {

        securityUtils.checkRoles("NURSE", "CNA", "DON", "ADMIN");

        log.info("Getting incident detail for id: {}", id);
        return ResponseEntity.ok(
                ApiResponse.success(incidentService.getIncidentDetail(id))
        );
    }

    /**
     * SC_043 - DON unlock chart và resolve sự cố
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> unlockIncident(
            @PathVariable Long id,
            @RequestBody UnlockIncidentRequest request) {

        securityUtils.checkRoles("DON", "ADMIN");

        log.info("Unlocking incident {} by DON", id);
        incidentService.unlockIncident(id, request);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .statusCode(200)
                        .message("Incident resolved and chart unlocked successfully")
                        .build()
        );
    }

    /**
     * SC_043 - Nurse/CNA/DON thêm timeline entry
     */
    @PostMapping("/{id}/timelines")
    public ResponseEntity<ApiResponse<IncidentDetailDTO.TimelineDTO>> addTimeline(
            @PathVariable Long id,
            @RequestBody CreateTimelineRequest request) {

        securityUtils.checkRoles("NURSE", "CNA", "DON");

        log.info("Adding timeline to incident {}", id);
        return ResponseEntity.status(201).body(
                ApiResponse.created(incidentService.addTimeline(id, request))
        );
    }
}
