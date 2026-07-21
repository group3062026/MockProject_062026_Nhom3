package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.dto.request.ReassessmentSubmitRequestDTO;
import com.nguyenquyen.mockproject_062026_group3.dto.response.CarePlanReassessmentResponseDTO;
import com.nguyenquyen.mockproject_062026_group3.dto.response.ReassessmentDashboardDTO;

public interface ReassessmentService {
    CarePlanReassessmentResponseDTO getCarePlanForReassessment(Long planId);
    void submitReassessment(Long oldPlanId, ReassessmentSubmitRequestDTO request);
    ReassessmentDashboardDTO getReassessmentDashboard();
}
