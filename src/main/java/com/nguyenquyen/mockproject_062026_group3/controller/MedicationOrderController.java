package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.request.CreateMedicationOrderRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.DiscontinueMedicationOrderRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.GetMedicationOrdersRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.response.*;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.service.MedicationOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class MedicationOrderController {

    private final MedicationOrderService medicationOrderService;

    @GetMapping("/medication-orders")
    public ResponseEntity<ApiResponse<MedicationOrderListResponse>> getMedicationOrders(
            @RequestHeader(value = "X-Facility-ID", required = true) Long facilityId,
            @RequestParam(required = false) Long residentId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {

        log.info("GET /medication-orders - facilityId: {}, residentId: {}, status: {}, search: {}, page: {}, limit: {}",
                facilityId, residentId, status, search, page, limit);

        if (facilityId == null) {
            throw new AppException(ErrorCode.MAR_FACILITY_REQUIRED);
        }

        GetMedicationOrdersRequest request = GetMedicationOrdersRequest.builder()
                .residentId(residentId)
                .status(status)
                .search(search)
                .page(page)
                .limit(limit)
                .build();

        MedicationOrderListResponse response = medicationOrderService.getMedicationOrders(facilityId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/medication-orders/{orderId}")
    public ResponseEntity<ApiResponse<MedicationOrderDetailResponse>> getMedicationOrderDetail(
            @RequestHeader(value = "X-Facility-ID", required = true) Long facilityId,
            @PathVariable Long orderId) {

        log.info("GET /medication-orders/{} - facilityId: {}", orderId, facilityId);

        if (facilityId == null) {
            throw new AppException(ErrorCode.MAR_FACILITY_REQUIRED);
        }

        if (orderId == null) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }

        MedicationOrderDetailResponse response = medicationOrderService.getMedicationOrderDetail(facilityId, orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/medication-orders")
    public ResponseEntity<ApiResponse<CreateMedicationOrderResponse>> createMedicationOrder(
            @RequestHeader(value = "X-Facility-ID", required = true) Long facilityId,
            @RequestBody CreateMedicationOrderRequest request) {

        log.info("POST /medication-orders - facilityId: {}, residentId: {}, drugName: {}",
                facilityId, request.getResidentId(), request.getDrugName());

        if (facilityId == null) {
            throw new AppException(ErrorCode.MAR_FACILITY_REQUIRED);
        }

        CreateMedicationOrderResponse response = medicationOrderService.createMedicationOrder(facilityId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PatchMapping("/medication-orders/{orderId}/discontinue")
    public ResponseEntity<ApiResponse<DiscontinueMedicationOrderResponse>> discontinueMedicationOrder(
            @RequestHeader(value = "X-Facility-ID", required = true) Long facilityId,
            @PathVariable Long orderId,
            @RequestBody DiscontinueMedicationOrderRequest request) {

        log.info("PATCH /medication-orders/{}/discontinue - facilityId: {}, reason: {}",
                orderId, facilityId, request.getDiscontinueReason());

        if (facilityId == null) {
            throw new AppException(ErrorCode.MAR_FACILITY_REQUIRED);
        }

        if (orderId == null) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }

        DiscontinueMedicationOrderResponse response = medicationOrderService.discontinueMedicationOrder(
                facilityId, orderId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/mar/med-pass/resident/{residentId}/pending")
    public ResponseEntity<ApiResponse<PendingMedicationResponse>> getResidentPendingMedications(
            @RequestHeader(value = "X-Facility-ID", required = true) Long facilityId,
            @PathVariable Long residentId,
            @RequestParam(required = false) String time) {

        log.info("GET /mar/med-pass/resident/{}/pending - facilityId: {}, time: {}",
                residentId, facilityId, time);

        if (facilityId == null) {
            throw new AppException(ErrorCode.MAR_FACILITY_REQUIRED);
        }

        if (residentId == null) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }

        PendingMedicationResponse response = medicationOrderService.getResidentPendingMedications(
                facilityId, residentId, time);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}