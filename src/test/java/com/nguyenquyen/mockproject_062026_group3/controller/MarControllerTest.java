package com.nguyenquyen.mockproject_062026_group3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nguyenquyen.mockproject_062026_group3.dto.request.MarDashboardRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.response.MarDashboardResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.response.MarResidentResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.response.MarScheduleShiftResponse;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.service.MarService;
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
@DisplayName("MarController Unit Tests - Phần 1")
class MarControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private MarService marService;

    @InjectMocks
    private MarController marController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(marController).build();
        objectMapper = new ObjectMapper();
    }

    // ==================== API 1: Get Dashboard Tests ====================

    @Nested
    @DisplayName("API 1: Get eMAR Dashboard - Tests")
    class GetDashboardTests {

        @Test
        @DisplayName("TC01 - Success: Should return dashboard with full data")
        void getDashboard_Success_ShouldReturnFullData() throws Exception {
            // Given
            Long facilityId = 1L;
            MarDashboardResponse mockResponse = TestDataFactory.createDashboardResponse();

            when(marService.getDashboard(eq(facilityId), any(MarDashboardRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/mar/dashboard")
                            .header("X-Facility-ID", facilityId)
                            .param("shift", "DAY")
                            .param("status", "ALL")
                            .param("date", LocalDate.now().toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Success"))
                    .andExpect(jsonPath("$.data.shift").value("DAY"))
                    .andExpect(jsonPath("$.data.summary.pending").value(10))
                    .andExpect(jsonPath("$.data.summary.completed").value(25))
                    .andExpect(jsonPath("$.data.summary.overdue").value(3))
                    .andExpect(jsonPath("$.data.summary.held").value(1))
                    .andExpect(jsonPath("$.data.summary.notAvailable").value(1))
                    .andExpect(jsonPath("$.data.medPassList").isArray())
                    .andExpect(jsonPath("$.data.medPassList.length()").value(2))
                    .andExpect(jsonPath("$.data.globalAllergyAlerts").isArray());

            verify(marService, times(1)).getDashboard(eq(facilityId), any(MarDashboardRequest.class));
        }

        @Test
        @DisplayName("TC02 - Success: Should use default date when not provided")
        void getDashboard_NoDate_ShouldUseToday() throws Exception {
            // Given
            Long facilityId = 1L;
            MarDashboardResponse mockResponse = TestDataFactory.createDashboardResponse();

            when(marService.getDashboard(eq(facilityId), any(MarDashboardRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/mar/dashboard")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200));

            verify(marService, times(1)).getDashboard(eq(facilityId), any(MarDashboardRequest.class));
        }

        @Test
        @DisplayName("TC03 - Success: Should use default shift when not provided")
        void getDashboard_NoShift_ShouldUseDefaultShift() throws Exception {
            // Given
            Long facilityId = 1L;
            MarDashboardResponse mockResponse = TestDataFactory.createDashboardResponse();

            when(marService.getDashboard(eq(facilityId), any(MarDashboardRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/mar/dashboard")
                            .header("X-Facility-ID", facilityId)
                            .param("status", "ALL")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("TC04 - Success: With EVENING shift and PENDING status")
        void getDashboard_EveningShiftPendingStatus_ShouldSucceed() throws Exception {
            // Given
            Long facilityId = 1L;
            MarDashboardResponse mockResponse = TestDataFactory.createDashboardResponse();

            when(marService.getDashboard(eq(facilityId), any(MarDashboardRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/mar/dashboard")
                            .header("X-Facility-ID", facilityId)
                            .param("shift", "EVENING")
                            .param("status", "PENDING")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("TC05 - Error: Missing Facility ID should return error")
        void getDashboard_NoFacilityId_ShouldReturnError() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/mar/dashboard")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC06 - Error: Facility not found should return error")
        void getDashboard_FacilityNotFound_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 999L;

            when(marService.getDashboard(eq(facilityId), any(MarDashboardRequest.class)))
                    .thenThrow(new AppException(ErrorCode.FACILITY_NOT_FOUND));

            // When & Then
            mockMvc.perform(get("/api/v1/mar/dashboard")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("TC07 - Error: Invalid date format should return error")
        void getDashboard_InvalidDateFormat_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;

            // When & Then
            mockMvc.perform(get("/api/v1/mar/dashboard")
                            .header("X-Facility-ID", facilityId)
                            .param("date", "invalid-date")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC08 - Error: Service throws runtime exception should propagate")
        void getDashboard_ServiceThrowsRuntimeException_ShouldPropagate() throws Exception {
            // Given
            Long facilityId = 1L;

            when(marService.getDashboard(eq(facilityId), any(MarDashboardRequest.class)))
                    .thenThrow(new RuntimeException("Unexpected error"));

            // When & Then
            mockMvc.perform(get("/api/v1/mar/dashboard")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("TC09 - Success: With NIGHT shift and OVERDUE status")
        void getDashboard_NightShiftOverdueStatus_ShouldSucceed() throws Exception {
            // Given
            Long facilityId = 1L;
            MarDashboardResponse mockResponse = TestDataFactory.createDashboardResponse();

            when(marService.getDashboard(eq(facilityId), any(MarDashboardRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/mar/dashboard")
                            .header("X-Facility-ID", facilityId)
                            .param("shift", "NIGHT")
                            .param("status", "OVERDUE")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("TC10 - Success: With COMPLETED status filter")
        void getDashboard_CompletedStatus_ShouldSucceed() throws Exception {
            // Given
            Long facilityId = 1L;
            MarDashboardResponse mockResponse = TestDataFactory.createDashboardResponse();

            when(marService.getDashboard(eq(facilityId), any(MarDashboardRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/mar/dashboard")
                            .header("X-Facility-ID", facilityId)
                            .param("status", "COMPLETED")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    // ==================== API 2: Get Resident MAR Tests ====================

    @Nested
    @DisplayName("API 2: Get Resident MAR - Tests")
    class GetResidentMarTests {

        @Test
        @DisplayName("TC11 - Success: Should return full MAR for resident")
        void getResidentMar_Success_ShouldReturnFullMAR() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;
            MarResidentResponse mockResponse = TestDataFactory.createMarResidentResponse();

            when(marService.getResidentMar(eq(facilityId), eq(residentId), any(), any(), any(), any()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/mar/residents/{residentId}/mar", residentId)
                            .header("X-Facility-ID", facilityId)
                            .param("dateRange", "THIS_WEEK")
                            .param("statusFilter", "ALL")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data.resident.id").value(1L))
                    .andExpect(jsonPath("$.data.resident.fullName").value("John Smith"))
                    .andExpect(jsonPath("$.data.resident.roomNumber").value("208"))
                    .andExpect(jsonPath("$.data.summaryStats.totalScheduled").value(21))
                    .andExpect(jsonPath("$.data.summaryStats.administered").value(18))
                    .andExpect(jsonPath("$.data.medicationGrid").isArray());
        }

        @Test
        @DisplayName("TC12 - Success: With specific date range")
        void getResidentMar_WithDateRange_ShouldPassDates() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();
            MarResidentResponse mockResponse = TestDataFactory.createMarResidentResponse();

            when(marService.getResidentMar(eq(facilityId), eq(residentId), any(), any(), any(), any()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/mar/residents/{residentId}/mar", residentId)
                            .header("X-Facility-ID", facilityId)
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("TC13 - Success: With status filter ADMINISTERED")
        void getResidentMar_StatusFilterAdministered_ShouldSucceed() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;
            MarResidentResponse mockResponse = TestDataFactory.createMarResidentResponse();

            when(marService.getResidentMar(eq(facilityId), eq(residentId), any(), any(), any(), any()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/mar/residents/{residentId}/mar", residentId)
                            .header("X-Facility-ID", facilityId)
                            .param("statusFilter", "ADMINISTERED")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("TC14 - Success: With status filter HELD")
        void getResidentMar_StatusFilterHeld_ShouldSucceed() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;
            MarResidentResponse mockResponse = TestDataFactory.createMarResidentResponse();

            when(marService.getResidentMar(eq(facilityId), eq(residentId), any(), any(), any(), any()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/mar/residents/{residentId}/mar", residentId)
                            .header("X-Facility-ID", facilityId)
                            .param("statusFilter", "HELD")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("TC15 - Success: With status filter OVERRIDE")
        void getResidentMar_StatusFilterOverride_ShouldSucceed() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;
            MarResidentResponse mockResponse = TestDataFactory.createMarResidentResponse();

            when(marService.getResidentMar(eq(facilityId), eq(residentId), any(), any(), any(), any()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/mar/residents/{residentId}/mar", residentId)
                            .header("X-Facility-ID", facilityId)
                            .param("statusFilter", "OVERRIDE")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("TC16 - Success: With date range THIS_MONTH")
        void getResidentMar_DateRangeThisMonth_ShouldSucceed() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;
            MarResidentResponse mockResponse = TestDataFactory.createMarResidentResponse();

            when(marService.getResidentMar(eq(facilityId), eq(residentId), any(), any(), any(), any()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/mar/residents/{residentId}/mar", residentId)
                            .header("X-Facility-ID", facilityId)
                            .param("dateRange", "THIS_MONTH")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("TC17 - Error: Resident not found should return 404")
        void getResidentMar_ResidentNotFound_ShouldReturn404() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 999L;

            when(marService.getResidentMar(eq(facilityId), eq(residentId), any(), any(), any(), any()))
                    .thenThrow(new AppException(ErrorCode.MAR_RESIDENT_NOT_FOUND));

            // When & Then
            mockMvc.perform(get("/api/v1/mar/residents/{residentId}/mar", residentId)
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("TC18 - Error: Invalid date range should return 400")
        void getResidentMar_InvalidDateRange_ShouldReturn400() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;

            when(marService.getResidentMar(eq(facilityId), eq(residentId), any(), any(), any(), any()))
                    .thenThrow(new AppException(ErrorCode.MAR_INVALID_DATE_RANGE));

            // When & Then
            mockMvc.perform(get("/api/v1/mar/residents/{residentId}/mar", residentId)
                            .header("X-Facility-ID", facilityId)
                            .param("startDate", "2026-07-20")
                            .param("endDate", "2026-07-10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC19 - Error: No active orders should return 404")
        void getResidentMar_NoActiveOrders_ShouldReturn404() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;

            when(marService.getResidentMar(eq(facilityId), eq(residentId), any(), any(), any(), any()))
                    .thenThrow(new AppException(ErrorCode.MAR_NO_ACTIVE_ORDERS));

            // When & Then
            mockMvc.perform(get("/api/v1/mar/residents/{residentId}/mar", residentId)
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("TC20 - Error: Missing resident ID should return error")
        void getResidentMar_NoResidentId_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;

            // When & Then - path variable missing will cause 404
            mockMvc.perform(get("/api/v1/mar/residents//mar")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("TC21 - Error: Invalid status filter should return error")
        void getResidentMar_InvalidStatusFilter_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;

            when(marService.getResidentMar(eq(facilityId), eq(residentId), any(), any(), any(), any()))
                    .thenThrow(new AppException(ErrorCode.INVALID_PARAMETER));

            // When & Then
            mockMvc.perform(get("/api/v1/mar/residents/{residentId}/mar", residentId)
                            .header("X-Facility-ID", facilityId)
                            .param("statusFilter", "INVALID_STATUS")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== API 3: Print MAR Tests ====================

    @Nested
    @DisplayName("API 3: Print MAR - Tests")
    class PrintMarTests {

        @Test
        @DisplayName("TC22 - Success: Should return PDF file with correct headers")
        void printMar_Success_ShouldReturnPDF() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;
            byte[] pdfData = "Mock PDF Content".getBytes();

            when(marService.printMar(eq(facilityId), eq(residentId), any(), any()))
                    .thenReturn(pdfData);

            // When & Then
            mockMvc.perform(get("/api/v1/mar/residents/{residentId}/mar/print", residentId)
                            .header("X-Facility-ID", facilityId)
                            .accept(MediaType.APPLICATION_PDF))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                    .andExpect(header().string("Content-Disposition", "attachment; filename=MAR_Resident_1_" + LocalDate.now() + ".pdf"))
                    .andExpect(content().bytes(pdfData));
        }

        @Test
        @DisplayName("TC23 - Success: With date range should return PDF")
        void printMar_WithDateRange_ShouldReturnPDF() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();
            byte[] pdfData = "Mock PDF Content".getBytes();

            when(marService.printMar(eq(facilityId), eq(residentId), any(), any()))
                    .thenReturn(pdfData);

            // When & Then
            mockMvc.perform(get("/api/v1/mar/residents/{residentId}/mar/print", residentId)
                            .header("X-Facility-ID", facilityId)
                            .param("startDate", startDate.toString())
                            .param("endDate", endDate.toString())
                            .accept(MediaType.APPLICATION_PDF))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF));
        }

        @Test
        @DisplayName("TC24 - Success: With only start date")
        void printMar_WithStartDateOnly_ShouldReturnPDF() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;
            LocalDate startDate = LocalDate.now().minusDays(7);
            byte[] pdfData = "Mock PDF Content".getBytes();

            when(marService.printMar(eq(facilityId), eq(residentId), any(), any()))
                    .thenReturn(pdfData);

            // When & Then
            mockMvc.perform(get("/api/v1/mar/residents/{residentId}/mar/print", residentId)
                            .header("X-Facility-ID", facilityId)
                            .param("startDate", startDate.toString())
                            .accept(MediaType.APPLICATION_PDF))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("TC25 - Error: Resident not found should return 404")
        void printMar_ResidentNotFound_ShouldReturn404() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 999L;

            when(marService.printMar(eq(facilityId), eq(residentId), any(), any()))
                    .thenThrow(new AppException(ErrorCode.MAR_RESIDENT_NOT_FOUND));

            // When & Then
            mockMvc.perform(get("/api/v1/mar/residents/{residentId}/mar/print", residentId)
                            .header("X-Facility-ID", facilityId)
                            .accept(MediaType.APPLICATION_PDF))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("TC26 - Error: PDF generation failed should return 500")
        void printMar_PDFGenerationFailed_ShouldReturn500() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;

            when(marService.printMar(eq(facilityId), eq(residentId), any(), any()))
                    .thenThrow(new AppException(ErrorCode.MAR_PDF_GENERATION_FAILED));

            // When & Then
            mockMvc.perform(get("/api/v1/mar/residents/{residentId}/mar/print", residentId)
                            .header("X-Facility-ID", facilityId)
                            .accept(MediaType.APPLICATION_PDF))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("TC27 - Error: Missing facility ID should return error")
        void printMar_NoFacilityId_ShouldReturnError() throws Exception {
            // Given
            Long residentId = 1L;

            // When & Then
            mockMvc.perform(get("/api/v1/mar/residents/{residentId}/mar/print", residentId)
                            .accept(MediaType.APPLICATION_PDF))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC28 - Error: Invalid date format should return error")
        void printMar_InvalidDateFormat_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;

            // When & Then
            mockMvc.perform(get("/api/v1/mar/residents/{residentId}/mar/print", residentId)
                            .header("X-Facility-ID", facilityId)
                            .param("startDate", "invalid-date")
                            .accept(MediaType.APPLICATION_PDF))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== API 4: Get Shift Schedule Tests ====================

    @Nested
    @DisplayName("API 4: Get Shift Medication Schedule - Tests")
    class GetShiftScheduleTests {

        @Test
        @DisplayName("TC29 - Success: Should return shift schedule for DAY shift")
        void getShiftSchedule_DayShift_Success() throws Exception {
            // Given
            Long facilityId = 1L;
            String shift = "DAY";
            MarScheduleShiftResponse mockResponse = TestDataFactory.createShiftScheduleResponse(shift);

            when(marService.getShiftSchedule(eq(facilityId), eq(shift), any(LocalDate.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/mar/schedule/shift")
                            .header("X-Facility-ID", facilityId)
                            .param("shift", shift)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data.shift").value("DAY"))
                    .andExpect(jsonPath("$.data.schedule").isArray());
        }

        @Test
        @DisplayName("TC30 - Success: Should return shift schedule for EVENING shift")
        void getShiftSchedule_EveningShift_Success() throws Exception {
            // Given
            Long facilityId = 1L;
            String shift = "EVENING";
            MarScheduleShiftResponse mockResponse = TestDataFactory.createShiftScheduleResponse(shift);

            when(marService.getShiftSchedule(eq(facilityId), eq(shift), any(LocalDate.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/mar/schedule/shift")
                            .header("X-Facility-ID", facilityId)
                            .param("shift", shift)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.shift").value("EVENING"));
        }

        @Test
        @DisplayName("TC31 - Success: Should return shift schedule for NIGHT shift")
        void getShiftSchedule_NightShift_Success() throws Exception {
            // Given
            Long facilityId = 1L;
            String shift = "NIGHT";
            MarScheduleShiftResponse mockResponse = TestDataFactory.createShiftScheduleResponse(shift);

            when(marService.getShiftSchedule(eq(facilityId), eq(shift), any(LocalDate.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/mar/schedule/shift")
                            .header("X-Facility-ID", facilityId)
                            .param("shift", shift)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.shift").value("NIGHT"));
        }

        @Test
        @DisplayName("TC32 - Success: With date parameter")
        void getShiftSchedule_WithDate_Success() throws Exception {
            // Given
            Long facilityId = 1L;
            String shift = "DAY";
            LocalDate date = LocalDate.now().minusDays(1);
            MarScheduleShiftResponse mockResponse = TestDataFactory.createShiftScheduleResponse(shift);

            when(marService.getShiftSchedule(eq(facilityId), eq(shift), eq(date)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/mar/schedule/shift")
                            .header("X-Facility-ID", facilityId)
                            .param("shift", shift)
                            .param("date", date.toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.date").value(date.toString()));
        }

        @Test
        @DisplayName("TC33 - Success: Empty schedule when no medications")
        void getShiftSchedule_EmptySchedule_ShouldReturnEmptyList() throws Exception {
            // Given
            Long facilityId = 1L;
            String shift = "DAY";
            MarScheduleShiftResponse mockResponse = TestDataFactory.createEmptyShiftScheduleResponse(shift);

            when(marService.getShiftSchedule(eq(facilityId), eq(shift), any(LocalDate.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/mar/schedule/shift")
                            .header("X-Facility-ID", facilityId)
                            .param("shift", shift)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.schedule").isEmpty());
        }

        @Test
        @DisplayName("TC34 - Error: Invalid shift should return 400")
        void getShiftSchedule_InvalidShift_ShouldReturn400() throws Exception {
            // Given
            Long facilityId = 1L;
            String invalidShift = "INVALID";

            // When & Then
            mockMvc.perform(get("/api/v1/mar/schedule/shift")
                            .header("X-Facility-ID", facilityId)
                            .param("shift", invalidShift)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC35 - Error: Missing shift parameter should return 400")
        void getShiftSchedule_NoShift_ShouldReturn400() throws Exception {
            // Given
            Long facilityId = 1L;

            // When & Then
            mockMvc.perform(get("/api/v1/mar/schedule/shift")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC36 - Error: Facility not found should return error")
        void getShiftSchedule_FacilityNotFound_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 999L;
            String shift = "DAY";

            when(marService.getShiftSchedule(eq(facilityId), eq(shift), any(LocalDate.class)))
                    .thenThrow(new AppException(ErrorCode.FACILITY_NOT_FOUND));

            // When & Then
            mockMvc.perform(get("/api/v1/mar/schedule/shift")
                            .header("X-Facility-ID", facilityId)
                            .param("shift", shift)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("TC37 - Error: Invalid date format should return error")
        void getShiftSchedule_InvalidDateFormat_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            String shift = "DAY";

            // When & Then
            mockMvc.perform(get("/api/v1/mar/schedule/shift")
                            .header("X-Facility-ID", facilityId)
                            .param("shift", shift)
                            .param("date", "invalid-date")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC38 - Error: Missing facility ID should return error")
        void getShiftSchedule_NoFacilityId_ShouldReturnError() throws Exception {
            // Given
            String shift = "DAY";

            // When & Then
            mockMvc.perform(get("/api/v1/mar/schedule/shift")
                            .param("shift", shift)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }
}