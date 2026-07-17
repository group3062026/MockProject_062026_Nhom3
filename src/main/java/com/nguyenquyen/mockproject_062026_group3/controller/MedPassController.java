package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.request.*;
import com.nguyenquyen.mockproject_062026_group3.dto.response.*;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.service.MedPassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mar/med-pass")
@RequiredArgsConstructor
@Slf4j
public class MedPassController {

    private final MedPassService medPassService;

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<StartMedPassResponse>> startSession(
            @RequestHeader(value = "X-Facility-ID", required = true) Long facilityId,
            @RequestBody StartMedPassRequest request) {

        log.info("POST /med-pass/start - facilityId: {}, residentId: {}", facilityId, request.getResidentId());

        if (facilityId == null) {
            throw new AppException(ErrorCode.MAR_FACILITY_REQUIRED);
        }

        if (request.getResidentId() == null) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }

        StartMedPassResponse response = medPassService.startSession(facilityId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/scan")
    public ResponseEntity<ApiResponse<ScanBarcodeResponse>> scanBarcode(
            @RequestHeader(value = "X-Facility-ID", required = true) Long facilityId,
            @RequestBody ScanBarcodeRequest request) {

        log.info("POST /med-pass/scan - facilityId: {}, residentId: {}, orderId: {}, scheduleId: {}",
                facilityId, request.getResidentId(), request.getOrderId(), request.getScheduleId());

        if (facilityId == null) {
            throw new AppException(ErrorCode.MAR_FACILITY_REQUIRED);
        }

        ScanBarcodeResponse response = medPassService.scanBarcode(facilityId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/administer")
    public ResponseEntity<ApiResponse<AdministerMedicationResponse>> administerMedication(
            @RequestHeader(value = "X-Facility-ID", required = true) Long facilityId,
            @RequestBody AdministerMedicationRequest request) {

        log.info("POST /med-pass/administer - facilityId: {}, orderId: {}, scheduleId: {}",
                facilityId, request.getOrderId(), request.getScheduleId());

        if (facilityId == null) {
            throw new AppException(ErrorCode.MAR_FACILITY_REQUIRED);
        }

        AdministerMedicationResponse response = medPassService.administerMedication(facilityId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/override")
    public ResponseEntity<ApiResponse<OverrideVerificationResponse>> overrideVerification(
            @RequestHeader(value = "X-Facility-ID", required = true) Long facilityId,
            @RequestBody OverrideVerificationRequest request) {

        log.info("POST /med-pass/override - facilityId: {}, orderId: {}, scheduleId: {}, reason: {}",
                facilityId, request.getOrderId(), request.getScheduleId(), request.getOverrideReason());

        if (facilityId == null) {
            throw new AppException(ErrorCode.MAR_FACILITY_REQUIRED);
        }

        OverrideVerificationResponse response = medPassService.overrideVerification(facilityId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/refuse")
    public ResponseEntity<ApiResponse<RefuseMedicationResponse>> refuseMedication(
            @RequestHeader(value = "X-Facility-ID", required = true) Long facilityId,
            @RequestBody RefuseMedicationRequest request) {

        log.info("POST /med-pass/refuse - facilityId: {}, orderId: {}, scheduleId: {}",
                facilityId, request.getOrderId(), request.getScheduleId());

        if (facilityId == null) {
            throw new AppException(ErrorCode.MAR_FACILITY_REQUIRED);
        }

        RefuseMedicationResponse response = medPassService.refuseMedication(facilityId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/hold")
    public ResponseEntity<ApiResponse<HoldMedicationResponse>> holdMedication(
            @RequestHeader(value = "X-Facility-ID", required = true) Long facilityId,
            @RequestBody HoldMedicationRequest request) {

        log.info("POST /med-pass/hold - facilityId: {}, orderId: {}, scheduleId: {}",
                facilityId, request.getOrderId(), request.getScheduleId());

        if (facilityId == null) {
            throw new AppException(ErrorCode.MAR_FACILITY_REQUIRED);
        }

        HoldMedicationResponse response = medPassService.holdMedication(facilityId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}