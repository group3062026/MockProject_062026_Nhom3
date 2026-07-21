package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.request.BatchAdministerRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.RegenerateSchedulesRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.response.BatchAdministerResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.response.RegenerateSchedulesResponse;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.service.BatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class BatchController {

    private final BatchService batchService;

    @PostMapping("/mar/med-pass/batch-administer")
    public ResponseEntity<ApiResponse<BatchAdministerResponse>> batchAdminister(
            @RequestHeader(value = "X-Facility-ID", required = true) Long facilityId,
            @RequestBody BatchAdministerRequest request) {

        log.info("POST /mar/med-pass/batch-administer - facilityId: {}, residentId: {}, orderCount: {}",
                facilityId, request.getResidentId(),
                request.getOrderIds() != null ? request.getOrderIds().size() : 0);

        if (facilityId == null) {
            throw new AppException(ErrorCode.MAR_FACILITY_REQUIRED);
        }

        if (request.getOrderIds() == null || request.getOrderIds().isEmpty()) {
            throw new AppException(ErrorCode.MAR_BATCH_EMPTY);
        }

        if (request.getScheduleIds() == null || request.getScheduleIds().isEmpty()) {
            throw new AppException(ErrorCode.MAR_BATCH_EMPTY);
        }

        if (request.getOrderIds().size() != request.getScheduleIds().size()) {
            throw new AppException(ErrorCode.MAR_BATCH_SIZE_MISMATCH);
        }

        BatchAdministerResponse response = batchService.batchAdminister(facilityId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/medication-orders/{orderId}/regenerate-schedules")
    public ResponseEntity<ApiResponse<RegenerateSchedulesResponse>> regenerateSchedules(
            @RequestHeader(value = "X-Facility-ID", required = true) Long facilityId,
            @PathVariable Long orderId,
            @RequestBody RegenerateSchedulesRequest request) {

        log.info("POST /medication-orders/{}/regenerate-schedules - facilityId: {}, times: {}",
                orderId, facilityId, request.getNewScheduledTimes());

        if (facilityId == null) {
            throw new AppException(ErrorCode.MAR_FACILITY_REQUIRED);
        }

        if (orderId == null) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }

        if (request.getNewScheduledTimes() == null || request.getNewScheduledTimes().isEmpty()) {
            throw new AppException(ErrorCode.MAR_REGENERATE_NO_TIMES);
        }

        RegenerateSchedulesResponse response = batchService.regenerateSchedules(
                facilityId, orderId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}