package com.nguyenquyen.mockproject_062026_group3.controller;


import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.request.RecordVitalsRequestDTO;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.service.VitalSignService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
//sc-033
@RestController
@RequestMapping("/api/v1/vitalsign")
@RequiredArgsConstructor
public class VitalSignController {

    private final VitalSignService vitalSignService;

    @PostMapping("/recordvital")
    public ApiResponse<Void> recordVitals(
            @RequestBody RecordVitalsRequestDTO request) {
        vitalSignService.recordVitalsAndCompleteTask(request);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .message("Lưu sinh hiệu thành công và đã cập nhật công việc!")
                .build();

        return response;
    }
}