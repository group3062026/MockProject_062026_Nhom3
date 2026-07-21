package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.request.MarDashboardRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.response.MarDashboardResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.response.MarResidentResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.response.MarScheduleShiftResponse;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.service.MarService;
import jakarta.validation.constraints.NotNull;
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
@RequestMapping("/api/v1/mar")
@RequiredArgsConstructor
@Slf4j
public class MarController {

    private final MarService marService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<MarDashboardResponse>> getDashboard(
            @RequestHeader(value = "X-Facility-ID", required = true) Long facilityId,
            @RequestParam(required = false) String shift,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("GET /dashboard - facilityId: {}, shift: {}, status: {}, date: {}", facilityId, shift, status, date);

        if (facilityId == null) {
            throw new AppException(ErrorCode.MAR_FACILITY_REQUIRED);
        }

        MarDashboardRequest request = MarDashboardRequest.builder()
                .shift(shift)
                .status(status)
                .date(date != null ? date : LocalDate.now())
                .build();

        MarDashboardResponse response = marService.getDashboard(facilityId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/residents/{residentId}/mar")
    public ResponseEntity<ApiResponse<MarResidentResponse>> getResidentMar(
            @RequestHeader(value = "X-Facility-ID", required = true) Long facilityId,
            @PathVariable @NotNull Long residentId,
            @RequestParam(required = false) String dateRange,
            @RequestParam(required = false) String statusFilter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("GET /residents/{}/mar - facilityId: {}, dateRange: {}, statusFilter: {}",
                residentId, facilityId, dateRange, statusFilter);

        if (facilityId == null) {
            throw new AppException(ErrorCode.MAR_FACILITY_REQUIRED);
        }

        MarResidentResponse response = marService.getResidentMar(
                facilityId, residentId, dateRange, statusFilter, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping(value = "/residents/{residentId}/mar/print", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> printMar(
            @RequestHeader(value = "X-Facility-ID", required = true) Long facilityId,
            @PathVariable @NotNull Long residentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("GET /residents/{}/mar/print - facilityId: {}", residentId, facilityId);

        if (facilityId == null) {
            throw new AppException(ErrorCode.MAR_FACILITY_REQUIRED);
        }

        byte[] pdfData = marService.printMar(facilityId, residentId, startDate, endDate);

        String filename = "MAR_Resident_" + residentId + "_" + LocalDate.now() + ".pdf";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);

        return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
    }

    @GetMapping("/schedule/shift")
    public ResponseEntity<ApiResponse<MarScheduleShiftResponse>> getShiftSchedule(
            @RequestHeader(value = "X-Facility-ID", required = true) Long facilityId,
            @RequestParam(required = true) String shift,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("GET /schedule/shift - facilityId: {}, shift: {}, date: {}", facilityId, shift, date);

        if (facilityId == null) {
            throw new AppException(ErrorCode.MAR_FACILITY_REQUIRED);
        }

        if (!isValidShift(shift)) {
            throw new AppException(ErrorCode.MAR_INVALID_SHIFT);
        }

        MarScheduleShiftResponse response = marService.getShiftSchedule(
                facilityId, shift, date != null ? date : LocalDate.now());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private boolean isValidShift(String shift) {
        return shift != null && (
                shift.equalsIgnoreCase(com.nguyenquyen.mockproject_062026_group3.common.constants.MedicationStatus.DAY) ||
                        shift.equalsIgnoreCase(com.nguyenquyen.mockproject_062026_group3.common.constants.MedicationStatus.EVENING) ||
                        shift.equalsIgnoreCase(com.nguyenquyen.mockproject_062026_group3.common.constants.MedicationStatus.NIGHT)
        );
    }
}