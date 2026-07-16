package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.request.ReassessmentSubmitRequestDTO;
import com.nguyenquyen.mockproject_062026_group3.dto.response.CarePlanReassessmentResponseDTO;
import com.nguyenquyen.mockproject_062026_group3.dto.response.ReassessmentDashboardDTO;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.service.ReassessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
//sc-034
@RestController
@RequestMapping("/api/v1/reassessment")
@RequiredArgsConstructor
public class ReassessmentController {
    @Autowired
    private final ReassessmentService reassessmentService;

    @PostMapping
    public ApiResponse<Void> submitReassessment(
            @PathVariable Long planId,
             @RequestBody ReassessmentSubmitRequestDTO request) {

        reassessmentService.submitReassessment(planId, request);
        ApiResponse<Void> response = ApiResponse.<Void>builder().code(ErrorCode.SUCCESS.getCode())
                .message(ErrorCode.SUCCESS.getMessage()).data(null).build();
        return response;
    }
    @GetMapping("/{planId}")
    public ApiResponse<CarePlanReassessmentResponseDTO> getReassessmentForm(@PathVariable Long planId) {

        CarePlanReassessmentResponseDTO result = reassessmentService.getCarePlanForReassessment(planId);
        ApiResponse<CarePlanReassessmentResponseDTO> response = ApiResponse.success(result);
        return response;
    }
    @GetMapping
    public ApiResponse<ReassessmentDashboardDTO> getDashboard() {

        ReassessmentDashboardDTO data = reassessmentService.getReassessmentDashboard();
        ApiResponse<ReassessmentDashboardDTO> response = ApiResponse.success(data);
        return response;

    }
}
