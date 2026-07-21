package com.nguyenquyen.mockproject_062026_group3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nguyenquyen.mockproject_062026_group3.dto.request.BatchAdministerRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.RegenerateSchedulesRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.response.BatchAdministerResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.response.RegenerateSchedulesResponse;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.service.BatchService;
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
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BatchController Unit Tests - Phần 4")
class BatchControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private BatchService batchService;

    @InjectMocks
    private BatchController batchController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(batchController).build();
        objectMapper = new ObjectMapper();
    }

    // ==================== API 16: Batch Administer Tests ====================

    @Nested
    @DisplayName("API 16: Batch Administer Medications - Tests")
    class BatchAdministerTests {

        @Test
        @DisplayName("TC01 - Success: Batch administer all medications")
        void batchAdminister_AllSuccess_ShouldReturn200() throws Exception {
            // Given
            Long facilityId = 1L;
            BatchAdministerRequest request = TestDataFactory.createBatchAdministerRequest(
                    "MP-20260717-0001", 1L,
                    Arrays.asList(1L, 2L, 3L),
                    Arrays.asList(1L, 2L, 3L),
                    8L, "All medications administered together");

            BatchAdministerResponse mockResponse = TestDataFactory.createBatchAdministerResponseAllSuccess();

            when(batchService.batchAdminister(eq(facilityId), any(BatchAdministerRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/batch-administer")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data.total").value(3))
                    .andExpect(jsonPath("$.data.administered").value(3))
                    .andExpect(jsonPath("$.data.failed").value(0))
                    .andExpect(jsonPath("$.data.logs").isArray())
                    .andExpect(jsonPath("$.data.logs.length()").value(3));

            verify(batchService, times(1)).batchAdminister(eq(facilityId), any(BatchAdministerRequest.class));
        }

        @Test
        @DisplayName("TC02 - Success: Batch with controlled substances and witness")
        void batchAdminister_ControlledWithWitness_ShouldSucceed() throws Exception {
            // Given
            Long facilityId = 1L;
            BatchAdministerRequest request = TestDataFactory.createBatchAdministerRequest(
                    "MP-20260717-0001", 1L,
                    Arrays.asList(2L, 4L), // even IDs = controlled substances
                    Arrays.asList(2L, 4L),
                    8L, "Controlled substances with witness");

            BatchAdministerResponse mockResponse = TestDataFactory.createBatchAdministerResponse(2, 2, 0);

            when(batchService.batchAdminister(eq(facilityId), any(BatchAdministerRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/batch-administer")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.total").value(2))
                    .andExpect(jsonPath("$.data.administered").value(2));

            verify(batchService, times(1)).batchAdminister(eq(facilityId), any(BatchAdministerRequest.class));
        }

        @Test
        @DisplayName("TC03 - Success: Batch with some failures")
        void batchAdminister_PartialFailure_ShouldReturn200WithFailures() throws Exception {
            // Given
            Long facilityId = 1L;
            BatchAdministerRequest request = TestDataFactory.createBatchAdministerRequest(
                    "MP-20260717-0001", 1L,
                    Arrays.asList(1L, 2L, 3L),
                    Arrays.asList(1L, 2L, 3L),
                    8L, "Some medications failed");

            BatchAdministerResponse mockResponse = TestDataFactory.createBatchAdministerResponsePartialFailure();

            when(batchService.batchAdminister(eq(facilityId), any(BatchAdministerRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/batch-administer")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.total").value(3))
                    .andExpect(jsonPath("$.data.administered").value(2))
                    .andExpect(jsonPath("$.data.failed").value(1))
                    .andExpect(jsonPath("$.data.logs[2].status").value("FAILED"))
                    .andExpect(jsonPath("$.data.logs[2].errorMessage").exists());

            verify(batchService, times(1)).batchAdminister(eq(facilityId), any(BatchAdministerRequest.class));
        }

        @Test
        @DisplayName("TC04 - Success: Batch with single medication")
        void batchAdminister_SingleMedication_ShouldSucceed() throws Exception {
            // Given
            Long facilityId = 1L;
            BatchAdministerRequest request = TestDataFactory.createBatchAdministerRequest(
                    "MP-20260717-0001", 1L,
                    Collections.singletonList(1L),
                    Collections.singletonList(1L),
                    null, "Single medication");

            BatchAdministerResponse mockResponse = TestDataFactory.createBatchAdministerResponse(1, 1, 0);

            when(batchService.batchAdminister(eq(facilityId), any(BatchAdministerRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/batch-administer")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.total").value(1))
                    .andExpect(jsonPath("$.data.administered").value(1));

            verify(batchService, times(1)).batchAdminister(eq(facilityId), any(BatchAdministerRequest.class));
        }

        @Test
        @DisplayName("TC05 - Error: Empty order IDs")
        void batchAdminister_EmptyOrderIds_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            BatchAdministerRequest request = TestDataFactory.createBatchAdministerRequest(
                    "MP-20260717-0001", 1L,
                    Collections.emptyList(),
                    Collections.singletonList(1L),
                    null, "Empty order IDs");

            when(batchService.batchAdminister(eq(facilityId), any(BatchAdministerRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_BATCH_EMPTY));

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/batch-administer")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(batchService, times(1)).batchAdminister(eq(facilityId), any(BatchAdministerRequest.class));
        }

        @Test
        @DisplayName("TC06 - Error: Size mismatch between orderIds and scheduleIds")
        void batchAdminister_SizeMismatch_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            BatchAdministerRequest request = TestDataFactory.createBatchAdministerRequest(
                    "MP-20260717-0001", 1L,
                    Arrays.asList(1L, 2L, 3L), // 3 orders
                    Arrays.asList(1L, 2L),      // 2 schedules - mismatch
                    null, "Size mismatch");

            when(batchService.batchAdminister(eq(facilityId), any(BatchAdministerRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_BATCH_SIZE_MISMATCH));

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/batch-administer")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(batchService, times(1)).batchAdminister(eq(facilityId), any(BatchAdministerRequest.class));
        }

        @Test
        @DisplayName("TC07 - Error: Order not found")
        void batchAdminister_OrderNotFound_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            BatchAdministerRequest request = TestDataFactory.createBatchAdministerRequest(
                    "MP-20260717-0001", 1L,
                    Arrays.asList(999L, 2L), // 999 doesn't exist
                    Arrays.asList(1L, 2L),
                    null, "Order not found");

            when(batchService.batchAdminister(eq(facilityId), any(BatchAdministerRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_BATCH_ORDER_NOT_FOUND));

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/batch-administer")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            verify(batchService, times(1)).batchAdminister(eq(facilityId), any(BatchAdministerRequest.class));
        }

        @Test
        @DisplayName("TC08 - Error: Witness required for controlled substances")
        void batchAdminister_WitnessRequired_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            BatchAdministerRequest request = TestDataFactory.createBatchAdministerRequest(
                    "MP-20260717-0001", 1L,
                    Arrays.asList(2L, 4L), // controlled substances
                    Arrays.asList(2L, 4L),
                    null, // No witness
                    "No witness for controlled substances");

            when(batchService.batchAdminister(eq(facilityId), any(BatchAdministerRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_BATCH_WITNESS_REQUIRED));

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/batch-administer")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(batchService, times(1)).batchAdminister(eq(facilityId), any(BatchAdministerRequest.class));
        }

        @Test
        @DisplayName("TC09 - Error: Already administered")
        void batchAdminister_AlreadyAdministered_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            BatchAdministerRequest request = TestDataFactory.createBatchAdministerRequest(
                    "MP-20260717-0001", 1L,
                    Arrays.asList(1L, 2L, 3L),
                    Arrays.asList(1L, 2L, 3L),
                    8L, "Already administered");

            when(batchService.batchAdminister(eq(facilityId), any(BatchAdministerRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_BATCH_ALREADY_ADMINISTERED));

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/batch-administer")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(batchService, times(1)).batchAdminister(eq(facilityId), any(BatchAdministerRequest.class));
        }

        @Test
        @DisplayName("TC10 - Error: Invalid session")
        void batchAdminister_InvalidSession_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            BatchAdministerRequest request = TestDataFactory.createBatchAdministerRequest(
                    null, 1L, // No session ID
                    Arrays.asList(1L, 2L, 3L),
                    Arrays.asList(1L, 2L, 3L),
                    8L, "Invalid session");

            when(batchService.batchAdminister(eq(facilityId), any(BatchAdministerRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_INVALID_SESSION));

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/batch-administer")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(batchService, times(1)).batchAdminister(eq(facilityId), any(BatchAdministerRequest.class));
        }

        @Test
        @DisplayName("TC11 - Error: Missing facility ID")
        void batchAdminister_NoFacilityId_ShouldReturnError() throws Exception {
            // Given
            BatchAdministerRequest request = TestDataFactory.createBatchAdministerRequest(
                    "MP-20260717-0001", 1L,
                    Arrays.asList(1L, 2L, 3L),
                    Arrays.asList(1L, 2L, 3L),
                    8L, "Test");

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/batch-administer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(batchService, never()).batchAdminister(anyLong(), any(BatchAdministerRequest.class));
        }
    }

    // ==================== API 17: Regenerate Schedules Tests ====================

    @Nested
    @DisplayName("API 17: Regenerate Schedules - Tests")
    class RegenerateSchedulesTests {

        @Test
        @DisplayName("TC12 - Success: Regenerate schedules with multiple times")
        void regenerateSchedules_MultipleTimes_ShouldSucceed() throws Exception {
            // Given
            Long facilityId = 1L;
            Long orderId = 1L;
            RegenerateSchedulesRequest request = TestDataFactory.createRegenerateSchedulesRequest(
                    Arrays.asList("08:00:00", "16:00:00", "00:00:00"));

            RegenerateSchedulesResponse mockResponse = TestDataFactory.createRegenerateSchedulesResponse();

            when(batchService.regenerateSchedules(eq(facilityId), eq(orderId), any(RegenerateSchedulesRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/medication-orders/{orderId}/regenerate-schedules", orderId)
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data.orderId").value(1L))
                    .andExpect(jsonPath("$.data.schedules").isArray())
                    .andExpect(jsonPath("$.data.schedules.length()").value(3));

            verify(batchService, times(1)).regenerateSchedules(eq(facilityId), eq(orderId), any(RegenerateSchedulesRequest.class));
        }

        @Test
        @DisplayName("TC13 - Success: Regenerate schedules with single time")
        void regenerateSchedules_SingleTime_ShouldSucceed() throws Exception {
            // Given
            Long facilityId = 1L;
            Long orderId = 1L;
            RegenerateSchedulesRequest request = TestDataFactory.createRegenerateSchedulesRequest(
                    Collections.singletonList("08:00:00"));

            RegenerateSchedulesResponse mockResponse = TestDataFactory.createRegenerateSchedulesResponseWithSingleTime();

            when(batchService.regenerateSchedules(eq(facilityId), eq(orderId), any(RegenerateSchedulesRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/medication-orders/{orderId}/regenerate-schedules", orderId)
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.schedules.length()").value(1));

            verify(batchService, times(1)).regenerateSchedules(eq(facilityId), eq(orderId), any(RegenerateSchedulesRequest.class));
        }

        @Test
        @DisplayName("TC14 - Error: Empty scheduled times")
        void regenerateSchedules_EmptyTimes_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            Long orderId = 1L;
            RegenerateSchedulesRequest request = TestDataFactory.createRegenerateSchedulesRequest(
                    Collections.emptyList());

            when(batchService.regenerateSchedules(eq(facilityId), eq(orderId), any(RegenerateSchedulesRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_REGENERATE_NO_TIMES));

            // When & Then
            mockMvc.perform(post("/api/v1/medication-orders/{orderId}/regenerate-schedules", orderId)
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(batchService, times(1)).regenerateSchedules(eq(facilityId), eq(orderId), any(RegenerateSchedulesRequest.class));
        }

        @Test
        @DisplayName("TC15 - Error: Invalid time format")
        void regenerateSchedules_InvalidTimeFormat_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            Long orderId = 1L;
            RegenerateSchedulesRequest request = TestDataFactory.createRegenerateSchedulesRequest(
                    Arrays.asList("invalid", "08:00:00"));

            when(batchService.regenerateSchedules(eq(facilityId), eq(orderId), any(RegenerateSchedulesRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_REGENERATE_INVALID_TIME));

            // When & Then
            mockMvc.perform(post("/api/v1/medication-orders/{orderId}/regenerate-schedules", orderId)
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(batchService, times(1)).regenerateSchedules(eq(facilityId), eq(orderId), any(RegenerateSchedulesRequest.class));
        }

        @Test
        @DisplayName("TC16 - Error: Order not found")
        void regenerateSchedules_OrderNotFound_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            Long orderId = 999L;
            RegenerateSchedulesRequest request = TestDataFactory.createRegenerateSchedulesRequest(
                    Arrays.asList("08:00:00", "16:00:00"));

            when(batchService.regenerateSchedules(eq(facilityId), eq(orderId), any(RegenerateSchedulesRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_REGENERATE_ORDER_NOT_FOUND));

            // When & Then
            mockMvc.perform(post("/api/v1/medication-orders/{orderId}/regenerate-schedules", orderId)
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());

            verify(batchService, times(1)).regenerateSchedules(eq(facilityId), eq(orderId), any(RegenerateSchedulesRequest.class));
        }

        @Test
        @DisplayName("TC17 - Error: Order not active")
        void regenerateSchedules_OrderNotActive_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            Long orderId = 1L;
            RegenerateSchedulesRequest request = TestDataFactory.createRegenerateSchedulesRequest(
                    Arrays.asList("08:00:00", "16:00:00"));

            when(batchService.regenerateSchedules(eq(facilityId), eq(orderId), any(RegenerateSchedulesRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_REGENERATE_ORDER_NOT_ACTIVE));

            // When & Then
            mockMvc.perform(post("/api/v1/medication-orders/{orderId}/regenerate-schedules", orderId)
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(batchService, times(1)).regenerateSchedules(eq(facilityId), eq(orderId), any(RegenerateSchedulesRequest.class));
        }

        @Test
        @DisplayName("TC18 - Error: Missing facility ID")
        void regenerateSchedules_NoFacilityId_ShouldReturnError() throws Exception {
            // Given
            Long orderId = 1L;
            RegenerateSchedulesRequest request = TestDataFactory.createRegenerateSchedulesRequest(
                    Arrays.asList("08:00:00", "16:00:00"));

            // When & Then
            mockMvc.perform(post("/api/v1/medication-orders/{orderId}/regenerate-schedules", orderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(batchService, never()).regenerateSchedules(anyLong(), anyLong(), any(RegenerateSchedulesRequest.class));
        }
    }
}