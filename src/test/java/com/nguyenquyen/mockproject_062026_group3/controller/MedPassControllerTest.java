package com.nguyenquyen.mockproject_062026_group3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nguyenquyen.mockproject_062026_group3.dto.request.*;
import com.nguyenquyen.mockproject_062026_group3.dto.response.*;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.service.MedPassService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedPassController Unit Tests - Phần 2")
class MedPassControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private MedPassService medPassService;

    @InjectMocks
    private MedPassController medPassController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(medPassController).build();
        objectMapper = new ObjectMapper();
    }

    // ==================== API 5: Start Med-Pass Session Tests ====================

    @Nested
    @DisplayName("API 5: Start Med-Pass Session - Tests")
    class StartSessionTests {

        @Test
        @DisplayName("TC01 - Success: Should start session successfully")
        void startSession_Success_ShouldReturnSession() throws Exception {
            // Given
            Long facilityId = 1L;
            StartMedPassRequest request = TestDataFactory.createStartMedPassRequest(1L);
            StartMedPassResponse mockResponse = TestDataFactory.createStartMedPassResponse();

            when(medPassService.startSession(eq(facilityId), any(StartMedPassRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/start")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data.sessionId").value("MP-20260717-0001"))
                    .andExpect(jsonPath("$.data.resident.id").value(1L))
                    .andExpect(jsonPath("$.data.pendingMedications").isArray());

            verify(medPassService, times(1)).startSession(eq(facilityId), any(StartMedPassRequest.class));
        }

        @Test
        @DisplayName("TC02 - Success: With controlled substance should require witness")
        void startSession_WithControlledSubstance_ShouldReturnSession() throws Exception {
            // Given
            Long facilityId = 1L;
            StartMedPassRequest request = TestDataFactory.createStartMedPassRequest(2L);
            StartMedPassResponse mockResponse = TestDataFactory.createStartMedPassResponseWithControlledSubstance();

            when(medPassService.startSession(eq(facilityId), any(StartMedPassRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/start")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.pendingMedications[0].isControlledSubstance").value(true));

            verify(medPassService, times(1)).startSession(eq(facilityId), any(StartMedPassRequest.class));
        }

        @Test
        @DisplayName("TC03 - Error: Missing resident ID should return error")
        void startSession_NoResidentId_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            StartMedPassRequest request = new StartMedPassRequest();

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/start")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC04 - Error: Resident not found should return 404")
        void startSession_ResidentNotFound_ShouldReturn404() throws Exception {
            // Given
            Long facilityId = 1L;
            StartMedPassRequest request = TestDataFactory.createStartMedPassRequest(999L);

            when(medPassService.startSession(eq(facilityId), any(StartMedPassRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_RESIDENT_NOT_FOUND));

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/start")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("TC05 - Error: No active orders should return 404")
        void startSession_NoActiveOrders_ShouldReturn404() throws Exception {
            // Given
            Long facilityId = 1L;
            StartMedPassRequest request = TestDataFactory.createStartMedPassRequest(1L);

            when(medPassService.startSession(eq(facilityId), any(StartMedPassRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_NO_ACTIVE_ORDERS));

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/start")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("TC06 - Error: Missing facility ID should return error")
        void startSession_NoFacilityId_ShouldReturnError() throws Exception {
            // Given
            StartMedPassRequest request = TestDataFactory.createStartMedPassRequest(1L);

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/start")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== API 6: Scan Barcode Tests ====================

    @Nested
    @DisplayName("API 6: Scan Barcode - Tests")
    class ScanBarcodeTests {

        @Test
        @DisplayName("TC07 - Success: Barcode matched should return success")
        void scanBarcode_Matched_ShouldReturnSuccess() throws Exception {
            // Given
            Long facilityId = 1L;
            ScanBarcodeRequest request = TestDataFactory.createScanBarcodeRequest(
                    "MP-20260717-0001", 1L, 1L, 1L, "ASPIRIN_100MG_123456", "CAMERA");
            ScanBarcodeResponse mockResponse = TestDataFactory.createScanBarcodeResponse(true);

            when(medPassService.scanBarcode(eq(facilityId), any(ScanBarcodeRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/scan")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.data.verificationStatus").value("MATCHED"))
                    .andExpect(jsonPath("$.data.canAdminister").value(true))
                    .andExpect(jsonPath("$.data.requiresOverride").value(false));

            verify(medPassService, times(1)).scanBarcode(eq(facilityId), any(ScanBarcodeRequest.class));
        }

        @Test
        @DisplayName("TC08 - Success: Barcode mismatch should require override")
        void scanBarcode_Mismatch_ShouldRequireOverride() throws Exception {
            // Given
            Long facilityId = 1L;
            ScanBarcodeRequest request = TestDataFactory.createScanBarcodeRequest(
                    "MP-20260717-0001", 1L, 1L, 1L, "AMOXICILLIN_500MG_789012", "CAMERA");
            ScanBarcodeResponse mockResponse = TestDataFactory.createScanBarcodeResponse(false);

            when(medPassService.scanBarcode(eq(facilityId), any(ScanBarcodeRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/scan")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.verificationStatus").value("FAILED"))
                    .andExpect(jsonPath("$.data.canAdminister").value(false))
                    .andExpect(jsonPath("$.data.requiresOverride").value(true))
                    .andExpect(jsonPath("$.data.overrideReasons").isArray());

            verify(medPassService, times(1)).scanBarcode(eq(facilityId), any(ScanBarcodeRequest.class));
        }

        @Test
        @DisplayName("TC09 - Success: Manual entry should work")
        void scanBarcode_ManualEntry_ShouldSucceed() throws Exception {
            // Given
            Long facilityId = 1L;
            ScanBarcodeRequest request = TestDataFactory.createScanBarcodeRequest(
                    "MP-20260717-0001", 1L, 1L, 1L, "ASPIRIN_100MG_123456", "MANUAL");
            ScanBarcodeResponse mockResponse = TestDataFactory.createScanBarcodeResponse(true);

            when(medPassService.scanBarcode(eq(facilityId), any(ScanBarcodeRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/scan")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.verificationStatus").value("MATCHED"));

            verify(medPassService, times(1)).scanBarcode(eq(facilityId), any(ScanBarcodeRequest.class));
        }

        @Test
        @DisplayName("TC10 - Error: Invalid session should return error")
        void scanBarcode_InvalidSession_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            ScanBarcodeRequest request = TestDataFactory.createScanBarcodeRequest(
                    null, 1L, 1L, 1L, "ASPIRIN_100MG_123456", "CAMERA");

            when(medPassService.scanBarcode(eq(facilityId), any(ScanBarcodeRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_INVALID_SESSION));

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/scan")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC11 - Error: Order not found should return 404")
        void scanBarcode_OrderNotFound_ShouldReturn404() throws Exception {
            // Given
            Long facilityId = 1L;
            ScanBarcodeRequest request = TestDataFactory.createScanBarcodeRequest(
                    "MP-20260717-0001", 1L, 999L, 1L, "ASPIRIN_100MG_123456", "CAMERA");

            when(medPassService.scanBarcode(eq(facilityId), any(ScanBarcodeRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_ORDER_NOT_FOUND));

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/scan")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("TC12 - Error: Schedule not found should return 404")
        void scanBarcode_ScheduleNotFound_ShouldReturn404() throws Exception {
            // Given
            Long facilityId = 1L;
            ScanBarcodeRequest request = TestDataFactory.createScanBarcodeRequest(
                    "MP-20260717-0001", 1L, 1L, 999L, "ASPIRIN_100MG_123456", "CAMERA");

            when(medPassService.scanBarcode(eq(facilityId), any(ScanBarcodeRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_SCHEDULE_NOT_FOUND));

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/scan")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    // ==================== API 7: Administer Medication Tests ====================

    @Nested
    @DisplayName("API 7: Administer Medication - Tests")
    class AdministerMedicationTests {

        @Test
        @DisplayName("TC13 - Success: Should administer medication")
        void administerMedication_Success_ShouldReturnLog() throws Exception {
            // Given
            Long facilityId = 1L;
            AdministerMedicationRequest request = TestDataFactory.createAdministerMedicationRequest(
                    "MP-20260717-0001", 1L, 1L, null, "Patient took with water");
            AdministerMedicationResponse mockResponse = TestDataFactory.createAdministerMedicationResponse();

            when(medPassService.administerMedication(eq(facilityId), any(AdministerMedicationRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/administer")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.statusCode").value(201))
                    .andExpect(jsonPath("$.data.status").value("ADMINISTERED"))
                    .andExpect(jsonPath("$.data.logId").value(123456L));

            verify(medPassService, times(1)).administerMedication(eq(facilityId), any(AdministerMedicationRequest.class));
        }

        @Test
        @DisplayName("TC14 - Success: With witness for controlled substance")
        void administerMedication_WithWitness_ShouldSucceed() throws Exception {
            // Given
            Long facilityId = 1L;
            AdministerMedicationRequest request = TestDataFactory.createAdministerMedicationRequest(
                    "MP-20260717-0001", 2L, 2L, 8L, "Controlled substance with witness");
            AdministerMedicationResponse mockResponse = TestDataFactory.createAdministerMedicationResponse();

            when(medPassService.administerMedication(eq(facilityId), any(AdministerMedicationRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/administer")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("TC15 - Error: Already administered should return error")
        void administerMedication_AlreadyAdministered_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            AdministerMedicationRequest request = TestDataFactory.createAdministerMedicationRequest(
                    "MP-20260717-0001", 1L, 1L, null, "Patient took with water");

            when(medPassService.administerMedication(eq(facilityId), any(AdministerMedicationRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_ALREADY_ADMINISTERED));

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/administer")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC16 - Error: Witness required for controlled substance")
        void administerMedication_WitnessRequired_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            AdministerMedicationRequest request = TestDataFactory.createAdministerMedicationRequest(
                    "MP-20260717-0001", 2L, 2L, null, "Controlled substance without witness");

            when(medPassService.administerMedication(eq(facilityId), any(AdministerMedicationRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_WITNESS_REQUIRED));

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/administer")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== API 8: Override Verification Tests ====================

    @Nested
    @DisplayName("API 8: Override Verification - Tests")
    class OverrideVerificationTests {

        @Test
        @DisplayName("TC17 - Success: Should override verification")
        void overrideVerification_Success_ShouldReturnLog() throws Exception {
            // Given
            Long facilityId = 1L;
            OverrideVerificationRequest request = TestDataFactory.createOverrideVerificationRequest(
                    "MP-20260717-0001", 1L, 1L, "TIME_WINDOW_EXCEPTION", null, true, 8L, "Physician approved");
            OverrideVerificationResponse mockResponse = TestDataFactory.createOverrideVerificationResponse();

            when(medPassService.overrideVerification(eq(facilityId), any(OverrideVerificationRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/override")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.statusCode").value(201))
                    .andExpect(jsonPath("$.data.overrideReason").value("TIME_WINDOW_EXCEPTION"))
                    .andExpect(jsonPath("$.data.auditLogged").value(true));

            verify(medPassService, times(1)).overrideVerification(eq(facilityId), any(OverrideVerificationRequest.class));
        }

        @Test
        @DisplayName("TC18 - Success: With OTHER reason and text")
        void overrideVerification_WithOtherReason_ShouldSucceed() throws Exception {
            // Given
            Long facilityId = 1L;
            OverrideVerificationRequest request = TestDataFactory.createOverrideVerificationRequest(
                    "MP-20260717-0001", 1L, 1L, "OTHER", "Patient was in critical condition",
                    true, 8L, "Emergency situation");
            OverrideVerificationResponse mockResponse = TestDataFactory.createOverrideVerificationResponse();

            when(medPassService.overrideVerification(eq(facilityId), any(OverrideVerificationRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/override")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("TC19 - Error: Missing override reason should return error")
        void overrideVerification_NoReason_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            OverrideVerificationRequest request = TestDataFactory.createOverrideVerificationRequest(
                    "MP-20260717-0001", 1L, 1L, null, null, true, 8L, "Test");

            when(medPassService.overrideVerification(eq(facilityId), any(OverrideVerificationRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_OVERRIDE_REASON_REQUIRED));

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/override")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC20 - Error: Missing other reason text when OTHER")
        void overrideVerification_OtherReasonNoText_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            OverrideVerificationRequest request = TestDataFactory.createOverrideVerificationRequest(
                    "MP-20260717-0001", 1L, 1L, "OTHER", null, true, 8L, "Test");

            when(medPassService.overrideVerification(eq(facilityId), any(OverrideVerificationRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_OTHER_REASON_REQUIRED));

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/override")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("TC21 - Error: Missing clinical justification should return error")
        void overrideVerification_NoJustification_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            OverrideVerificationRequest request = TestDataFactory.createOverrideVerificationRequest(
                    "MP-20260717-0001", 1L, 1L, "TIME_WINDOW_EXCEPTION", null, false, 8L, "Test");

            when(medPassService.overrideVerification(eq(facilityId), any(OverrideVerificationRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_CLINICAL_JUSTIFICATION_REQUIRED));

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/override")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== API 9: Refuse Medication Tests ====================

    @Nested
    @DisplayName("API 9: Refuse Medication - Tests")
    class RefuseMedicationTests {

        @Test
        @DisplayName("TC22 - Success: Should mark as refused")
        void refuseMedication_Success_ShouldReturnLog() throws Exception {
            // Given
            Long facilityId = 1L;
            RefuseMedicationRequest request = TestDataFactory.createRefuseMedicationRequest(
                    "MP-20260717-0001", 1L, 1L, "Patient refused due to nausea");
            RefuseMedicationResponse mockResponse = TestDataFactory.createRefuseMedicationResponse();

            when(medPassService.refuseMedication(eq(facilityId), any(RefuseMedicationRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/refuse")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.statusCode").value(201))
                    .andExpect(jsonPath("$.data.status").value("REFUSED"))
                    .andExpect(jsonPath("$.data.overrideReason").value("Patient refused due to nausea"));

            verify(medPassService, times(1)).refuseMedication(eq(facilityId), any(RefuseMedicationRequest.class));
        }

        @Test
        @DisplayName("TC23 - Error: Missing reason should return error")
        void refuseMedication_NoReason_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            RefuseMedicationRequest request = TestDataFactory.createRefuseMedicationRequest(
                    "MP-20260717-0001", 1L, 1L, null);

            when(medPassService.refuseMedication(eq(facilityId), any(RefuseMedicationRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_OVERRIDE_REASON_REQUIRED));

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/refuse")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== API 10: Hold Medication Tests ====================

    @Nested
    @DisplayName("API 10: Hold Medication - Tests")
    class HoldMedicationTests {

        @Test
        @DisplayName("TC24 - Success: Should mark as held")
        void holdMedication_Success_ShouldReturnLog() throws Exception {
            // Given
            Long facilityId = 1L;
            HoldMedicationRequest request = TestDataFactory.createHoldMedicationRequest(
                    "MP-20260717-0001", 1L, 1L, "Patient was in therapy session");
            HoldMedicationResponse mockResponse = TestDataFactory.createHoldMedicationResponse();

            when(medPassService.holdMedication(eq(facilityId), any(HoldMedicationRequest.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/hold")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.statusCode").value(201))
                    .andExpect(jsonPath("$.data.status").value("HELD"))
                    .andExpect(jsonPath("$.data.overrideReason").value("Patient was in therapy session"));

            verify(medPassService, times(1)).holdMedication(eq(facilityId), any(HoldMedicationRequest.class));
        }

        @Test
        @DisplayName("TC25 - Error: Missing reason should return error")
        void holdMedication_NoReason_ShouldReturnError() throws Exception {
            // Given
            Long facilityId = 1L;
            HoldMedicationRequest request = TestDataFactory.createHoldMedicationRequest(
                    "MP-20260717-0001", 1L, 1L, null);

            when(medPassService.holdMedication(eq(facilityId), any(HoldMedicationRequest.class)))
                    .thenThrow(new AppException(ErrorCode.MAR_OVERRIDE_REASON_REQUIRED));

            // When & Then
            mockMvc.perform(post("/api/v1/mar/med-pass/hold")
                            .header("X-Facility-ID", facilityId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}