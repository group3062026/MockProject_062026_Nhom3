package com.nguyenquyen.mockproject_062026_group3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nguyenquyen.mockproject_062026_group3.dto.request.CreateMedicationOrderRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.DiscontinueMedicationOrderRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.response.*;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.service.MedicationOrderService;
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

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicationOrderController Unit Tests - Phần 3")
class MedicationOrderControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private MedicationOrderService medicationOrderService;

    @InjectMocks
    private MedicationOrderController medicationOrderController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(medicationOrderController).build();
        objectMapper = new ObjectMapper();
    }

    // ==================== API 11: Get Medication Orders Tests ====================

    @Nested
    @DisplayName("API 11: Get Medication Orders - Tests")
    class GetMedicationOrdersTests {

        @Test
        @DisplayName("TC01 - Success: Should return list of medication orders")
        void getMedicationOrders_Success_ShouldReturnList() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;
            String status = "ACTIVE";
            Integer page = 1;
            Integer limit = 20;

            MedicationOrderListResponse mockResponse = TestDataFactory.createMedicationOrderListResponse();

            when(medicationOrderService.getMedicationOrders(eq(facilityId), any()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/medication-orders")
                            .header("X-Facility-ID", facilityId)
                            .param("residentId", String.valueOf(residentId))
                            .param("status", status)
                            .param("page", String.valueOf(page))
                            .param("limit", String.valueOf(limit))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data.medicationOrders").isArray())
                    .andExpect(jsonPath("$.data.metadata.currentPage").value(1))
                    .andExpect(jsonPath("$.data.metadata.hasNext").value(true));

            verify(medicationOrderService, times(1)).getMedicationOrders(eq(facilityId), any());
        }

        @Test
        @DisplayName("TC02 - Success: With search filter")
        void getMedicationOrders_WithSearch_ShouldReturnFilteredList() throws Exception {
            // Given
            Long facilityId = 1L;
            String search = "Aspirin";

            MedicationOrderListResponse mockResponse = TestDataFactory.createMedicationOrderListResponse();

            when(medicationOrderService.getMedicationOrders(eq(facilityId), any()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/medication-orders")
                            .header("X-Facility-ID", facilityId)
                            .param("search", search)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200));

            verify(medicationOrderService, times(1)).getMedicationOrders(eq(facilityId), any());
        }

        @Test
        @DisplayName("TC03 - Success: With DISCONTINUED status")
        void getMedicationOrders_DiscontinuedStatus_ShouldReturnList() throws Exception {
            // Given
            Long facilityId = 1L;
            String status = "DISCONTINUED";

            MedicationOrderListResponse mockResponse = TestDataFactory.createMedicationOrderListResponse();

            when(medicationOrderService.getMedicationOrders(eq(facilityId), any()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/medication-orders")
                            .header("X-Facility-ID", facilityId)
                            .param("status", status)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(medicationOrderService, times(1)).getMedicationOrders(eq(facilityId), any());
        }

        @Test
        @DisplayName("TC04 - Success: With ON_HOLD status")
        void getMedicationOrders_OnHoldStatus_ShouldReturnList() throws Exception {
            // Given
            Long facilityId = 1L;
            String status = "ON_HOLD";

            MedicationOrderListResponse mockResponse = TestDataFactory.createMedicationOrderListResponse();

            when(medicationOrderService.getMedicationOrders(eq(facilityId), any()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/medication-orders")
                            .header("X-Facility-ID", facilityId)
                            .param("status", status)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("TC05 - Success: Without filters should return all")
        void getMedicationOrders_NoFilters_ShouldReturnAll() throws Exception {
            // Given
            Long facilityId = 1L;

            MedicationOrderListResponse mockResponse = TestDataFactory.createMedicationOrderListResponse();

            when(medicationOrderService.getMedicationOrders(eq(facilityId), any()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/medication-orders")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(medicationOrderService, times(1)).getMedicationOrders(eq(facilityId), any());
        }

        @Test
        @DisplayName("TC06 - Error: Missing facility ID should return error")
        void getMedicationOrders_NoFacilityId_ShouldReturnError() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/medication-orders")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC07 - Error: Invalid status should return error")
        void getMedicationOrders_InvalidStatus_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            String invalidStatus = "INVALID";

            when(medicationOrderService.getMedicationOrders(eq(facilityId), any()))
                    .thenThrow(new AppException(ErrorCode.INVALID_PARAMETER));

            // When & Then
            mockMvc.perform(get("/api/v1/medication-orders")
                            .header("X-Facility-ID", facilityId)
                            .param("status", invalidStatus)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== API 12: Get Medication Order Detail Tests ====================

    @Nested
    @DisplayName("API 12: Get Medication Order Detail - Tests")
    class GetMedicationOrderDetailTests {

        @Test
        @DisplayName("TC08 - Success: Should return order detail")
        void getMedicationOrderDetail_Success_ShouldReturnDetail() throws Exception {
            // Given
            Long facilityId = 1L;
            Long orderId = 1L;

            MedicationOrderDetailResponse mockResponse = TestDataFactory.createMedicationOrderDetailResponse();

            when(medicationOrderService.getMedicationOrderDetail(eq(facilityId), eq(orderId)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/medication-orders/{orderId}", orderId)
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.drugName").value("Aspirin"))
                    .andExpect(jsonPath("$.data.schedules").isArray())
                    .andExpect(jsonPath("$.data.recentLogs").isArray());

            verify(medicationOrderService, times(1)).getMedicationOrderDetail(eq(facilityId), eq(orderId));
        }

        @Test
        @DisplayName("TC09 - Error: Order not found should return 404")
        void getMedicationOrderDetail_OrderNotFound_ShouldReturn404() throws Exception {
            // Given
            Long facilityId = 1L;
            Long orderId = 999L;

            when(medicationOrderService.getMedicationOrderDetail(eq(facilityId), eq(orderId)))
                    .thenThrow(new AppException(ErrorCode.MAR_ORDER_NOT_FOUND));

            // When & Then
            mockMvc.perform(get("/api/v1/medication-orders/{orderId}", orderId)
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("TC10 - Error: Missing facility ID should return error")
        void getMedicationOrderDetail_NoFacilityId_ShouldReturnError() throws Exception {
            // Given
            Long orderId = 1L;

            // When & Then
            mockMvc.perform(get("/api/v1/medication-orders/{orderId}", orderId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== API 13: Create Medication Order Tests ====================

    @Nested
    @DisplayName("API 13: Create Medication Order - Tests")
    class CreateMedicationOrderTests {

        @Test
        @DisplayName("TC11 - Success: Should create medication order")
        void createMedicationOrder_Success_ShouldReturnCreated() throws Exception {
            // Given
            Long facilityId = 1L;
            CreateMedicationOrderRequest request = TestDataFactory.createCreateMedicationOrderRequest(
                    1L, "Lisinopril", "10 mg", "ORAL", "Once daily",
                    false, 4L, "Monitor blood pressure daily",
                    Arrays.asList("08:00:00"));

            CreateMedicationOrderResponse mockResponse = TestDataFactory.createCreateMedicationOrderResponse();

            when(medicationOrderService.createMedicationOrder(eq(facilityId), any(CreateMedicationOrderRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/medication-orders")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.statusCode").value(201))
                    .andExpect(jsonPath("$.data.id").value(25L))
                    .andExpect(jsonPath("$.data.drugName").value("Lisinopril"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.data.schedules").isArray());

            verify(medicationOrderService, times(1)).createMedicationOrder(eq(facilityId), any(CreateMedicationOrderRequest.class));
        }

        @Test
        @DisplayName("TC12 - Success: With controlled substance")
        void createMedicationOrder_ControlledSubstance_ShouldSucceed() throws Exception {
            // Given
            Long facilityId = 1L;
            CreateMedicationOrderRequest request = TestDataFactory.createCreateMedicationOrderRequest(
                    1L, "Morphine", "5 mg", "ORAL", "Every 6 hours",
                    true, 4L, "Monitor for respiratory depression",
                    Arrays.asList("08:00:00", "14:00:00", "20:00:00"));

            CreateMedicationOrderResponse mockResponse = TestDataFactory.createCreateMedicationOrderResponse();

            when(medicationOrderService.createMedicationOrder(eq(facilityId), any(CreateMedicationOrderRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/medication-orders")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(medicationOrderService, times(1)).createMedicationOrder(eq(facilityId), any(CreateMedicationOrderRequest.class));
        }

        @Test
        @DisplayName("TC13 - Success: With multiple schedules")
        void createMedicationOrder_MultipleSchedules_ShouldSucceed() throws Exception {
            // Given
            Long facilityId = 1L;
            CreateMedicationOrderRequest request = TestDataFactory.createCreateMedicationOrderRequest(
                    1L, "Metformin", "500 mg", "ORAL", "Twice Daily",
                    false, 4L, "Take with meals",
                    Arrays.asList("08:00:00", "20:00:00"));

            CreateMedicationOrderResponse mockResponse = TestDataFactory.createCreateMedicationOrderResponse();

            when(medicationOrderService.createMedicationOrder(eq(facilityId), any(CreateMedicationOrderRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/medication-orders")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("TC14 - Error: Missing resident ID should return error")
        void createMedicationOrder_NoResidentId_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            CreateMedicationOrderRequest request = TestDataFactory.createCreateMedicationOrderRequest(
                    null, "Lisinopril", "10 mg", "ORAL", "Once daily",
                    false, 4L, "Monitor blood pressure daily",
                    Arrays.asList("08:00:00"));

            // When & Then
            mockMvc.perform(post("/api/v1/medication-orders")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC15 - Error: Missing prescribed by should return error")
        void createMedicationOrder_NoPrescribedBy_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            CreateMedicationOrderRequest request = TestDataFactory.createCreateMedicationOrderRequest(
                    1L, "Lisinopril", "10 mg", "ORAL", "Once daily",
                    false, null, "Monitor blood pressure daily",
                    Arrays.asList("08:00:00"));

            when(medicationOrderService.createMedicationOrder(eq(facilityId), any(CreateMedicationOrderRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_PRESCRIBER_NOT_FOUND));

            // When & Then
            mockMvc.perform(post("/api/v1/medication-orders")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("TC16 - Error: Duplicate order should return conflict")
        void createMedicationOrder_Duplicate_ShouldReturnConflict() throws Exception {
            // Given
            Long facilityId = 1L;
            CreateMedicationOrderRequest request = TestDataFactory.createCreateMedicationOrderRequest(
                    1L, "Lisinopril", "10 mg", "ORAL", "Once daily",
                    false, 4L, "Monitor blood pressure daily",
                    Arrays.asList("08:00:00"));

            when(medicationOrderService.createMedicationOrder(eq(facilityId), any(CreateMedicationOrderRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_ORDER_DUPLICATE));

            // When & Then
            mockMvc.perform(post("/api/v1/medication-orders")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("TC17 - Error: Resident not found should return 404")
        void createMedicationOrder_ResidentNotFound_ShouldReturn404() throws Exception {
            // Given
            Long facilityId = 1L;
            CreateMedicationOrderRequest request = TestDataFactory.createCreateMedicationOrderRequest(
                    999L, "Lisinopril", "10 mg", "ORAL", "Once daily",
                    false, 4L, "Monitor blood pressure daily",
                    Arrays.asList("08:00:00"));

            when(medicationOrderService.createMedicationOrder(eq(facilityId), any(CreateMedicationOrderRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_RESIDENT_NOT_FOUND));

            // When & Then
            mockMvc.perform(post("/api/v1/medication-orders")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    // ==================== API 14: Discontinue Medication Order Tests ====================

    @Nested
    @DisplayName("API 14: Discontinue Medication Order - Tests")
    class DiscontinueMedicationOrderTests {

        @Test
        @DisplayName("TC18 - Success: Should discontinue order")
        void discontinueMedicationOrder_Success_ShouldReturnDiscontinued() throws Exception {
            // Given
            Long facilityId = 1L;
            Long orderId = 1L;
            DiscontinueMedicationOrderRequest request = TestDataFactory.createDiscontinueMedicationOrderRequest(
                    "Switched to different medication");

            DiscontinueMedicationOrderResponse mockResponse = TestDataFactory.createDiscontinueMedicationOrderResponse();

            when(medicationOrderService.discontinueMedicationOrder(eq(facilityId), eq(orderId), any()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(patch("/api/v1/medication-orders/{orderId}/discontinue", orderId)
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data.status").value("DISCONTINUED"))
                    .andExpect(jsonPath("$.data.discontinueReason").value("Switched to different medication"));

            verify(medicationOrderService, times(1)).discontinueMedicationOrder(eq(facilityId), eq(orderId), any());
        }

        @Test
        @DisplayName("TC19 - Error: Order not found should return 404")
        void discontinueMedicationOrder_OrderNotFound_ShouldReturn404() throws Exception {
            // Given
            Long facilityId = 1L;
            Long orderId = 999L;
            DiscontinueMedicationOrderRequest request = TestDataFactory.createDiscontinueMedicationOrderRequest(
                    "Switched to different medication");

            when(medicationOrderService.discontinueMedicationOrder(eq(facilityId), eq(orderId), any()))
                    .thenThrow(new AppException(ErrorCode.MAR_ORDER_NOT_FOUND));

            // When & Then
            mockMvc.perform(patch("/api/v1/medication-orders/{orderId}/discontinue", orderId)
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("TC20 - Error: Already discontinued should return error")
        void discontinueMedicationOrder_AlreadyDiscontinued_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            Long orderId = 1L;
            DiscontinueMedicationOrderRequest request = TestDataFactory.createDiscontinueMedicationOrderRequest(
                    "Switched to different medication");

            when(medicationOrderService.discontinueMedicationOrder(eq(facilityId), eq(orderId), any()))
                    .thenThrow(new AppException(ErrorCode.MAR_ORDER_ALREADY_DISCONTINUED));

            // When & Then
            mockMvc.perform(patch("/api/v1/medication-orders/{orderId}/discontinue", orderId)
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC21 - Error: Has pending doses should return error")
        void discontinueMedicationOrder_HasPendingDoses_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            Long orderId = 1L;
            DiscontinueMedicationOrderRequest request = TestDataFactory.createDiscontinueMedicationOrderRequest(
                    "Switched to different medication");

            when(medicationOrderService.discontinueMedicationOrder(eq(facilityId), eq(orderId), any()))
                    .thenThrow(new AppException(ErrorCode.MAR_ORDER_HAS_PENDING_DOSES));

            // When & Then
            mockMvc.perform(patch("/api/v1/medication-orders/{orderId}/discontinue", orderId)
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== API 15: Get Resident Pending Medications Tests ====================

    @Nested
    @DisplayName("API 15: Get Resident Pending Medications - Tests")
    class GetResidentPendingMedicationsTests {

        @Test
        @DisplayName("TC22 - Success: Should return pending medications")
        void getResidentPendingMedications_Success_ShouldReturnList() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;
            String time = "08:00";

            PendingMedicationResponse mockResponse = TestDataFactory.createPendingMedicationResponse();

            when(medicationOrderService.getResidentPendingMedications(eq(facilityId), eq(residentId), eq(time)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/mar/med-pass/resident/{residentId}/pending", residentId)
                            .header("X-Facility-ID", facilityId)
                            .param("time", time)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data.residentId").value(1L))
                    .andExpect(jsonPath("$.data.pendingMedications").isArray())
                    .andExpect(jsonPath("$.data.pendingMedications.length()").value(2));

            verify(medicationOrderService, times(1)).getResidentPendingMedications(eq(facilityId), eq(residentId), eq(time));
        }

        @Test
        @DisplayName("TC23 - Success: Without time parameter should use current time")
        void getResidentPendingMedications_NoTime_ShouldUseCurrentTime() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;

            PendingMedicationResponse mockResponse = TestDataFactory.createPendingMedicationResponse();

            when(medicationOrderService.getResidentPendingMedications(eq(facilityId), eq(residentId), isNull()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/mar/med-pass/resident/{residentId}/pending", residentId)
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(medicationOrderService, times(1)).getResidentPendingMedications(eq(facilityId), eq(residentId), isNull());
        }

        @Test
        @DisplayName("TC24 - Success: Empty pending medications")
        void getResidentPendingMedications_Empty_ShouldReturnEmptyList() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;

            PendingMedicationResponse mockResponse = TestDataFactory.createEmptyPendingMedicationResponse();

            when(medicationOrderService.getResidentPendingMedications(eq(facilityId), eq(residentId), isNull()))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(get("/api/v1/mar/med-pass/resident/{residentId}/pending", residentId)
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.pendingMedications").isEmpty());

            verify(medicationOrderService, times(1)).getResidentPendingMedications(eq(facilityId), eq(residentId), isNull());
        }

        @Test
        @DisplayName("TC25 - Error: Resident not found should return 404")
        void getResidentPendingMedications_ResidentNotFound_ShouldReturn404() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 999L;

            when(medicationOrderService.getResidentPendingMedications(eq(facilityId), eq(residentId), isNull()))
                    .thenThrow(new AppException(ErrorCode.MAR_RESIDENT_NOT_FOUND));

            // When & Then
            mockMvc.perform(get("/api/v1/mar/med-pass/resident/{residentId}/pending", residentId)
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("TC26 - Error: No active orders should return 404")
        void getResidentPendingMedications_NoActiveOrders_ShouldReturn404() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;

            when(medicationOrderService.getResidentPendingMedications(eq(facilityId), eq(residentId), isNull()))
                    .thenThrow(new AppException(ErrorCode.MAR_RESIDENT_NO_ACTIVE_ORDERS));

            // When & Then
            mockMvc.perform(get("/api/v1/mar/med-pass/resident/{residentId}/pending", residentId)
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("TC27 - Error: Invalid time format should return error")
        void getResidentPendingMedications_InvalidTime_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            Long residentId = 1L;
            String invalidTime = "invalid";

            when(medicationOrderService.getResidentPendingMedications(eq(facilityId), eq(residentId), eq(invalidTime)))
                    .thenThrow(new AppException(ErrorCode.INVALID_PARAMETER));

            // When & Then
            mockMvc.perform(get("/api/v1/mar/med-pass/resident/{residentId}/pending", residentId)
                            .header("X-Facility-ID", facilityId)
                            .param("time", invalidTime)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }
}