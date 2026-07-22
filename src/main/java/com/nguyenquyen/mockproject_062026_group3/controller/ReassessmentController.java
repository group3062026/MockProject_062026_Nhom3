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

 * API for managing care plan reassessment

 * sc-034

 */
@RestController
@RequestMapping("/api/v1/reassessment")
@RequiredArgsConstructor
@Slf4j
public class ReassessmentController {
    
    private final ReassessmentService reassessmentService;

    /**

     * Submit a reassessment for a care plan

     *
     * @param planId ID of the care plan

     * @param request DTO containing the reassessment data

     * @return Notification of success

     */
    @PostMapping("/{planId}")
    @RequireRole({"NURSE", "MANAGER", "ADMIN"})
    public ApiResponse<Void> submitReassessment(
            @PathVariable Long planId,
            @RequestBody ReassessmentSubmitRequestDTO request) {
        
        log.info("Submitting reassessment for plan ID: {}", planId);
        reassessmentService.submitReassessment(planId, request);
        
        return ApiResponse.<Void>builder()
                .statusCode(ErrorCode.SUCCESS.getCode())
                .message(ErrorCode.SUCCESS.getMessage())
                .build();
    }

    /**

     * Retrieve care plan information for reassessment

     *
     * @param planId Care plan ID

     * @return Plan information and data to be reassessed

     */
    @GetMapping("/{planId}")
    @RequireRole({"NURSE", "MANAGER", "ADMIN"})
    public ApiResponse<CarePlanReassessmentResponseDTO> getReassessmentForm(@PathVariable Long planId) {
        
        log.info("Getting reassessment form for plan ID: {}", planId);
        CarePlanReassessmentResponseDTO result = reassessmentService.getCarePlanForReassessment(planId);
        return ApiResponse.success(result);
    }

    /**

     * Get the reassessment dashboard containing the list of plans to be reassessed

     *
     * @return Reassessment Dashboard data

     */
    @GetMapping
    @RequireRole({"NURSE", "MANAGER", "ADMIN"})
    public ApiResponse<ReassessmentDashboardDTO> getDashboard() {
        
        log.info("Getting reassessment dashboard");
        ReassessmentDashboardDTO data = reassessmentService.getReassessmentDashboard();
        return ApiResponse.success(data);
    }
}
