package com.nguyenquyen.mockproject_062026_group3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nguyenquyen.mockproject_062026_group3.dto.response.MedicationAuditResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.response.PhiAccessLogResponse;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.service.AuditService;
import com.nguyenquyen.mockproject_062026_group3.testutils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditController Unit Tests - Phần 5")
class AuditControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuditController auditController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(auditController).build();
        objectMapper = new ObjectMapper();
    }

    // ==================== API 18: Get Medication Audit Log Tests ====================

    @Nested
    @DisplayName("API 18: Get Medication Audit Log - Tests")
    class GetMedicationAuditLogTests {

        @Test
        @DisplayName("TC01 - Success: Should return audit logs")
        void getMedicationAuditLog_Success_ShouldReturnLogs() throws Exception {
            // Given
            Long facilityId = 1L;
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            MedicationAuditResponse mockResponse = TestDataFactory.createMedicationAuditResponse();

            when(auditService.getMedicationAuditLog(eq(facilityId), any()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/audit/medication")
                            .header("X-Facility-ID", facilityId)
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .param("page", "1")
                            .param("limit", "50")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data.total").value(10))
                    .andExpect(jsonPath("$.data.page").value(1))
                    .andExpect(jsonPath("$.data.limit").value(50))
                    .andExpect(jsonPath("$.data.logs").isArray())
                    .andExpect(jsonPath("$.data.logs.length()").value(2));

            verify(auditService, times(1)).getMedicationAuditLog(eq(facilityId), any());
        }

        @Test
        @DisplayName("TC02 - Success: With resident ID filter")
        void getMedicationAuditLog_WithResidentId_ShouldReturnFilteredLogs() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            MedicationAuditResponse mockResponse = TestDataFactory.createMedicationAuditResponse();

            when(auditService.getMedicationAuditLog(eq(facilityId), any()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/audit/medication")
                            .header("X-Facility-ID", facilityId)
                            .param("residentId", String.valueOf(residentId))
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(auditService, times(1)).getMedicationAuditLog(eq(facilityId), any());
        }

        @Test
        @DisplayName("TC03 - Success: With order ID filter")
        void getMedicationAuditLog_WithOrderId_ShouldReturnFilteredLogs() throws Exception {
            // Given
            Long facilityId = 1L;
            Long orderId = 1L;
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            MedicationAuditResponse mockResponse = TestDataFactory.createMedicationAuditResponse();

            when(auditService.getMedicationAuditLog(eq(facilityId), any()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/audit/medication")
                            .header("X-Facility-ID", facilityId)
                            .param("orderId", String.valueOf(orderId))
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(auditService, times(1)).getMedicationAuditLog(eq(facilityId), any());
        }

        @Test
        @DisplayName("TC04 - Success: With INSERT action filter")
        void getMedicationAuditLog_WithInsertAction_ShouldReturnFilteredLogs() throws Exception {
            // Given
            Long facilityId = 1L;
            String action = "INSERT";
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            MedicationAuditResponse mockResponse = TestDataFactory.createMedicationAuditResponse();

            when(auditService.getMedicationAuditLog(eq(facilityId), any()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/audit/medication")
                            .header("X-Facility-ID", facilityId)
                            .param("action", action)
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(auditService, times(1)).getMedicationAuditLog(eq(facilityId), any());
        }

        @Test
        @DisplayName("TC05 - Success: With UPDATE action filter")
        void getMedicationAuditLog_WithUpdateAction_ShouldReturnFilteredLogs() throws Exception {
            // Given
            Long facilityId = 1L;
            String action = "UPDATE";
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            MedicationAuditResponse mockResponse = TestDataFactory.createMedicationAuditResponse();

            when(auditService.getMedicationAuditLog(eq(facilityId), any()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/audit/medication")
                            .header("X-Facility-ID", facilityId)
                            .param("action", action)
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(auditService, times(1)).getMedicationAuditLog(eq(facilityId), any());
        }

        @Test
        @DisplayName("TC06 - Success: With custom pagination")
        void getMedicationAuditLog_CustomPagination_ShouldReturnPaginated() throws Exception {
            // Given
            Long facilityId = 1L;
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();
            Integer page = 2;
            Integer limit = 20;

            MedicationAuditResponse mockResponse = TestDataFactory.createMedicationAuditResponse();

            when(auditService.getMedicationAuditLog(eq(facilityId), any()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/audit/medication")
                            .header("X-Facility-ID", facilityId)
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .param("page", String.valueOf(page))
                            .param("limit", String.valueOf(limit))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(auditService, times(1)).getMedicationAuditLog(eq(facilityId), any());
        }

        @Test
        @DisplayName("TC07 - Success: Empty logs should return empty list")
        void getMedicationAuditLog_EmptyLogs_ShouldReturnEmptyList() throws Exception {
            // Given
            Long facilityId = 1L;
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            MedicationAuditResponse mockResponse = TestDataFactory.createEmptyMedicationAuditResponse();

            when(auditService.getMedicationAuditLog(eq(facilityId), any()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/audit/medication")
                            .header("X-Facility-ID", facilityId)
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.logs").isEmpty());

            verify(auditService, times(1)).getMedicationAuditLog(eq(facilityId), any());
        }

        @Test
        @DisplayName("TC08 - Error: Missing start date should return error")
        void getMedicationAuditLog_NoStartDate_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            LocalDate endDate = LocalDate.now();

            // When & Then
            mockMvc.perform(get("/api/v1/audit/medication")
                            .header("X-Facility-ID", facilityId)
                            .param("endDate", endDate.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC09 - Error: Missing end date should return error")
        void getMedicationAuditLog_NoEndDate_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            LocalDate startDate = LocalDate.now().minusDays(7);

            // When & Then
            mockMvc.perform(get("/api/v1/audit/medication")
                            .header("X-Facility-ID", facilityId)
                            .param("startDate", startDate.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC10 - Error: Invalid date range should return error")
        void getMedicationAuditLog_InvalidDateRange_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = LocalDate.now().minusDays(7);

            // When & Then
            mockMvc.perform(get("/api/v1/audit/medication")
                            .header("X-Facility-ID", facilityId)
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC11 - Error: Invalid action should return error")
        void getMedicationAuditLog_InvalidAction_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            String invalidAction = "INVALID";
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            when(auditService.getMedicationAuditLog(eq(facilityId), any()))
                    .thenThrow(new AppException(ErrorCode.MAR_AUDIT_ACTION_INVALID));

            // When & Then
            mockMvc.perform(get("/api/v1/audit/medication")
                            .header("X-Facility-ID", facilityId)
                            .param("action", invalidAction)
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(auditService, times(1)).getMedicationAuditLog(eq(facilityId), any());
        }

        @Test
        @DisplayName("TC12 - Error: Missing facility ID should return error")
        void getMedicationAuditLog_NoFacilityId_ShouldReturnError() throws Exception {
            // Given
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            // When & Then
            mockMvc.perform(get("/api/v1/audit/medication")
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(auditService, never()).getMedicationAuditLog(anyLong(), any());
        }
    }

    // ==================== API 19: Get PHI Access Log Tests ====================

    @Nested
    @DisplayName("API 19: Get PHI Access Log - Tests")
    class GetPhiAccessLogTests {

        @Test
        @DisplayName("TC13 - Success: Should return PHI access logs")
        void getPhiAccessLog_Success_ShouldReturnLogs() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            PhiAccessLogResponse mockResponse = TestDataFactory.createPhiAccessLogResponse();

            when(auditService.getPhiAccessLog(eq(facilityId), any()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/audit/phi-access")
                            .header("X-Facility-ID", facilityId)
                            .param("residentId", String.valueOf(residentId))
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data.logs").isArray())
                    .andExpect(jsonPath("$.data.logs.length()").value(2))
                    .andExpect(jsonPath("$.data.logs[0].accessType").value("VIEW"))
                    .andExpect(jsonPath("$.data.logs[1].accessType").value("PRINT"));

            verify(auditService, times(1)).getPhiAccessLog(eq(facilityId), any());
        }

        @Test
        @DisplayName("TC14 - Success: With VIEW access type filter")
        void getPhiAccessLog_WithViewAccessType_ShouldReturnFilteredLogs() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;
            String accessType = "VIEW";
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            PhiAccessLogResponse mockResponse = TestDataFactory.createPhiAccessLogResponse();

            when(auditService.getPhiAccessLog(eq(facilityId), any()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/audit/phi-access")
                            .header("X-Facility-ID", facilityId)
                            .param("residentId", String.valueOf(residentId))
                            .param("accessType", accessType)
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(auditService, times(1)).getPhiAccessLog(eq(facilityId), any());
        }

        @Test
        @DisplayName("TC15 - Success: With PRINT access type filter")
        void getPhiAccessLog_WithPrintAccessType_ShouldReturnFilteredLogs() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;
            String accessType = "PRINT";
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            PhiAccessLogResponse mockResponse = TestDataFactory.createPhiAccessLogResponse();

            when(auditService.getPhiAccessLog(eq(facilityId), any()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/audit/phi-access")
                            .header("X-Facility-ID", facilityId)
                            .param("residentId", String.valueOf(residentId))
                            .param("accessType", accessType)
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(auditService, times(1)).getPhiAccessLog(eq(facilityId), any());
        }

        @Test
        @DisplayName("TC16 - Success: With EXPORT access type filter")
        void getPhiAccessLog_WithExportAccessType_ShouldReturnFilteredLogs() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;
            String accessType = "EXPORT";
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            PhiAccessLogResponse mockResponse = TestDataFactory.createPhiAccessLogResponse();

            when(auditService.getPhiAccessLog(eq(facilityId), any()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/audit/phi-access")
                            .header("X-Facility-ID", facilityId)
                            .param("residentId", String.valueOf(residentId))
                            .param("accessType", accessType)
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(auditService, times(1)).getPhiAccessLog(eq(facilityId), any());
        }

        @Test
        @DisplayName("TC17 - Success: Empty logs should return empty list")
        void getPhiAccessLog_EmptyLogs_ShouldReturnEmptyList() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            PhiAccessLogResponse mockResponse = TestDataFactory.createEmptyPhiAccessLogResponse();

            when(auditService.getPhiAccessLog(eq(facilityId), any()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/audit/phi-access")
                            .header("X-Facility-ID", facilityId)
                            .param("residentId", String.valueOf(residentId))
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.logs").isEmpty());

            verify(auditService, times(1)).getPhiAccessLog(eq(facilityId), any());
        }

        @Test
        @DisplayName("TC18 - Error: Missing resident ID should return error")
        void getPhiAccessLog_NoResidentId_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            // When & Then
            mockMvc.perform(get("/api/v1/audit/phi-access")
                            .header("X-Facility-ID", facilityId)
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(auditService, never()).getPhiAccessLog(anyLong(), any());
        }

        @Test
        @DisplayName("TC19 - Error: Invalid access type should return error")
        void getPhiAccessLog_InvalidAccessType_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;
            String invalidAccessType = "INVALID";
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            when(auditService.getPhiAccessLog(eq(facilityId), any()))
                    .thenThrow(new AppException(ErrorCode.MAR_AUDIT_ACCESS_TYPE_INVALID));

            // When & Then
            mockMvc.perform(get("/api/v1/audit/phi-access")
                            .header("X-Facility-ID", facilityId)
                            .param("residentId", String.valueOf(residentId))
                            .param("accessType", invalidAccessType)
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            verify(auditService, times(1)).getPhiAccessLog(eq(facilityId), any());
        }

        @Test
        @DisplayName("TC20 - Error: Invalid date range should return error")
        void getPhiAccessLog_InvalidDateRange_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = LocalDate.now().minusDays(7);

            // When & Then
            mockMvc.perform(get("/api/v1/audit/phi-access")
                            .header("X-Facility-ID", facilityId)
                            .param("residentId", String.valueOf(residentId))
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== API 20: Export MAR Audit Report Tests ====================

    @Nested
    @DisplayName("API 20: Export MAR Audit Report - Tests")
    class ExportMarAuditReportTests {

        @Test
        @DisplayName("TC21 - Success: Should return CSV file")
        void exportMarAuditReport_Success_ShouldReturnCSV() throws Exception {
            // Given
            Long facilityId = 1L;
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            byte[] csvData = TestDataFactory.createMarExportCSV().getBytes();

            when(auditService.exportMarAuditReport(eq(facilityId), any()))
                    .thenReturn(csvData);

            // When & Then
            mockMvc.perform(get("/api/v1/audit/mar-export")
                            .header("X-Facility-ID", facilityId)
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .accept("text/csv"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("text/csv"))
                    .andExpect(header().string("Content-Disposition",
                            "attachment; filename=MAR_Audit_" + startDate + "_to_" + endDate + ".csv"));

            verify(auditService, times(1)).exportMarAuditReport(eq(facilityId), any());
        }

        @Test
        @DisplayName("TC22 - Success: With resident ID filter")
        void exportMarAuditReport_WithResidentId_ShouldReturnCSV() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            byte[] csvData = TestDataFactory.createMarExportCSV().getBytes();

            when(auditService.exportMarAuditReport(eq(facilityId), any()))
                    .thenReturn(csvData);

            // When & Then
            mockMvc.perform(get("/api/v1/audit/mar-export")
                            .header("X-Facility-ID", facilityId)
                            .param("residentId", String.valueOf(residentId))
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .accept("text/csv"))
                    .andExpect(status().isOk());

            verify(auditService, times(1)).exportMarAuditReport(eq(facilityId), any());
        }

        @Test
        @DisplayName("TC23 - Success: Single day range")
        void exportMarAuditReport_SingleDay_ShouldReturnCSV() throws Exception {
            // Given
            Long facilityId = 1L;
            LocalDate date = LocalDate.now();

            byte[] csvData = TestDataFactory.createMarExportCSV().getBytes();

            when(auditService.exportMarAuditReport(eq(facilityId), any()))
                    .thenReturn(csvData);

            // When & Then
            mockMvc.perform(get("/api/v1/audit/mar-export")
                            .header("X-Facility-ID", facilityId)
                            .param("startDate", date.toString())
                            .param("endDate", date.toString())
                            .accept("text/csv"))
                    .andExpect(status().isOk());

            verify(auditService, times(1)).exportMarAuditReport(eq(facilityId), any());
        }

        @Test
        @DisplayName("TC24 - Error: Missing start date should return error")
        void exportMarAuditReport_NoStartDate_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            LocalDate endDate = LocalDate.now();

            // When & Then
            mockMvc.perform(get("/api/v1/audit/mar-export")
                            .header("X-Facility-ID", facilityId)
                            .param("endDate", endDate.toString())
                            .accept("text/csv"))
                    .andExpect(status().isBadRequest());

            verify(auditService, never()).exportMarAuditReport(anyLong(), any());
        }

        @Test
        @DisplayName("TC25 - Error: Missing end date should return error")
        void exportMarAuditReport_NoEndDate_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            LocalDate startDate = LocalDate.now().minusDays(7);

            // When & Then
            mockMvc.perform(get("/api/v1/audit/mar-export")
                            .header("X-Facility-ID", facilityId)
                            .param("startDate", startDate.toString())
                            .accept("text/csv"))
                    .andExpect(status().isBadRequest());

            verify(auditService, never()).exportMarAuditReport(anyLong(), any());
        }

        @Test
        @DisplayName("TC26 - Error: Invalid date range should return error")
        void exportMarAuditReport_InvalidDateRange_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = LocalDate.now().minusDays(7);

            // When & Then
            mockMvc.perform(get("/api/v1/audit/mar-export")
                            .header("X-Facility-ID", facilityId)
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .accept("text/csv"))
                    .andExpect(status().isBadRequest());

            verify(auditService, never()).exportMarAuditReport(anyLong(), any());
        }

        @Test
        @DisplayName("TC27 - Error: Export failed should return 500")
        void exportMarAuditReport_ExportFailed_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            when(auditService.exportMarAuditReport(eq(facilityId), any()))
                    .thenThrow(new AppException(ErrorCode.MAR_AUDIT_EXPORT_FAILED));

            // When & Then
            mockMvc.perform(get("/api/v1/audit/mar-export")
                            .header("X-Facility-ID", facilityId)
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .accept("text/csv"))
                    .andExpect(status().isInternalServerError());

            verify(auditService, times(1)).exportMarAuditReport(eq(facilityId), any());
        }

        @Test
        @DisplayName("TC28 - Error: Missing facility ID should return error")
        void exportMarAuditReport_NoFacilityId_ShouldReturnError() throws Exception {
            // Given
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            // When & Then
            mockMvc.perform(get("/api/v1/audit/mar-export")
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .accept("text/csv"))
                    .andExpect(status().isBadRequest());

            verify(auditService, never()).exportMarAuditReport(anyLong(), any());
        }
    }
}