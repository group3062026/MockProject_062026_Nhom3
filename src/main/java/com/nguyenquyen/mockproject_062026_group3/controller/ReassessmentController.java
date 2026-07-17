package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.common.RequireRole;
import com.nguyenquyen.mockproject_062026_group3.dto.request.ReassessmentSubmitRequestDTO;
import com.nguyenquyen.mockproject_062026_group3.dto.response.CarePlanReassessmentResponseDTO;
import com.nguyenquyen.mockproject_062026_group3.dto.response.ReassessmentDashboardDTO;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.service.ReassessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * API để quản lý tái đánh giá kế hoạch chăm sóc (Reassessment)
 * sc-034
 */
@RestController
@RequestMapping("/api/v1/reassessment")
@RequiredArgsConstructor
@Slf4j
public class ReassessmentController {
    
    private final ReassessmentService reassessmentService;

    /**
     * Gửi tái đánh giá cho một kế hoạch chăm sóc
     * 
     * @param planId ID của kế hoạch chăm sóc
     * @param request DTO chứa dữ liệu tái đánh giá
     * @return Thông báo thành công
     */
    @PostMapping("/{planId}")
    @RequireRole({"NURSE", "MANAGER", "ADMIN"})
    public ApiResponse<Void> submitReassessment(
            @PathVariable Long planId,
            @RequestBody ReassessmentSubmitRequestDTO request) {
        
        log.info("Submitting reassessment for plan ID: {}", planId);
        reassessmentService.submitReassessment(planId, request);
        
        return ApiResponse.<Void>builder()
                .code(ErrorCode.SUCCESS.getCode())
                .message(ErrorCode.SUCCESS.getMessage())
                .build();
    }

    /**
     * Lấy thông tin kế hoạch chăm sóc để tái đánh giá
     * 
     * @param planId ID của kế hoạch chăm sóc
     * @return Thông tin kế hoạch và dữ liệu cần tái đánh giá
     */
    @GetMapping("/{planId}")
    @RequireRole({"NURSE", "MANAGER", "ADMIN"})
    public ApiResponse<CarePlanReassessmentResponseDTO> getReassessmentForm(@PathVariable Long planId) {
        
        log.info("Getting reassessment form for plan ID: {}", planId);
        CarePlanReassessmentResponseDTO result = reassessmentService.getCarePlanForReassessment(planId);
        return ApiResponse.success(result);
    }

    /**
     * Lấy dashboard tái đánh giá chứa danh sách các kế hoạch cần tái đánh giá
     * 
     * @return Dashboard dữ liệu tái đánh giá
     */
    @GetMapping
    @RequireRole({"NURSE", "MANAGER", "ADMIN"})
    public ApiResponse<ReassessmentDashboardDTO> getDashboard() {
        
        log.info("Getting reassessment dashboard");
        ReassessmentDashboardDTO data = reassessmentService.getReassessmentDashboard();
        return ApiResponse.success(data);
    }
}
