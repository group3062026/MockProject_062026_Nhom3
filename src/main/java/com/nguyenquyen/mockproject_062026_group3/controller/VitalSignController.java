package com.nguyenquyen.mockproject_062026_group3.controller;


import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.common.RequireRole;
import com.nguyenquyen.mockproject_062026_group3.dto.request.RecordVitalsRequestDTO;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.service.VitalSignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

/**
 * API để ghi nhận các chỉ số sống của bệnh nhân (Vital Signs)
 * sc-033
 */
@RestController
@RequestMapping("/api/v1/vitalsign")
@RequiredArgsConstructor
@Slf4j
public class VitalSignController {

    private final VitalSignService vitalSignService;

    /**
     * Ghi nhận các chỉ số sống của bệnh nhân và cập nhật công việc liên quan
     * 
     * @param request DTO chứa thông tin chỉ số sống
     * @return Thông báo thành công
     */
    @PostMapping("/recordvital")
    @RequireRole({"CNA", "NURSE", "MANAGER", "ADMIN"})
    public ApiResponse<Void> recordVitals(
            @RequestBody RecordVitalsRequestDTO request) {
        
        log.info("Recording vital signs for resident");
        vitalSignService.recordVitalsAndCompleteTask(request);

        return ApiResponse.<Void>builder()
                .statusCode(ErrorCode.SUCCESS.getCode())
                .message("Lưu sinh hiệu thành công và đã cập nhật công việc!")
                .build();
    }
}