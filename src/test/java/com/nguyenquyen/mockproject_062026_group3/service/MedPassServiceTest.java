package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.common.constants.MedicationStatus;
import com.nguyenquyen.mockproject_062026_group3.dto.request.*;
import com.nguyenquyen.mockproject_062026_group3.dto.response.*;
import com.nguyenquyen.mockproject_062026_group3.entity.*;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.repository.*;
import com.nguyenquyen.mockproject_062026_group3.testutils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedPassService Unit Tests - Phần 2")
class MedPassServiceTest {

    @Mock
    private ResidentRepository residentRepository;

    @Mock
    private MedicationOrderRepository medicationOrderRepository;

    @Mock
    private MedicationScheduleRepository medicationScheduleRepository;

    @Mock
    private MedicationLogRepository medicationLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MedPassService medPassService;

    private Long facilityId;
    private Long residentId;
    private Resident mockResident;
    private MedicationOrder mockOrder;
    private MedicationSchedule mockSchedule;
    private MedicationLog mockLog;
    private User mockUser;
    private User mockWitness;

    @BeforeEach
    void setUp() {
        facilityId = 1L;
        residentId = 1L;

        mockUser = TestDataFactory.createUser(7L, "RN");
        mockWitness = TestDataFactory.createUser(8L, "LPN");
        mockResident = TestDataFactory.createResident(residentId);
        mockOrder = TestDataFactory.createMedicationOrder(1L, mockResident, mockUser, MedicationStatus.ACTIVE, false);
        mockSchedule = TestDataFactory.createMedicationSchedule(1L, mockOrder, LocalTime.of(8, 0), true);
        mockLog = TestDataFactory.createMedicationLog(1L, mockOrder, mockUser, MedicationStatus.ADMINISTERED, OffsetDateTime.now());
    }

    // ==================== API 5: Start Session Tests ====================

    @Nested
    @DisplayName("API 5: Start Med-Pass Session - Tests")
    class StartSessionTests {

        @Test
        @DisplayName("TC01 - Success: Should start session with pending medications")
        void startSession_Success_ShouldReturnSession() {
            // Given
            StartMedPassRequest request = TestDataFactory.createStartMedPassRequest(residentId);
            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findByResidentIdAndStatus(eq(residentId), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            // When
            StartMedPassResponse response = medPassService.startSession(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getSessionId()).isNotNull();
            assertThat(response.getSessionId()).startsWith("MP-");
            assertThat(response.getExpiresAt()).isNotNull();
            assertThat(response.getResident()).isNotNull();
            assertThat(response.getResident().getId()).isEqualTo(residentId);
            assertThat(response.getPendingMedications()).isNotEmpty();
            assertThat(response.getPendingMedications().get(0).getOrderId()).isEqualTo(mockOrder.getId());

            verify(residentRepository, times(1)).findById(residentId);
            verify(medicationOrderRepository, times(1)).findByResidentIdAndStatus(eq(residentId), anyString());
            verify(medicationScheduleRepository, times(1)).findByOrderIdInAndIsActiveTrue(anyList());
        }

        @Test
        @DisplayName("TC02 - Success: With controlled substance")
        void startSession_WithControlledSubstance_ShouldReturnSession() {
            // Given
            Long controlledResidentId = 2L;
            Resident controlledResident = TestDataFactory.createResident(controlledResidentId);
            MedicationOrder controlledOrder = TestDataFactory.createMedicationOrder(
                    2L, controlledResident, mockUser, MedicationStatus.ACTIVE, true);
            MedicationSchedule controlledSchedule = TestDataFactory.createMedicationSchedule(
                    2L, controlledOrder, LocalTime.of(8, 0), true);

            StartMedPassRequest request = TestDataFactory.createStartMedPassRequest(controlledResidentId);

            when(residentRepository.findById(controlledResidentId)).thenReturn(Optional.of(controlledResident));
            when(medicationOrderRepository.findByResidentIdAndStatus(eq(controlledResidentId), anyString()))
                    .thenReturn(Collections.singletonList(controlledOrder));
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(Collections.singletonList(controlledSchedule));
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            // When
            StartMedPassResponse response = medPassService.startSession(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getPendingMedications()).isNotEmpty();
            assertThat(response.getPendingMedications().get(0).getIsControlledSubstance()).isTrue();
        }

        @Test
        @DisplayName("TC03 - Success: Multiple pending medications")
        void startSession_MultipleMedications_ShouldReturnAll() {
            // Given
            MedicationOrder order2 = TestDataFactory.createMedicationOrder(2L, mockResident, mockUser, MedicationStatus.ACTIVE, false);
            MedicationSchedule schedule2 = TestDataFactory.createMedicationSchedule(2L, order2, LocalTime.of(16, 0), true);

            StartMedPassRequest request = TestDataFactory.createStartMedPassRequest(residentId);
            List<MedicationOrder> orders = Arrays.asList(mockOrder, order2);
            List<MedicationSchedule> schedules = Arrays.asList(mockSchedule, schedule2);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findByResidentIdAndStatus(eq(residentId), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            // When
            StartMedPassResponse response = medPassService.startSession(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getPendingMedications()).hasSize(2);
        }

        @Test
        @DisplayName("TC04 - Error: Resident not found")
        void startSession_ResidentNotFound_ShouldThrowException() {
            // Given
            Long nonExistentResidentId = 999L;
            StartMedPassRequest request = TestDataFactory.createStartMedPassRequest(nonExistentResidentId);

            when(residentRepository.findById(nonExistentResidentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> medPassService.startSession(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_RESIDENT_NOT_FOUND);
        }

        @Test
        @DisplayName("TC05 - Error: No active orders")
        void startSession_NoActiveOrders_ShouldThrowException() {
            // Given
            StartMedPassRequest request = TestDataFactory.createStartMedPassRequest(residentId);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findByResidentIdAndStatus(eq(residentId), anyString()))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertThatThrownBy(() -> medPassService.startSession(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_NO_ACTIVE_ORDERS);
        }

        @Test
        @DisplayName("TC06 - Error: No schedules")
        void startSession_NoSchedules_ShouldThrowException() {
            // Given
            StartMedPassRequest request = TestDataFactory.createStartMedPassRequest(residentId);
            List<MedicationOrder> orders = Collections.singletonList(mockOrder);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findByResidentIdAndStatus(eq(residentId), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertThatThrownBy(() -> medPassService.startSession(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_NO_SCHEDULES);
        }

        @Test
        @DisplayName("TC07 - Error: All medications already administered today")
        void startSession_AllAdministered_ShouldThrowException() {
            // Given
            StartMedPassRequest request = TestDataFactory.createStartMedPassRequest(residentId);
            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);
            List<MedicationLog> logs = Collections.singletonList(mockLog);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findByResidentIdAndStatus(eq(residentId), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(logs);

            // When & Then
            assertThatThrownBy(() -> medPassService.startSession(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_ALREADY_ADMINISTERED);
        }
    }

    // ==================== API 6: Scan Barcode Tests ====================

    @Nested
    @DisplayName("API 6: Scan Barcode - Tests")
    class ScanBarcodeTests {

        @Test
        @DisplayName("TC08 - Success: Barcode matched all rights")
        void scanBarcode_Matched_ShouldReturnSuccess() {
            // Given
            ScanBarcodeRequest request = TestDataFactory.createScanBarcodeRequest(
                    "MP-20260717-0001", residentId, mockOrder.getId(), mockSchedule.getId(),
                    "ASPIRIN_100MG_123456", "CAMERA");

            when(medicationOrderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
            when(medicationScheduleRepository.findById(mockSchedule.getId())).thenReturn(Optional.of(mockSchedule));
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            // When
            ScanBarcodeResponse response = medPassService.scanBarcode(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getVerificationStatus()).isEqualTo("MATCHED");
            assertThat(response.getCanAdminister()).isTrue();
            assertThat(response.getRequiresOverride()).isFalse();
            assertThat(response.getFiveRights()).isNotNull();
            assertThat(response.getFiveRights().getRightResident().getPassed()).isTrue();
            assertThat(response.getFiveRights().getRightMedication().getPassed()).isTrue();
            assertThat(response.getFiveRights().getRightDose().getPassed()).isTrue();
            assertThat(response.getFiveRights().getRightRoute().getPassed()).isTrue();
            // Right time may vary based on current time
        }

        @Test
        @DisplayName("TC09 - Success: Barcode mismatch should require override")
        void scanBarcode_Mismatch_ShouldRequireOverride() {
            // Given
            ScanBarcodeRequest request = TestDataFactory.createScanBarcodeRequest(
                    "MP-20260717-0001", residentId, mockOrder.getId(), mockSchedule.getId(),
                    "AMOXICILLIN_500MG_789012", "CAMERA");

            when(medicationOrderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
            when(medicationScheduleRepository.findById(mockSchedule.getId())).thenReturn(Optional.of(mockSchedule));
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            // When
            ScanBarcodeResponse response = medPassService.scanBarcode(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getVerificationStatus()).isEqualTo("FAILED");
            assertThat(response.getCanAdminister()).isFalse();
            assertThat(response.getRequiresOverride()).isTrue();
            assertThat(response.getOverrideReasons()).isNotEmpty();
            assertThat(response.getFiveRights().getRightMedication().getPassed()).isFalse();
        }

        @Test
        @DisplayName("TC10 - Success: Manual entry should work")
        void scanBarcode_ManualEntry_ShouldSucceed() {
            // Given
            ScanBarcodeRequest request = TestDataFactory.createScanBarcodeRequest(
                    "MP-20260717-0001", residentId, mockOrder.getId(), mockSchedule.getId(),
                    "ASPIRIN_100MG_123456", "MANUAL");

            when(medicationOrderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
            when(medicationScheduleRepository.findById(mockSchedule.getId())).thenReturn(Optional.of(mockSchedule));
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            // When
            ScanBarcodeResponse response = medPassService.scanBarcode(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getVerificationStatus()).isEqualTo("MATCHED");
        }

        @Test
        @DisplayName("TC11 - Error: Invalid session")
        void scanBarcode_InvalidSession_ShouldThrowException() {
            // Given
            ScanBarcodeRequest request = TestDataFactory.createScanBarcodeRequest(
                    null, residentId, mockOrder.getId(), mockSchedule.getId(),
                    "ASPIRIN_100MG_123456", "CAMERA");

            // When & Then
            assertThatThrownBy(() -> medPassService.scanBarcode(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_INVALID_SESSION);
        }

        @Test
        @DisplayName("TC12 - Error: Order not found")
        void scanBarcode_OrderNotFound_ShouldThrowException() {
            // Given
            Long nonExistentOrderId = 999L;
            ScanBarcodeRequest request = TestDataFactory.createScanBarcodeRequest(
                    "MP-20260717-0001", residentId, nonExistentOrderId, mockSchedule.getId(),
                    "ASPIRIN_100MG_123456", "CAMERA");

            when(medicationOrderRepository.findById(nonExistentOrderId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> medPassService.scanBarcode(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_ORDER_NOT_FOUND);
        }

        @Test
        @DisplayName("TC13 - Error: Schedule not found")
        void scanBarcode_ScheduleNotFound_ShouldThrowException() {
            // Given
            Long nonExistentScheduleId = 999L;
            ScanBarcodeRequest request = TestDataFactory.createScanBarcodeRequest(
                    "MP-20260717-0001", residentId, mockOrder.getId(), nonExistentScheduleId,
                    "ASPIRIN_100MG_123456", "CAMERA");

            when(medicationOrderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
            when(medicationScheduleRepository.findById(nonExistentScheduleId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> medPassService.scanBarcode(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_SCHEDULE_NOT_FOUND);
        }

        @Test
        @DisplayName("TC14 - Error: Already administered today")
        void scanBarcode_AlreadyAdministered_ShouldThrowException() {
            // Given
            ScanBarcodeRequest request = TestDataFactory.createScanBarcodeRequest(
                    "MP-20260717-0001", residentId, mockOrder.getId(), mockSchedule.getId(),
                    "ASPIRIN_100MG_123456", "CAMERA");

            when(medicationOrderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
            when(medicationScheduleRepository.findById(mockSchedule.getId())).thenReturn(Optional.of(mockSchedule));
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.singletonList(mockLog));

            // When & Then
            assertThatThrownBy(() -> medPassService.scanBarcode(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_ALREADY_ADMINISTERED);
        }
    }

    // ==================== API 7: Administer Medication Tests ====================

    @Nested
    @DisplayName("API 7: Administer Medication - Tests")
    class AdministerMedicationTests {

        @Test
        @DisplayName("TC15 - Success: Should administer and create log")
        void administerMedication_Success_ShouldCreateLog() {
            // Given
            AdministerMedicationRequest request = TestDataFactory.createAdministerMedicationRequest(
                    "MP-20260717-0001", mockOrder.getId(), mockSchedule.getId(), null, "Patient took with water");

            when(medicationOrderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());
            when(userRepository.findById(eq(1L))).thenReturn(Optional.of(mockUser));
            when(medicationLogRepository.save(any(MedicationLog.class))).thenReturn(mockLog);

            // When
            AdministerMedicationResponse response = medPassService.administerMedication(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(MedicationStatus.ADMINISTERED);
            assertThat(response.getOrderId()).isEqualTo(mockOrder.getId());
            assertThat(response.getScheduleId()).isEqualTo(mockSchedule.getId());
            assertThat(response.getAdministeredBy()).isNotNull();

            verify(medicationLogRepository, times(1)).save(any(MedicationLog.class));
        }

        @Test
        @DisplayName("TC16 - Success: With witness")
        void administerMedication_WithWitness_ShouldSucceed() {
            // Given
            AdministerMedicationRequest request = TestDataFactory.createAdministerMedicationRequest(
                    "MP-20260717-0001", mockOrder.getId(), mockSchedule.getId(), 8L, "With witness");

            when(medicationOrderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());
            when(userRepository.findById(eq(1L))).thenReturn(Optional.of(mockUser));
            when(userRepository.findById(eq(8L))).thenReturn(Optional.of(mockWitness));
            when(medicationLogRepository.save(any(MedicationLog.class))).thenReturn(mockLog);

            // When
            AdministerMedicationResponse response = medPassService.administerMedication(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getWitnessedBy()).isNotNull();
        }

        @Test
        @DisplayName("TC17 - Error: Invalid session")
        void administerMedication_InvalidSession_ShouldThrowException() {
            // Given
            AdministerMedicationRequest request = TestDataFactory.createAdministerMedicationRequest(
                    null, mockOrder.getId(), mockSchedule.getId(), null, "Test");

            // When & Then
            assertThatThrownBy(() -> medPassService.administerMedication(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_INVALID_SESSION);
        }

        @Test
        @DisplayName("TC18 - Error: Order not found")
        void administerMedication_OrderNotFound_ShouldThrowException() {
            // Given
            Long nonExistentOrderId = 999L;
            AdministerMedicationRequest request = TestDataFactory.createAdministerMedicationRequest(
                    "MP-20260717-0001", nonExistentOrderId, mockSchedule.getId(), null, "Test");

            when(medicationOrderRepository.findById(nonExistentOrderId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> medPassService.administerMedication(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_ORDER_NOT_FOUND);
        }

        @Test
        @DisplayName("TC19 - Error: Already administered")
        void administerMedication_AlreadyAdministered_ShouldThrowException() {
            // Given
            AdministerMedicationRequest request = TestDataFactory.createAdministerMedicationRequest(
                    "MP-20260717-0001", mockOrder.getId(), mockSchedule.getId(), null, "Test");

            when(medicationOrderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.singletonList(mockLog));

            // When & Then
            assertThatThrownBy(() -> medPassService.administerMedication(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_ALREADY_ADMINISTERED);
        }

        @Test
        @DisplayName("TC20 - Error: Witness required for controlled substance")
        void administerMedication_WitnessRequired_ShouldThrowException() {
            // Given
            MedicationOrder controlledOrder = TestDataFactory.createMedicationOrder(
                    2L, mockResident, mockUser, MedicationStatus.ACTIVE, true);
            MedicationSchedule controlledSchedule = TestDataFactory.createMedicationSchedule(
                    2L, controlledOrder, LocalTime.of(8, 0), true);

            AdministerMedicationRequest request = TestDataFactory.createAdministerMedicationRequest(
                    "MP-20260717-0001", controlledOrder.getId(), controlledSchedule.getId(), null, "No witness");

            when(medicationOrderRepository.findById(controlledOrder.getId())).thenReturn(Optional.of(controlledOrder));
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertThatThrownBy(() -> medPassService.administerMedication(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_WITNESS_REQUIRED);
        }
    }

    // ==================== API 8: Override Verification Tests ====================

    @Nested
    @DisplayName("API 8: Override Verification - Tests")
    class OverrideVerificationTests {

        @Test
        @DisplayName("TC21 - Success: Should override and create log")
        void overrideVerification_Success_ShouldCreateLog() {
            // Given
            OverrideVerificationRequest request = TestDataFactory.createOverrideVerificationRequest(
                    "MP-20260717-0001", mockOrder.getId(), mockSchedule.getId(),
                    "TIME_WINDOW_EXCEPTION", null, true, 8L, "Physician approved");

            when(medicationOrderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());
            when(userRepository.findById(eq(1L))).thenReturn(Optional.of(mockUser));
            when(userRepository.findById(eq(8L))).thenReturn(Optional.of(mockWitness));
            when(medicationLogRepository.save(any(MedicationLog.class))).thenReturn(mockLog);

            // When
            OverrideVerificationResponse response = medPassService.overrideVerification(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(MedicationStatus.ADMINISTERED);
            assertThat(response.getOverrideReason()).isEqualTo("TIME_WINDOW_EXCEPTION");
            assertThat(response.getAuditLogged()).isTrue();

            verify(medicationLogRepository, times(1)).save(any(MedicationLog.class));
        }

        @Test
        @DisplayName("TC22 - Success: With OTHER reason")
        void overrideVerification_WithOtherReason_ShouldSucceed() {
            // Given
            OverrideVerificationRequest request = TestDataFactory.createOverrideVerificationRequest(
                    "MP-20260717-0001", mockOrder.getId(), mockSchedule.getId(),
                    "OTHER", "Patient was in critical condition", true, 8L, "Emergency");

            when(medicationOrderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());
            when(userRepository.findById(eq(1L))).thenReturn(Optional.of(mockUser));
            when(userRepository.findById(eq(8L))).thenReturn(Optional.of(mockWitness));
            when(medicationLogRepository.save(any(MedicationLog.class))).thenReturn(mockLog);

            // When
            OverrideVerificationResponse response = medPassService.overrideVerification(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getOverrideReason()).isEqualTo("Patient was in critical condition");
        }

        @Test
        @DisplayName("TC23 - Error: Invalid session")
        void overrideVerification_InvalidSession_ShouldThrowException() {
            // Given
            OverrideVerificationRequest request = TestDataFactory.createOverrideVerificationRequest(
                    null, mockOrder.getId(), mockSchedule.getId(),
                    "TIME_WINDOW_EXCEPTION", null, true, 8L, "Test");

            // When & Then
            assertThatThrownBy(() -> medPassService.overrideVerification(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_INVALID_SESSION);
        }

        @Test
        @DisplayName("TC24 - Error: Missing override reason")
        void overrideVerification_NoReason_ShouldThrowException() {
            // Given
            OverrideVerificationRequest request = TestDataFactory.createOverrideVerificationRequest(
                    "MP-20260717-0001", mockOrder.getId(), mockSchedule.getId(),
                    null, null, true, 8L, "Test");

            // When & Then
            assertThatThrownBy(() -> medPassService.overrideVerification(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_OVERRIDE_REASON_REQUIRED);
        }

        @Test
        @DisplayName("TC25 - Error: Missing other reason text when OTHER")
        void overrideVerification_OtherReasonNoText_ShouldThrowException() {
            // Given
            OverrideVerificationRequest request = TestDataFactory.createOverrideVerificationRequest(
                    "MP-20260717-0001", mockOrder.getId(), mockSchedule.getId(),
                    "OTHER", null, true, 8L, "Test");

            // When & Then
            assertThatThrownBy(() -> medPassService.overrideVerification(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_OTHER_REASON_REQUIRED);
        }

        @Test
        @DisplayName("TC26 - Error: Missing clinical justification")
        void overrideVerification_NoJustification_ShouldThrowException() {
            // Given
            OverrideVerificationRequest request = TestDataFactory.createOverrideVerificationRequest(
                    "MP-20260717-0001", mockOrder.getId(), mockSchedule.getId(),
                    "TIME_WINDOW_EXCEPTION", null, false, 8L, "Test");

            // When & Then
            assertThatThrownBy(() -> medPassService.overrideVerification(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_CLINICAL_JUSTIFICATION_REQUIRED);
        }

        @Test
        @DisplayName("TC27 - Error: Order not found")
        void overrideVerification_OrderNotFound_ShouldThrowException() {
            // Given
            Long nonExistentOrderId = 999L;
            OverrideVerificationRequest request = TestDataFactory.createOverrideVerificationRequest(
                    "MP-20260717-0001", nonExistentOrderId, mockSchedule.getId(),
                    "TIME_WINDOW_EXCEPTION", null, true, 8L, "Test");

            when(medicationOrderRepository.findById(nonExistentOrderId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> medPassService.overrideVerification(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_ORDER_NOT_FOUND);
        }
    }

    // ==================== API 9: Refuse Medication Tests ====================

    @Nested
    @DisplayName("API 9: Refuse Medication - Tests")
    class RefuseMedicationTests {

        @Test
        @DisplayName("TC28 - Success: Should mark as refused")
        void refuseMedication_Success_ShouldCreateLog() {
            // Given
            RefuseMedicationRequest request = TestDataFactory.createRefuseMedicationRequest(
                    "MP-20260717-0001", mockOrder.getId(), mockSchedule.getId(), "Patient refused due to nausea");

            when(medicationOrderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());
            when(userRepository.findById(eq(1L))).thenReturn(Optional.of(mockUser));
            when(medicationLogRepository.save(any(MedicationLog.class))).thenReturn(mockLog);

            // When
            RefuseMedicationResponse response = medPassService.refuseMedication(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(MedicationStatus.REFUSED);
            assertThat(response.getOverrideReason()).isEqualTo("Patient refused due to nausea");
            assertThat(response.getOrderId()).isEqualTo(mockOrder.getId());

            verify(medicationLogRepository, times(1)).save(any(MedicationLog.class));
        }

        @Test
        @DisplayName("TC29 - Error: Invalid session")
        void refuseMedication_InvalidSession_ShouldThrowException() {
            // Given
            RefuseMedicationRequest request = TestDataFactory.createRefuseMedicationRequest(
                    null, mockOrder.getId(), mockSchedule.getId(), "Reason");

            // When & Then
            assertThatThrownBy(() -> medPassService.refuseMedication(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_INVALID_SESSION);
        }

        @Test
        @DisplayName("TC30 - Error: Missing reason")
        void refuseMedication_NoReason_ShouldThrowException() {
            // Given
            RefuseMedicationRequest request = TestDataFactory.createRefuseMedicationRequest(
                    "MP-20260717-0001", mockOrder.getId(), mockSchedule.getId(), null);

            // When & Then
            assertThatThrownBy(() -> medPassService.refuseMedication(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_OVERRIDE_REASON_REQUIRED);
        }

        @Test
        @DisplayName("TC31 - Error: Order not found")
        void refuseMedication_OrderNotFound_ShouldThrowException() {
            // Given
            Long nonExistentOrderId = 999L;
            RefuseMedicationRequest request = TestDataFactory.createRefuseMedicationRequest(
                    "MP-20260717-0001", nonExistentOrderId, mockSchedule.getId(), "Reason");

            when(medicationOrderRepository.findById(nonExistentOrderId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> medPassService.refuseMedication(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_ORDER_NOT_FOUND);
        }
    }

    // ==================== API 10: Hold Medication Tests ====================

    @Nested
    @DisplayName("API 10: Hold Medication - Tests")
    class HoldMedicationTests {

        @Test
        @DisplayName("TC32 - Success: Should mark as held")
        void holdMedication_Success_ShouldCreateLog() {
            // Given
            HoldMedicationRequest request = TestDataFactory.createHoldMedicationRequest(
                    "MP-20260717-0001", mockOrder.getId(), mockSchedule.getId(), "Patient was in therapy session");

            when(medicationOrderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());
            when(userRepository.findById(eq(1L))).thenReturn(Optional.of(mockUser));
            when(medicationLogRepository.save(any(MedicationLog.class))).thenReturn(mockLog);

            // When
            HoldMedicationResponse response = medPassService.holdMedication(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(MedicationStatus.HELD);
            assertThat(response.getOverrideReason()).isEqualTo("Patient was in therapy session");
            assertThat(response.getOrderId()).isEqualTo(mockOrder.getId());

            verify(medicationLogRepository, times(1)).save(any(MedicationLog.class));
        }

        @Test
        @DisplayName("TC33 - Error: Invalid session")
        void holdMedication_InvalidSession_ShouldThrowException() {
            // Given
            HoldMedicationRequest request = TestDataFactory.createHoldMedicationRequest(
                    null, mockOrder.getId(), mockSchedule.getId(), "Reason");

            // When & Then
            assertThatThrownBy(() -> medPassService.holdMedication(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_INVALID_SESSION);
        }

        @Test
        @DisplayName("TC34 - Error: Missing reason")
        void holdMedication_NoReason_ShouldThrowException() {
            // Given
            HoldMedicationRequest request = TestDataFactory.createHoldMedicationRequest(
                    "MP-20260717-0001", mockOrder.getId(), mockSchedule.getId(), null);

            // When & Then
            assertThatThrownBy(() -> medPassService.holdMedication(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_OVERRIDE_REASON_REQUIRED);
        }

        @Test
        @DisplayName("TC35 - Error: Order not found")
        void holdMedication_OrderNotFound_ShouldThrowException() {
            // Given
            Long nonExistentOrderId = 999L;
            HoldMedicationRequest request = TestDataFactory.createHoldMedicationRequest(
                    "MP-20260717-0001", nonExistentOrderId, mockSchedule.getId(), "Reason");

            when(medicationOrderRepository.findById(nonExistentOrderId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> medPassService.holdMedication(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_ORDER_NOT_FOUND);
        }

        @Test
        @DisplayName("TC36 - Error: Already administered")
        void holdMedication_AlreadyAdministered_ShouldThrowException() {
            // Given
            HoldMedicationRequest request = TestDataFactory.createHoldMedicationRequest(
                    "MP-20260717-0001", mockOrder.getId(), mockSchedule.getId(), "Reason");

            when(medicationOrderRepository.findById(mockOrder.getId())).thenReturn(Optional.of(mockOrder));
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.singletonList(mockLog));

            // When & Then
            assertThatThrownBy(() -> medPassService.holdMedication(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_ALREADY_ADMINISTERED);
        }
    }
}