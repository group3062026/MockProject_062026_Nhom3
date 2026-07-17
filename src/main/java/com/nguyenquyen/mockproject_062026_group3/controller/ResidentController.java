package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.common.SecurityUtils;
import com.nguyenquyen.mockproject_062026_group3.dto.*;
import com.nguyenquyen.mockproject_062026_group3.service.ResidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/residents")
public class ResidentController {

    @Autowired
    private ResidentService residentService;

    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResidents(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "bed_id", required = false) Long bedId,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "page_size", required = false, defaultValue = "10") int pageSize) {
        
        securityUtils.checkRoles("Nurse", "CNA", "DON", "Admission_Staff", "Facility_Manager", "System_Administrator");
        
        Page<ResidentResponse> residentPage = residentService.getResidents(status, bedId, search, page, pageSize);

        Map<String, Object> meta = new HashMap<>();
        meta.put("total", residentPage.getTotalElements());
        meta.put("page", page);
        meta.put("pageSize", pageSize);

        Map<String, Object> data = new HashMap<>();
        data.put("residents", residentPage.getContent());
        data.put("meta", meta);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResidentById(@PathVariable("id") Long id) {
        securityUtils.checkRoles("Nurse", "CNA", "DON", "Admission_Staff", "Facility_Manager", "System_Administrator");
        
        ResidentResponse resident = residentService.getResidentById(id);
        
        Map<String, Object> data = new HashMap<>();
        data.put("resident", resident);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createResident(@RequestBody ResidentCreateRequest request) {
        securityUtils.checkRoles("Admission_Staff", "System_Administrator");
        
        ResidentResponse resident = residentService.createResident(request);
        
        Map<String, Object> data = new HashMap<>();
        data.put("resident", resident);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(data));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateResident(
            @PathVariable("id") Long id,
            @RequestBody ResidentUpdateRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        securityUtils.checkRoles("Nurse", "CNA", "Admission_Staff", "DON", "System_Administrator");
        
        ResidentResponse resident = residentService.updateResident(id, request, userRole);
        
        Map<String, Object> data = new HashMap<>();
        data.put("resident", resident);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateResidentStatus(
            @PathVariable("id") Long id,
            @RequestBody ResidentStatusUpdateRequest request) {
        
        securityUtils.checkRoles("DON", "Admission_Staff", "System_Administrator");
        
        ResidentResponse resident = residentService.updateResidentStatus(id, request);
        
        Map<String, Object> data = new HashMap<>();
        data.put("resident", resident);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PatchMapping("/{id}/assign-bed")
    public ResponseEntity<ApiResponse<Map<String, Object>>> assignResidentBed(
            @PathVariable("id") Long id,
            @RequestBody ResidentBedAssignRequest request) {
        
        securityUtils.checkRoles("Nurse", "Admission_Staff", "System_Administrator");
        
        ResidentResponse resident = residentService.assignResidentBed(id, request);
        
        Map<String, Object> data = new HashMap<>();
        data.put("resident", resident);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PatchMapping("/{id}/lock-chart")
    public ResponseEntity<ApiResponse<Map<String, Object>>> lockResidentChart(
            @PathVariable("id") Long id,
            @RequestBody ResidentChartLockRequest request) {
        
        securityUtils.checkRoles("DON", "System_Administrator");
        
        ResidentResponse resident = residentService.lockResidentChart(id, request.getReason());
        
        Map<String, Object> data = new HashMap<>();
        data.put("resident", resident);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PatchMapping("/{id}/unlock-chart")
    public ResponseEntity<ApiResponse<Map<String, Object>>> unlockResidentChart(
            @PathVariable("id") Long id,
            @RequestBody ResidentChartLockRequest request) {
        
        securityUtils.checkRoles("DON", "System_Administrator");
        
        ResidentResponse resident = residentService.unlockResidentChart(id, request.getReason());
        
        Map<String, Object> data = new HashMap<>();
        data.put("resident", resident);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteResident(@PathVariable("id") Long id) {
        // PHI retention - deletion fully blocked
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error(HttpStatus.METHOD_NOT_ALLOWED.value(), "Method Not Allowed"));
    }




  @GetMapping("/{residentId}/loc-status")
  public ResponseEntity<ApiResponse<LocStatusResponse>> getLocStatus(
      @PathVariable Long residentId
  ){

    return ResponseEntity.ok(
        ApiResponse.success(
            residentService.getLocStatus(residentId)
        )
    );

  }
}
