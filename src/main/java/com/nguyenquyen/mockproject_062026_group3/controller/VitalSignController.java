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

 * API for recording patient vital signs

 * sc-033

 */
@RestController
@RequestMapping("/api/v1/vitalsign")
@RequiredArgsConstructor
@Slf4j
public class VitalSignController {

    private final VitalSignService vitalSignService;

    /**
     * Record patient vital signs and update related tasks
     *
     * @param request DTO containing vital sign information
     * @return Success notification
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