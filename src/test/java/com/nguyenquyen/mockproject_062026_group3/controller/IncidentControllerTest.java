package com.nguyenquyen.mockproject_062026_group3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nguyenquyen.mockproject_062026_group3.dto.request.CreateTimelineRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.UnlockIncidentRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.response.IncidentDetailDTO;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.service.IncidentService;
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

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// SC_043 - M7-US-03b (incident-detail-unlocked)
// SC_044 - M7-US-03  (incident-detail-nurse)
@WebMvcTest(
        controllers = IncidentController.class,
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
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IncidentService incidentService;

    // ── SC_044: GET /api/v1/incidents/{id} ────────────────────────────────────

    @Test
    @WithMockUser(roles = "NURSE")
    void getIncidentDetail_asNurse_returnsSuccess() throws Exception {
        when(incidentService.getIncidentDetail(1L)).thenReturn(buildDetailDTO());

        mockMvc.perform(get("/api/v1/incidents/1")
                        .header("X-User-Role", "NURSE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.incidentType").value("MEDICATION_ERROR"))
                .andExpect(jsonPath("$.data.status").value("UNDER_INVESTIGATION"))
                .andExpect(jsonPath("$.data.resident.displayName").value("Stephen Curry"))
                .andExpect(jsonPath("$.data.severity.levelName").value("Low"))
                .andExpect(jsonPath("$.data.reporter.displayName").value("Angela Reyes"))
                .andExpect(jsonPath("$.data.timelines.length()").value(1));
    }

    @Test
    @WithMockUser(roles = "CNA")
    void getIncidentDetail_asCna_returnsSuccess() throws Exception {
        when(incidentService.getIncidentDetail(1L)).thenReturn(buildDetailDTO());

        mockMvc.perform(get("/api/v1/incidents/1")
                        .header("X-User-Role", "CNA")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser(roles = "DON")
    void getIncidentDetail_asDon_returnsSuccess() throws Exception {
        when(incidentService.getIncidentDetail(1L)).thenReturn(buildDetailDTO());

        mockMvc.perform(get("/api/v1/incidents/1")
                        .header("X-User-Role", "DON")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNDER_INVESTIGATION"));
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void getIncidentDetail_notFound_returns4xx() throws Exception {
        when(incidentService.getIncidentDetail(9999L))
                .thenThrow(new AppException(ErrorCode.INCIDENT_NOT_FOUND));

        mockMvc.perform(get("/api/v1/incidents/9999")
                        .header("X-User-Role", "NURSE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void getIncidentDetail_withoutRoleHeader_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/incidents/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void getIncidentDetail_residentHasBedAndRoom_returnsBedInfo() throws Exception {
        IncidentDetailDTO dto = buildDetailDTO();
        dto.getResident().setBed(IncidentDetailDTO.BedDTO.builder()
                .id(34L)
                .bedNumber("C")
                .isLocked(true)
                .room(IncidentDetailDTO.RoomDTO.builder()
                        .id(2L).roomNumber("102").roomType("SEMI_PRIVATE").build())
                .build());

        when(incidentService.getIncidentDetail(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/incidents/1")
                        .header("X-User-Role", "NURSE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resident.bed.bedNumber").value("C"))
                .andExpect(jsonPath("$.data.resident.bed.room.roomType").value("SEMI_PRIVATE"))
                .andExpect(jsonPath("$.data.resident.bed.isLocked").value(true));
    }

    // ── SC_043: PUT /api/v1/incidents/{id} (unlock) ───────────────────────────

    @Test
    @WithMockUser(roles = "DON")
    void unlockIncident_asDon_returnsSuccess() throws Exception {
        UnlockIncidentRequest request = new UnlockIncidentRequest();
        request.setReason("Problem resolved. Resident condition stable.");
        request.setPassword("securePass123");

        doNothing().when(incidentService).unlockIncident(eq(1L), any());

        mockMvc.perform(put("/api/v1/incidents/1")
                        .header("X-User-Role", "DON")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Incident resolved and chart unlocked successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unlockIncident_asAdmin_returnsSuccess() throws Exception {
        UnlockIncidentRequest request = new UnlockIncidentRequest();
        request.setReason("Reviewed and closed by admin.");
        request.setPassword("adminPass");

        doNothing().when(incidentService).unlockIncident(eq(2L), any());

        mockMvc.perform(put("/api/v1/incidents/2")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200));
    }

    @Test
    @WithMockUser(roles = "DON")
    void unlockIncident_emptyReason_returns4xx() throws Exception {
        UnlockIncidentRequest request = new UnlockIncidentRequest();
        request.setReason("");
        request.setPassword("pass");

        doThrow(new AppException(ErrorCode.INVALID_PARAMETER))
                .when(incidentService).unlockIncident(eq(1L), any());

        mockMvc.perform(put("/api/v1/incidents/1")
                        .header("X-User-Role", "DON")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "DON")
    void unlockIncident_notFound_returns4xx() throws Exception {
        UnlockIncidentRequest request = new UnlockIncidentRequest();
        request.setReason("Resolved");
        request.setPassword("pass");

        doThrow(new AppException(ErrorCode.INCIDENT_NOT_FOUND))
                .when(incidentService).unlockIncident(eq(9999L), any());

        mockMvc.perform(put("/api/v1/incidents/9999")
                        .header("X-User-Role", "DON")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void unlockIncident_asNurse_returnsUnauthorized() throws Exception {
        UnlockIncidentRequest request = new UnlockIncidentRequest();
        request.setReason("Trying to unlock");
        request.setPassword("pass");

        mockMvc.perform(put("/api/v1/incidents/1")
                        .header("X-User-Role", "NURSE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CNA")
    void unlockIncident_asCna_returnsForbidden() throws Exception {
        UnlockIncidentRequest request = new UnlockIncidentRequest();
        request.setReason("Trying to unlock");
        request.setPassword("pass");

        mockMvc.perform(put("/api/v1/incidents/1")
                        .header("X-User-Role", "CNA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "DON")
    void unlockIncident_withoutRoleHeader_returnsUnauthorized() throws Exception {
        UnlockIncidentRequest request = new UnlockIncidentRequest();
        request.setReason("Resolved");
        request.setPassword("pass");

        mockMvc.perform(put("/api/v1/incidents/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ── SC_043: POST /api/v1/incidents/{id}/timelines ─────────────────────────

    @Test
    @WithMockUser(roles = "NURSE")
    void addTimeline_asNurse_returnsCreated() throws Exception {
        CreateTimelineRequest request = new CreateTimelineRequest();
        request.setAction("Physician notified. Vitals monitored every 2 hours.");
        request.setReason(null);

        IncidentDetailDTO.TimelineDTO timelineDTO = IncidentDetailDTO.TimelineDTO.builder()
                .id(10L)
                .action("Physician notified. Vitals monitored every 2 hours.")
                .reason(null)
                .actor(IncidentDetailDTO.ActorDTO.builder().id(1L).displayName("Angela Reyes").build())
                .createdAt(OffsetDateTime.now())
                .build();

        when(incidentService.addTimeline(eq(1L), any())).thenReturn(timelineDTO);

        mockMvc.perform(post("/api/v1/incidents/1/timelines")
                        .header("X-User-Role", "NURSE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.action").value("Physician notified. Vitals monitored every 2 hours."))
                .andExpect(jsonPath("$.data.actor.displayName").value("Angela Reyes"));
    }

    @Test
    @WithMockUser(roles = "CNA")
    void addTimeline_asCna_returnsCreated() throws Exception {
        CreateTimelineRequest request = new CreateTimelineRequest();
        request.setAction("Resident assisted back to bed. No visible injuries.");

        IncidentDetailDTO.TimelineDTO timelineDTO = IncidentDetailDTO.TimelineDTO.builder()
                .id(11L)
                .action("Resident assisted back to bed. No visible injuries.")
                .createdAt(OffsetDateTime.now())
                .build();

        when(incidentService.addTimeline(eq(1L), any())).thenReturn(timelineDTO);

        mockMvc.perform(post("/api/v1/incidents/1/timelines")
                        .header("X-User-Role", "CNA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(11));
    }

    @Test
    @WithMockUser(roles = "DON")
    void addTimeline_asDon_withReason_returnsCreated() throws Exception {
        CreateTimelineRequest request = new CreateTimelineRequest();
        request.setAction("Investigation initiated. Family notified.");
        request.setReason("Significant incident requiring DON review");

        IncidentDetailDTO.TimelineDTO timelineDTO = IncidentDetailDTO.TimelineDTO.builder()
                .id(12L)
                .action("Investigation initiated. Family notified.")
                .reason("Significant incident requiring DON review")
                .createdAt(OffsetDateTime.now())
                .build();

        when(incidentService.addTimeline(eq(1L), any())).thenReturn(timelineDTO);

        mockMvc.perform(post("/api/v1/incidents/1/timelines")
                        .header("X-User-Role", "DON")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.reason").value("Significant incident requiring DON review"));
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void addTimeline_emptyAction_returns4xx() throws Exception {
        CreateTimelineRequest request = new CreateTimelineRequest();
        request.setAction("");

        doThrow(new AppException(ErrorCode.INVALID_PARAMETER))
                .when(incidentService).addTimeline(eq(1L), any());

        mockMvc.perform(post("/api/v1/incidents/1/timelines")
                        .header("X-User-Role", "NURSE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void addTimeline_incidentNotFound_returns4xx() throws Exception {
        CreateTimelineRequest request = new CreateTimelineRequest();
        request.setAction("Test action");

        doThrow(new AppException(ErrorCode.INCIDENT_NOT_FOUND))
                .when(incidentService).addTimeline(eq(9999L), any());

        mockMvc.perform(post("/api/v1/incidents/9999/timelines")
                        .header("X-User-Role", "NURSE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "NURSE")
    void addTimeline_withoutRoleHeader_returnsUnauthorized() throws Exception {
        CreateTimelineRequest request = new CreateTimelineRequest();
        request.setAction("Test");

        mockMvc.perform(post("/api/v1/incidents/1/timelines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private IncidentDetailDTO buildDetailDTO() {
        return IncidentDetailDTO.builder()
                .id(1L)
                .incidentType("MEDICATION_ERROR")
                .status("UNDER_INVESTIGATION")
                .description("Medication was administered later than scheduled.")
                .automaticLockChart(false)
                .slaDeadlineHours(13)
                .resident(IncidentDetailDTO.ResidentDTO.builder()
                        .id(12L)
                        .displayName("Stephen Curry")
                        .gender("Male")
                        .build())
                .severity(IncidentDetailDTO.SeverityDTO.builder()
                        .id(1L).levelName("Low").slaConfigured(24).build())
                .reporter(IncidentDetailDTO.ReporterDTO.builder()
                        .id(1L)
                        .employeeCode("NHMS-0004")
                        .displayName("Angela Reyes")
                        .email("angela.reyes@nhms-demo.local")
                        .phoneNumber("1-619-555-105")
                        .status("ACTIVE")
                        .build())
                .timelines(List.of(
                        IncidentDetailDTO.TimelineDTO.builder()
                                .id(1L)
                                .action("incident reported")
                                .reason(null)
                                .actor(IncidentDetailDTO.ActorDTO.builder()
                                        .id(1L).displayName("Kelvin").build())
                                .createdAt(OffsetDateTime.now())
                                .build()
                ))
                .reportedAt(OffsetDateTime.now())
                .build();
    }
}
