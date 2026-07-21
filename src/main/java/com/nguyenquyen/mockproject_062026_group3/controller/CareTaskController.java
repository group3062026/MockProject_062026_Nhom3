package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.common.RequireRole;
import com.nguyenquyen.mockproject_062026_group3.dto.request.TaskStatusUpdateRequestDTO;
import com.nguyenquyen.mockproject_062026_group3.dto.response.CareTaskResponseDTO;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.service.CareTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * API untuk quản lý công việc chăm sóc (Care Tasks)
 * sc-032
 */
@RestController
@RequestMapping("api/v1/care-task")
@RequiredArgsConstructor
@Slf4j
public class CareTaskController {
    
    private final CareTaskService careTaskService;
    
    /**
     * Lấy danh sách công việc chăm sóc của ca làm việc
     * 
     * @param localDate Ngày cần lấy công việc (nếu null sẽ lấy ngày hiện tại)
     * @return Danh sách công việc được gom nhóm theo bệnh nhân
     */
    @GetMapping("/tasks")
    @RequireRole({"CNA", "NURSE", "MANAGER", "ADMIN"})
    public ApiResponse<CareTaskResponseDTO> getCareTask(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate localDate) {
        
        log.info("Getting care tasks for date: {}", localDate != null ? localDate : "today");
        CareTaskResponseDTO result = careTaskService.getCareTasks(localDate);
        return ApiResponse.success(result);
    }
    
    /**
     * Cập nhật trạng thái công việc chăm sóc
     * 
     * @param taskId ID của công việc
     * @param request DTO chứa trạng thái mới và cảnh báo (nếu có)
     * @return Thông báo thành công
     */
    @PatchMapping("/statustasks/{taskId}")
    @RequireRole({"CNA", "NURSE", "MANAGER", "ADMIN"})
    public ApiResponse<Void> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestBody TaskStatusUpdateRequestDTO request) {
        
        log.info("Updating task status for taskId: {} with status: {}", taskId, request.getStatus());
        careTaskService.updateTaskStatus(taskId, request);
        
        return ApiResponse.<Void>builder()
                .statusCode(ErrorCode.SUCCESS.getCode())
                .message("Cập nhật trạng thái công việc thành công")
                .build();
    }
}
