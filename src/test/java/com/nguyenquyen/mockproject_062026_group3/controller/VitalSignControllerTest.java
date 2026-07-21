package com.nguyenquyen.mockproject_062026_group3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nguyenquyen.mockproject_062026_group3.dto.request.RecordVitalsRequestDTO;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.service.VitalSignService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = VitalSignController.class,
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
class VitalSignControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VitalSignService vitalSignService;

    // ── recordVitals ──────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "CNA")
    void recordVitals_normalReadings_returnsSuccess() throws Exception {
        RecordVitalsRequestDTO request = buildRequest(1L, 1L, 120, 80, 72, 98, 98.6, 16, 0, null);
        doNothing().when(vitalSignService).recordVitalsAndCompleteTask(any());

        mockMvc.perform(post("/api/v1/vitalsign/recordvital")
                        .header("X-User-Role", "CNA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Lưu sinh hiệu thành công và đã cập nhật công việc!"));
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void recordVitals_asNurse_returnsSuccess() throws Exception {
        RecordVitalsRequestDTO request = buildRequest(2L, 2L, 130, 85, 78, 96, 99.1, 18, 2, "Routine check");
        doNothing().when(vitalSignService).recordVitalsAndCompleteTask(any());

        mockMvc.perform(post("/api/v1/vitalsign/recordvital")
                        .header("X-User-Role", "NURSE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void recordVitals_asManager_returnsSuccess() throws Exception {
        RecordVitalsRequestDTO request = buildRequest(3L, 3L, 125, 82, 80, 97, 98.9, 17, 1, null);
        doNothing().when(vitalSignService).recordVitalsAndCompleteTask(any());

        mockMvc.perform(post("/api/v1/vitalsign/recordvital")
                        .header("X-User-Role", "MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CNA")
    void recordVitals_abnormalReadings_returnsSuccess() throws Exception {
        // Controller vẫn trả 200 — logic abnormal flag xử lý ở service
        RecordVitalsRequestDTO request = buildRequest(4L, 1L, 180, 110, 105, 88, 101.5, 24, 8,
                "Resident complained of chest tightness. RN notified.");
        doNothing().when(vitalSignService).recordVitalsAndCompleteTask(any());

        mockMvc.perform(post("/api/v1/vitalsign/recordvital")
                        .header("X-User-Role", "CNA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Lưu sinh hiệu thành công và đã cập nhật công việc!"));
    }

    @Test
    @WithMockUser(roles = "CNA")
    void recordVitals_nullTaskId_returnsSuccess() throws Exception {
        RecordVitalsRequestDTO request = buildRequest(null, 1L, 120, 80, 72, 98, 98.6, 16, 0, null);
        doNothing().when(vitalSignService).recordVitalsAndCompleteTask(any());

        mockMvc.perform(post("/api/v1/vitalsign/recordvital")
                        .header("X-User-Role", "CNA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CNA")
    void recordVitals_taskNotFound_returnsError() throws Exception {
        RecordVitalsRequestDTO request = buildRequest(9999L, 1L, 120, 80, 72, 98, 98.6, 16, 0, null);
        doThrow(new AppException(ErrorCode.CARE_PLAN_NOT_FOUND))
                .when(vitalSignService).recordVitalsAndCompleteTask(any());

        mockMvc.perform(post("/api/v1/vitalsign/recordvital")
                        .header("X-User-Role", "CNA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "CNA")
    void recordVitals_withoutRole_returnsUnauthorized() throws Exception {
        RecordVitalsRequestDTO request = buildRequest(1L, 1L, 120, 80, 72, 98, 98.6, 16, 0, null);

        mockMvc.perform(post("/api/v1/vitalsign/recordvital")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private RecordVitalsRequestDTO buildRequest(Long taskId, Long residentId,
            int systolic, int diastolic, int heartRate, int spo2,
            double temp, int respRate, int painScale, String notes) {
        RecordVitalsRequestDTO req = new RecordVitalsRequestDTO();
        req.setTaskId(taskId);
        req.setResidentId(residentId);
        req.setBloodPressureSystolic(systolic);
        req.setBloodPressureDiastolic(diastolic);
        req.setHeartRateBpm(heartRate);
        req.setSpo2Percentage(spo2);
        req.setTemperatureFahrenheit(temp);
        req.setRespiratoryRate(respRate);
        req.setPainScale(painScale);
        req.setNotes(notes);
        return req;
    }
}
