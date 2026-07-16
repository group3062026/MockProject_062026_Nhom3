package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.request.MarExportRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.MedicationAuditRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.PhiAccessLogRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.response.MarExportResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.response.MedicationAuditResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.response.PhiAccessLogResponse;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/audit/medication")
    public ResponseEntity<ApiResponse<MedicationAuditResponse>> getMedicationAuditLog(
            @RequestHeader(value = "X-Facility-ID", required = true) Long facilityId,
            @RequestParam(required = false) Long residentId,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String action,
            @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "50") Integer limit) {

        log.info("GET /audit/medication - facilityId: {}, residentId: {}, orderId: {}, action: {}, startDate: {}, endDate: {}",
                facilityId, residentId, orderId, action, startDate, endDate);

        if (facilityId == null) {
            throw new AppException(ErrorCode.MAR_FACILITY_REQUIRED);
        }

        if (startDate.isAfter(endDate)) {
            throw new AppException(ErrorCode.MAR_AUDIT_INVALID_DATE_RANGE);
        }

        if (action != null && !action.isEmpty()) {
            String validActions = "INSERT,UPDATE,DELETE";
            if (!validActions.contains(action.toUpperCase())) {
                throw new AppException(ErrorCode.MAR_AUDIT_ACTION_INVALID);
            }
        }

        MedicationAuditRequest request = MedicationAuditRequest.builder()
                .residentId(residentId)
                .orderId(orderId)
                .action(action != null ? action.toUpperCase() : null)
                .startDate(startDate)
                .endDate(endDate)
                .page(page)
                .limit(limit)
                .build();

        MedicationAuditResponse response = auditService.getMedicationAuditLog(facilityId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/audit/phi-access")
    public ResponseEntity<ApiResponse<PhiAccessLogResponse>> getPhiAccessLog(
            @RequestHeader(value = "X-Facility-ID", required = true) Long facilityId,
            @RequestParam(required = true) Long residentId,
            @RequestParam(required = false) String accessType,
            @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("GET /audit/phi-access - facilityId: {}, residentId: {}, accessType: {}, startDate: {}, endDate: {}",
                facilityId, residentId, accessType, startDate, endDate);

        if (facilityId == null) {
            throw new AppException(ErrorCode.MAR_FACILITY_REQUIRED);
        }

        if (residentId == null) {
            throw new AppException(ErrorCode.MAR_PHI_ACCESS_REQUIRED);
        }

        if (startDate.isAfter(endDate)) {
            throw new AppException(ErrorCode.MAR_AUDIT_INVALID_DATE_RANGE);
        }

        if (accessType != null && !accessType.isEmpty()) {
            String validTypes = "VIEW,PRINT,EXPORT,DOWNLOAD";
            if (!validTypes.contains(accessType.toUpperCase())) {
                throw new AppException(ErrorCode.MAR_AUDIT_ACCESS_TYPE_INVALID);
            }
        }

        PhiAccessLogRequest request = PhiAccessLogRequest.builder()
                .residentId(residentId)
                .accessType(accessType != null ? accessType.toUpperCase() : null)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        PhiAccessLogResponse response = auditService.getPhiAccessLog(facilityId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping(value = "/audit/mar-export", produces = "text/csv")
    public ResponseEntity<byte[]> exportMarAuditReport(
            @RequestHeader(value = "X-Facility-ID", required = true) Long facilityId,
            @RequestParam(required = false) Long residentId,
            @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("GET /audit/mar-export - facilityId: {}, residentId: {}, startDate: {}, endDate: {}",
                facilityId, residentId, startDate, endDate);

        if (facilityId == null) {
            throw new AppException(ErrorCode.MAR_FACILITY_REQUIRED);
        }

        if (startDate.isAfter(endDate)) {
            throw new AppException(ErrorCode.MAR_AUDIT_INVALID_DATE_RANGE);
        }

        MarExportRequest request = MarExportRequest.builder()
                .residentId(residentId)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        byte[] csvData = auditService.exportMarAuditReport(facilityId, request);

        String filename = "MAR_Audit_" + startDate + "_to_" + endDate + ".csv";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", filename);

        return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
    }
}