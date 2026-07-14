package com.mockproject.group3.controller;

import com.mockproject.group3.common.ApiResponse;
import com.mockproject.group3.common.AppConstants;
import com.mockproject.group3.dto.carelevelhistory.CareLevelActiveSummaryResponse;
import com.mockproject.group3.dto.carelevelhistory.CareLevelHistoryResponse;
import com.mockproject.group3.dto.carelevelhistory.TransitionCareLevelRequest;
import com.mockproject.group3.dto.carelevelhistory.UpdateCareLevelHistoryRequest;
import com.mockproject.group3.service.CareLevelHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller handling operations related to resident care level history.
 */
@RestController
@RequestMapping(AppConstants.API_PREFIX)
@RequiredArgsConstructor
@Tag(name = "Care Level History", description = "Resident care level transitions and history")
public class CareLevelHistoryController {

    private final CareLevelHistoryService careLevelHistoryService;

    @Operation(summary = "Get resident care level history", description = "Returns full history of care level changes for a resident, sorted by start date DESC")
    @PreAuthorize("hasAnyRole('NURSE', 'CNA', 'DON', 'DOCTOR', 'SYSTEM_ADMINISTRATOR')")
    @GetMapping("/residents/{resident_id}/care-level-history")
    public ResponseEntity<ApiResponse<List<CareLevelHistoryResponse>>> getResidentCareLevelHistory(
            @PathVariable("resident_id") Long residentId) {
        return ResponseEntity.ok(ApiResponse.success(careLevelHistoryService.getResidentCareLevelHistory(residentId)));
    }

    @Operation(summary = "Transition resident care level", description = "Creates a new history record and automatically closes the current record's end date")
    @PreAuthorize("hasAnyRole('NURSE', 'DON', 'DOCTOR', 'SYSTEM_ADMINISTRATOR')")
    @PostMapping("/residents/{resident_id}/care-level-history")
    public ResponseEntity<ApiResponse<CareLevelHistoryResponse>> transitionResidentCareLevel(
            @PathVariable("resident_id") Long residentId,
            @Valid @RequestBody TransitionCareLevelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(careLevelHistoryService.transitionResidentCareLevel(residentId, request)));
    }

    @Operation(summary = "Get care level active summary", description = "Count of active residents per care level for a specific facility")
    @PreAuthorize("hasAnyRole('FACILITY_MANAGER', 'DON', 'SYSTEM_ADMINISTRATOR')")
    @GetMapping("/care-level-history/active-summary")
    public ResponseEntity<ApiResponse<List<CareLevelActiveSummaryResponse>>> getCareLevelActiveSummary(
            @RequestParam("facility_id") Long facilityId) {
        return ResponseEntity.ok(ApiResponse.success(careLevelHistoryService.getCareLevelActiveSummary(facilityId)));
    }

    @Operation(summary = "Update care level history record", description = "Only allows editing non-current records; reason is required")
    @PreAuthorize("hasAnyRole('DON', 'SYSTEM_ADMINISTRATOR')")
    @PatchMapping("/care-level-history/{id}")
    public ResponseEntity<ApiResponse<CareLevelHistoryResponse>> updateCareLevelHistory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCareLevelHistoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(careLevelHistoryService.updateCareLevelHistory(id, request)));
    }

    @Operation(summary = "Delete care level history", description = "Fully blocked via API as this is mandatory retention data")
    @DeleteMapping("/care-level-history/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCareLevelHistory(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error(405, "Mandatory retention data; deletion via API is not allowed"));
    }
}

