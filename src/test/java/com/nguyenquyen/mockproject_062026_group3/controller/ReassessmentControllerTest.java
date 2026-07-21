package com.nguyenquyen.mockproject_062026_group3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nguyenquyen.mockproject_062026_group3.dto.request.ReassessmentSubmitRequestDTO;
import com.nguyenquyen.mockproject_062026_group3.dto.response.CarePlanReassessmentResponseDTO;
import com.nguyenquyen.mockproject_062026_group3.dto.response.ReassessmentDashboardDTO;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.service.ReassessmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ReassessmentController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        com.nguyenquyen.mockproject_062026_group3.config.SecurityConfig.class,
                        com.nguyenquyen.mockproject_062026_group3.security.JwtAuthenticationFilter.class,
                        com.nguyenquyen.mockproject_062026_group3.security.JwtAuthEntryPoint.class
                }
        )
)
@AutoConfigureMockMvc(addFilters = false)
class ReassessmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReassessmentService reassessmentService;

    // ── getDashboard ──────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "NURSE")
    void getDashboard_returnsSuccess() throws Exception {
        ReassessmentDashboardDTO dto = ReassessmentDashboardDTO.builder()
                .totalRequiresReassessment(3)
                .totalOverdueEscalated(1)
                .escalationMessage("Escalation: Robert Hayes reassessment is past its grace period.")
                .items(List.of(
                        ReassessmentDashboardDTO.ReassessmentItemDTO.builder()
                                .carePlanId(1L)
                                .residentId(10L)
                                .residentDisplayName("Robert Hayes")
                                .trigger("90-day cycle")
                                .dueDate(LocalDate.now().minusDays(5))
                                .overdueDays(5)
                                .isEscalated(true)
                                .status("Review Due")
                                .action("Start")
                                .build()
                ))
                .build();

        when(reassessmentService.getReassessmentDashboard()).thenReturn(dto);

        mockMvc.perform(get("/api/v1/reassessment")
                        .header("X-User-Role", "NURSE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRequiresReassessment").value(3))
                .andExpect(jsonPath("$.data.totalOverdueEscalated").value(1))
                .andExpect(jsonPath("$.data.items[0].residentDisplayName").value("Robert Hayes"))
                .andExpect(jsonPath("$.data.items[0].isEscalated").value(true));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getDashboard_asManager_returnsSuccess() throws Exception {
        ReassessmentDashboardDTO dto = ReassessmentDashboardDTO.builder()
                .totalRequiresReassessment(0)
                .totalOverdueEscalated(0)
                .items(Collections.emptyList())
                .build();

        when(reassessmentService.getReassessmentDashboard()).thenReturn(dto);

        mockMvc.perform(get("/api/v1/reassessment")
                        .header("X-User-Role", "MANAGER")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRequiresReassessment").value(0));
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void getDashboard_withoutRole_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/reassessment")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ── getReassessmentForm ───────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "NURSE")
    void getReassessmentForm_returnsSuccess() throws Exception {
        CarePlanReassessmentResponseDTO dto = CarePlanReassessmentResponseDTO.builder()
                .planId(1L)
                .currentStatus("ACTIVE")
                .reviewDueDate(LocalDate.now().plusDays(5))
                .reviewTrigger("90_DAY_CYCLE")
                .goals(List.of(
                        CarePlanReassessmentResponseDTO.GoalResponseDTO.builder()
                                .id(1L)
                                .description("Improve mobility")
                                .status("IN_PROGRESS")
                                .build()
                ))
                .interventions(List.of(
                        CarePlanReassessmentResponseDTO.InterventionResponseDTO.builder()
                                .id(1L)
                                .description("Assist with ambulation")
                                .assignedRole("CNA")
                                .frequency("TWICE_DAILY")
                                .build()
                ))
                .build();

        when(reassessmentService.getCarePlanForReassessment(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/reassessment/1")
                        .header("X-User-Role", "NURSE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.planId").value(1))
                .andExpect(jsonPath("$.data.currentStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.data.goals[0].description").value("Improve mobility"))
                .andExpect(jsonPath("$.data.interventions[0].assignedRole").value("CNA"));
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void getReassessmentForm_planNotFound_returnsError() throws Exception {
        when(reassessmentService.getCarePlanForReassessment(9999L))
                .thenThrow(new AppException(ErrorCode.CARE_PLAN_NOT_FOUND));

        mockMvc.perform(get("/api/v1/reassessment/9999")
                        .header("X-User-Role", "NURSE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    // ── submitReassessment ────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "NURSE")
    void submitReassessment_fullPayload_returnsSuccess() throws Exception {
        ReassessmentSubmitRequestDTO request = new ReassessmentSubmitRequestDTO();
        request.setReassessmentReason("90-day cycle review");
        request.setGoals(List.of(
                buildGoal("Improve mobility", "IN_PROGRESS"),
                buildGoal("Maintain skin integrity", "IN_PROGRESS")
        ));
        request.setInterventions(List.of(
                buildIntervention("Assist with ambulation", "CNA", "TWICE_DAILY")
        ));

        doNothing().when(reassessmentService).submitReassessment(eq(1L), any());

        mockMvc.perform(post("/api/v1/reassessment/1")
                        .header("X-User-Role", "NURSE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void submitReassessment_significantChange_returnsSuccess() throws Exception {
        ReassessmentSubmitRequestDTO request = new ReassessmentSubmitRequestDTO();
        request.setReassessmentReason("SIGNIFICANT_CHANGE");
        request.setGoals(List.of(buildGoal("Prevent fall recurrence", "IN_PROGRESS")));
        request.setInterventions(List.of(buildIntervention("Fall prevention protocol", "RN", "DAILY")));

        doNothing().when(reassessmentService).submitReassessment(eq(2L), any());

        mockMvc.perform(post("/api/v1/reassessment/2")
                        .header("X-User-Role", "MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void submitReassessment_emptyGoalsAndInterventions_returnsSuccess() throws Exception {
        ReassessmentSubmitRequestDTO request = new ReassessmentSubmitRequestDTO();
        request.setReassessmentReason("Routine review");
        request.setGoals(Collections.emptyList());
        request.setInterventions(Collections.emptyList());

        doNothing().when(reassessmentService).submitReassessment(eq(3L), any());

        mockMvc.perform(post("/api/v1/reassessment/3")
                        .header("X-User-Role", "NURSE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void submitReassessment_planNotFound_returnsError() throws Exception {
        ReassessmentSubmitRequestDTO request = new ReassessmentSubmitRequestDTO();
        request.setReassessmentReason("Test");
        request.setGoals(Collections.emptyList());
        request.setInterventions(Collections.emptyList());

        doThrow(new AppException(ErrorCode.CARE_PLAN_NOT_FOUND))
                .when(reassessmentService).submitReassessment(eq(9999L), any());

        mockMvc.perform(post("/api/v1/reassessment/9999")
                        .header("X-User-Role", "NURSE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void submitReassessment_withoutRole_returnsUnauthorized() throws Exception {
        ReassessmentSubmitRequestDTO request = new ReassessmentSubmitRequestDTO();
        request.setReassessmentReason("Test");
        request.setGoals(Collections.emptyList());
        request.setInterventions(Collections.emptyList());

        mockMvc.perform(post("/api/v1/reassessment/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private ReassessmentSubmitRequestDTO.GoalDTO buildGoal(String description, String status) {
        ReassessmentSubmitRequestDTO.GoalDTO goal = new ReassessmentSubmitRequestDTO.GoalDTO();
        goal.setDescription(description);
        goal.setStatus(status);
        return goal;
    }

    private ReassessmentSubmitRequestDTO.InterventionDTO buildIntervention(String description, String role, String frequency) {
        ReassessmentSubmitRequestDTO.InterventionDTO intervention = new ReassessmentSubmitRequestDTO.InterventionDTO();
        intervention.setDescription(description);
        intervention.setAssignedRole(role);
        intervention.setFrequency(frequency);
        return intervention;
    }
}
