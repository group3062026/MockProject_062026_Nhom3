package com.nguyenquyen.mockproject_062026_group3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nguyenquyen.mockproject_062026_group3.dto.request.TaskStatusUpdateRequestDTO;
import com.nguyenquyen.mockproject_062026_group3.dto.response.CareTaskResponseDTO;
import com.nguyenquyen.mockproject_062026_group3.service.CareTaskService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CareTaskController.class,
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
class CareTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CareTaskService careTaskService;

    // ── getCareTasks ──────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "CNA")
    void getCareTasks_noDate_returnsToday() throws Exception {
        CareTaskResponseDTO dto = CareTaskResponseDTO.builder()
                .shiftProgress(CareTaskResponseDTO.ShiftProgressDTO.builder()
                        .completed(2).total(5).build())
                .residents(List.of(
                        CareTaskResponseDTO.ResidentTasksDTO.builder()
                                .residentId(1L)
                                .name("Smith Alice")
                                .planStatus("ACTIVE")
                                .tasks(List.of(
                                        CareTaskResponseDTO.TaskItemDTO.builder()
                                                .taskId(1L)
                                                .taskName("BATHING")
                                                .dueTime("08:00")
                                                .status("PENDING")
                                                .hasAbnormalAlert(false)
                                                .build()
                                ))
                                .build()
                ))
                .build();

        when(careTaskService.getCareTasks(null)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/care-task/tasks")
                        .header("X-User-Role", "CNA")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.shiftProgress.total").value(5))
                .andExpect(jsonPath("$.data.shiftProgress.completed").value(2))
                .andExpect(jsonPath("$.data.residents[0].residentId").value(1))
                .andExpect(jsonPath("$.data.residents[0].tasks[0].taskName").value("BATHING"));
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void getCareTasks_withDate_returnsSuccess() throws Exception {
        CareTaskResponseDTO dto = CareTaskResponseDTO.builder()
                .shiftProgress(CareTaskResponseDTO.ShiftProgressDTO.builder()
                        .completed(0).total(3).build())
                .residents(List.of())
                .build();

        when(careTaskService.getCareTasks(any())).thenReturn(dto);

        mockMvc.perform(get("/api/v1/care-task/tasks")
                        .param("localDate", "2026-07-10")
                        .header("X-User-Role", "NURSE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.shiftProgress.total").value(3));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getCareTasks_asManager_returnsSuccess() throws Exception {
        CareTaskResponseDTO dto = CareTaskResponseDTO.builder()
                .shiftProgress(CareTaskResponseDTO.ShiftProgressDTO.builder()
                        .completed(1).total(1).build())
                .residents(List.of())
                .build();

        when(careTaskService.getCareTasks(any())).thenReturn(dto);

        mockMvc.perform(get("/api/v1/care-task/tasks")
                        .header("X-User-Role", "MANAGER")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ── updateTaskStatus ──────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "CNA")
    void updateTaskStatus_completed_returnsSuccess() throws Exception {
        TaskStatusUpdateRequestDTO request = new TaskStatusUpdateRequestDTO("COMPLETED", false, "Done");
        doNothing().when(careTaskService).updateTaskStatus(eq(1L), any());

        mockMvc.perform(patch("/api/v1/care-task/statustasks/1")
                        .header("X-User-Role", "CNA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Cập nhật trạng thái công việc thành công"));
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void updateTaskStatus_missed_returnsSuccess() throws Exception {
        TaskStatusUpdateRequestDTO request = new TaskStatusUpdateRequestDTO("MISSED", true, "Resident unavailable");
        doNothing().when(careTaskService).updateTaskStatus(eq(2L), any());

        mockMvc.perform(patch("/api/v1/care-task/statustasks/2")
                        .header("X-User-Role", "NURSE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    @WithMockUser(roles = "CNA")
    void updateTaskStatus_refused_returnsSuccess() throws Exception {
        TaskStatusUpdateRequestDTO request = new TaskStatusUpdateRequestDTO("REFUSED", false, "Resident refused");
        doNothing().when(careTaskService).updateTaskStatus(eq(3L), any());

        mockMvc.perform(patch("/api/v1/care-task/statustasks/3")
                        .header("X-User-Role", "CNA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CNA")
    void updateTaskStatus_withoutRole_returnsUnauthorized() throws Exception {
        TaskStatusUpdateRequestDTO request = new TaskStatusUpdateRequestDTO("COMPLETED", false, null);

        mockMvc.perform(patch("/api/v1/care-task/statustasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
