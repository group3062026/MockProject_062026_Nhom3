package com.nguyenquyen.mockproject_062026_group3.service.impl;

import com.nguyenquyen.mockproject_062026_group3.dto.request.ReassessmentSubmitRequestDTO;
import com.nguyenquyen.mockproject_062026_group3.dto.response.CarePlanReassessmentResponseDTO;
import com.nguyenquyen.mockproject_062026_group3.dto.response.ReassessmentDashboardDTO;
import com.nguyenquyen.mockproject_062026_group3.entity.CareGoal;
import com.nguyenquyen.mockproject_062026_group3.entity.CareIntervention;
import com.nguyenquyen.mockproject_062026_group3.entity.CarePlan;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.repository.CareGoalRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.CareInterventionRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.CarePlanRepository;
import com.nguyenquyen.mockproject_062026_group3.service.ReassessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
//sc-034
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReassessmentServiceImpl implements ReassessmentService {
    @Autowired
    private final CarePlanRepository carePlanRepository;
    @Autowired
    private final CareGoalRepository careGoalRepository;
    @Autowired
    private final CareInterventionRepository careInterventionRepository;

    @Override
    public void submitReassessment(Long oldPlanId, ReassessmentSubmitRequestDTO request) {

        // 1. Get the original
        CarePlan oldPlan = carePlanRepository.findById(oldPlanId)
                .orElseThrow(() -> new AppException(ErrorCode.CARE_PLAN_NOT_FOUND));

        // 2. CREATE A NEW DRAFT (Version 2)
        CarePlan newPlan = new CarePlan();
        newPlan.setResident(oldPlan.getResident());
        newPlan.setVersion(oldPlan.getVersion() + 1); // Tăng version
        newPlan.setParentPlanId(oldPlan.getId());     // Lưu vết gốc
        newPlan.setStatus("PENDING_REVIEW");          // AC 3: Chờ DON duyệt
        newPlan.setReviewTrigger(request.getReassessmentReason());
        newPlan.setReviewDueDate(LocalDate.now().plusDays(90));

        if ("SIGNIFICANT_CHANGE".equals(request.getReassessmentReason())) {
            newPlan.setSignificantChangeFlag(true);
        } else {
            newPlan.setSignificantChangeFlag(oldPlan.getSignificantChangeFlag());
        }

        CarePlan savedNewPlan = carePlanRepository.save(newPlan);

        // 3. SAVE NEW GOALS
        for (ReassessmentSubmitRequestDTO.GoalDTO goalDto : request.getGoals()) {
            CareGoal goal = new CareGoal();
            goal.setCarePlan(savedNewPlan);
            goal.setGoalDescription(goalDto.getDescription());
            goal.setStatus(goalDto.getStatus() != null ? goalDto.getStatus() : "IN_PROGRESS");
            careGoalRepository.save(goal);
        }

        // 4. SAVE NEW INTERVENTIONS
        for (ReassessmentSubmitRequestDTO.InterventionDTO intDto : request.getInterventions()) {
            CareIntervention intervention = new CareIntervention();
            intervention.setCarePlan(savedNewPlan);
            intervention.setDescription(intDto.getDescription());
            intervention.setAssignedRole(intDto.getAssignedRole() != null ? intDto.getAssignedRole() : "CNA");
            intervention.setFrequency(intDto.getFrequency());
            careInterventionRepository.save(intervention);
        }
    }

    @Override
    public CarePlanReassessmentResponseDTO getCarePlanForReassessment(Long planId) {
        // 1. Get the original Plan
        CarePlan plan = carePlanRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.CARE_PLAN_NOT_FOUND));

        // 2. Map the list of Goals
        List<CarePlanReassessmentResponseDTO.GoalResponseDTO> goalDTOs = plan.getCareGoals().stream()
                .map(g -> CarePlanReassessmentResponseDTO.GoalResponseDTO.builder()
                        .id(g.getId())
                        .description(g.getGoalDescription())
                        .status(g.getStatus())
                        .build())
                .collect(Collectors.toList());

        // 3. Map of Interventions
        List<CarePlanReassessmentResponseDTO.InterventionResponseDTO> interventionDTOs = plan.getCareInterventions().stream()
                .map(i -> CarePlanReassessmentResponseDTO.InterventionResponseDTO.builder()
                        .id(i.getId())
                        .description(i.getDescription())
                        .assignedRole(i.getAssignedRole())
                        .frequency(i.getFrequency())
                        .build())
                .collect(Collectors.toList());

        // 4. Wrap everything in a Response DTO
        return CarePlanReassessmentResponseDTO.builder()
                .planId(plan.getId())
                .currentStatus(plan.getStatus())
                .reviewDueDate(plan.getReviewDueDate())
                .reviewTrigger(plan.getReviewTrigger())
                .goals(goalDTOs)
                .interventions(interventionDTOs)
                .build();
    }
    public ReassessmentDashboardDTO getReassessmentDashboard() {
        List<CarePlan> plans = carePlanRepository.findAllDashboardPlans();
        LocalDate today = LocalDate.now();

        List<ReassessmentDashboardDTO.ReassessmentItemDTO> itemDTOs = new ArrayList<>();
        int requireCount = 0;
        int escalatedCount = 0;
        String escalationMsg = null;

        for (CarePlan cp : plans) {
            // Get patient's name
            String displayName = cp.getResident().getFirstName() + " " + cp.getResident().getLastName();

            // Handle the text display for the Trigger
            String triggerName = "90-day cycle";
            if ("SIGNIFICANT_CHANGE".equals(cp.getReviewTrigger())) {
                triggerName = "Significant Change (SCS)";
            }

            // Calculate the number of overdue days
            Integer overdueDays = null;
            boolean isEscalated = false;

            if (cp.getReviewDueDate() != null && cp.getReviewDueDate().isBefore(today)) {
                overdueDays = (int) ChronoUnit.DAYS.between(cp.getReviewDueDate(), today);
                // If there is a delay of more than 3 days (Grace Period), a red alert will be triggered.
                if (overdueDays > 3) {
                    isEscalated = true;
                    escalatedCount++;
                    escalationMsg = "Escalation: " + displayName + " reassessment is past its grace period.";
                }
            }

            // Define UI Actions and States
            String action = "Start";
            String uiStatus = "Review Due";

            if ("SIGNIFICANT_CHANGE".equals(cp.getReviewTrigger()) || "NEEDS_UPDATE".equals(cp.getStatus())) {
                uiStatus = "Needs Update";
                requireCount++;
            } else if (overdueDays != null || (cp.getReviewDueDate() != null && ChronoUnit.DAYS.between(today, cp.getReviewDueDate()) <= 7)) {
                uiStatus = "Review Due";
                requireCount++;
            } else {
                uiStatus = "Active";
                action = "View"; // If it's active normally, only allow View
            }

            itemDTOs.add(ReassessmentDashboardDTO.ReassessmentItemDTO.builder()
                    .carePlanId(cp.getId())
                    .residentId(cp.getResident().getId())
                    .residentDisplayName(displayName)
                    .trigger(triggerName)
                    .dueDate(cp.getReviewDueDate())
                    .overdueDays(overdueDays)
                    .isEscalated(isEscalated)
                    .status(uiStatus)
                    .action(action)
                    .build());
        }

        return ReassessmentDashboardDTO.builder()
                .totalRequiresReassessment(requireCount)
                .totalOverdueEscalated(escalatedCount)
                .escalationMessage(escalationMsg)
                .items(itemDTOs)
                .build();
    }
}
