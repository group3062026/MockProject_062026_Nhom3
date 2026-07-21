package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.common.constants.MedicationStatus;
import com.nguyenquyen.mockproject_062026_group3.dto.request.BatchAdministerRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.RegenerateSchedulesRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.response.BatchAdministerResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.response.RegenerateSchedulesResponse;
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
@DisplayName("BatchService Unit Tests - Phần 4")
class BatchServiceTest {

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
    private BatchService batchService;

    private Long facilityId;
    private Long residentId;
    private Long orderId;
    private Resident mockResident;
    private MedicationOrder mockOrder;
    private MedicationOrder mockOrder2;
    private MedicationOrder mockOrder3;
    private MedicationSchedule mockSchedule;
    private MedicationSchedule mockSchedule2;
    private MedicationSchedule mockSchedule3;
    private MedicationLog mockLog;
    private User mockUser;
    private User mockWitness;
    private User mockPrescriber;

    @BeforeEach
    void setUp() {
        facilityId = 1L;
        residentId = 1L;
        orderId = 1L;

        mockUser = TestDataFactory.createUser(7L, "RN");
        mockWitness = TestDataFactory.createUser(8L, "LPN");
        mockPrescriber = TestDataFactory.createUser(4L, "Doctor");
        mockResident = TestDataFactory.createResident(residentId);

        mockOrder = TestDataFactory.createMedicationOrderEntity(
                orderId, mockResident, mockPrescriber, MedicationStatus.ACTIVE, false,
                "Aspirin", "100 mg", "ORAL", "Every 8 hours");

        mockOrder2 = TestDataFactory.createMedicationOrderEntity(
                2L, mockResident, mockPrescriber, MedicationStatus.ACTIVE, false,
                "Lisinopril", "10 mg", "ORAL", "Once daily");

        mockOrder3 = TestDataFactory.createMedicationOrderEntity(
                3L, mockResident, mockPrescriber, MedicationStatus.ACTIVE, false,
                "Metformin", "500 mg", "ORAL", "Twice daily");

        mockSchedule = TestDataFactory.createMedicationSchedule(1L, mockOrder, LocalTime.of(8, 0), true);
        mockSchedule2 = TestDataFactory.createMedicationSchedule(2L, mockOrder2, LocalTime.of(9, 0), true);
        mockSchedule3 = TestDataFactory.createMedicationSchedule(3L, mockOrder3, LocalTime.of(10, 0), true);

        mockLog = TestDataFactory.createMedicationLog(1L, mockOrder, mockUser, MedicationStatus.ADMINISTERED, OffsetDateTime.now());
    }

    // ==================== API 16: Batch Administer Tests ====================

    @Nested
    @DisplayName("API 16: Batch Administer - Tests")
    class BatchAdministerTests {

        @Test
        @DisplayName("TC01 - Success: Batch administer all medications")
        void batchAdminister_AllSuccess_ShouldAdministerAll() {
            // Given
            BatchAdministerRequest request = TestDataFactory.createBatchAdministerRequest(
                    "MP-20260717-0001", residentId,
                    Arrays.asList(1L, 2L, 3L),
                    Arrays.asList(1L, 2L, 3L),
                    8L, "All medications administered together");

            List<MedicationOrder> orders = Arrays.asList(mockOrder, mockOrder2, mockOrder3);
            List<MedicationSchedule> schedules = Arrays.asList(mockSchedule, mockSchedule2, mockSchedule3);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findAllById(anyList())).thenReturn(orders);
            when(medicationScheduleRepository.findAllById(anyList())).thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            when(userRepository.findById(8L)).thenReturn(Optional.of(mockWitness));
            when(medicationLogRepository.save(any(MedicationLog.class))).thenReturn(mockLog);

            // When
            BatchAdministerResponse response = batchService.batchAdminister(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTotal()).isEqualTo(3);
            assertThat(response.getAdministered()).isEqualTo(3);
            assertThat(response.getFailed()).isEqualTo(0);
            assertThat(response.getLogs()).hasSize(3);

            verify(medicationLogRepository, times(3)).save(any(MedicationLog.class));
        }

        @Test
        @DisplayName("TC02 - Success: Batch with controlled substances and witness")
        void batchAdminister_ControlledWithWitness_ShouldSucceed() {
            // Given
            // Create controlled substance orders (even IDs)
            MedicationOrder controlledOrder1 = TestDataFactory.createMedicationOrderEntity(
                    2L, mockResident, mockPrescriber, MedicationStatus.ACTIVE, true,
                    "Morphine", "5 mg", "ORAL", "Every 6 hours");
            MedicationOrder controlledOrder2 = TestDataFactory.createMedicationOrderEntity(
                    4L, mockResident, mockPrescriber, MedicationStatus.ACTIVE, true,
                    "Oxycodone", "10 mg", "ORAL", "Every 4 hours");

            MedicationSchedule controlledSchedule1 = TestDataFactory.createMedicationSchedule(
                    2L, controlledOrder1, LocalTime.of(8, 0), true);
            MedicationSchedule controlledSchedule2 = TestDataFactory.createMedicationSchedule(
                    4L, controlledOrder2, LocalTime.of(12, 0), true);

            BatchAdministerRequest request = TestDataFactory.createBatchAdministerRequest(
                    "MP-20260717-0001", residentId,
                    Arrays.asList(2L, 4L),
                    Arrays.asList(2L, 4L),
                    8L, "Controlled substances with witness");

            List<MedicationOrder> orders = Arrays.asList(controlledOrder1, controlledOrder2);
            List<MedicationSchedule> schedules = Arrays.asList(controlledSchedule1, controlledSchedule2);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findAllById(anyList())).thenReturn(orders);
            when(medicationScheduleRepository.findAllById(anyList())).thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            when(userRepository.findById(8L)).thenReturn(Optional.of(mockWitness));
            when(medicationLogRepository.save(any(MedicationLog.class))).thenReturn(mockLog);

            // When
            BatchAdministerResponse response = batchService.batchAdminister(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTotal()).isEqualTo(2);
            assertThat(response.getAdministered()).isEqualTo(2);
            assertThat(response.getFailed()).isEqualTo(0);
        }

        @Test
        @DisplayName("TC03 - Success: Batch with partial failure")
        void batchAdminister_PartialFailure_ShouldReturnFailureDetails() {
            // Given
            BatchAdministerRequest request = TestDataFactory.createBatchAdministerRequest(
                    "MP-20260717-0001", residentId,
                    Arrays.asList(1L, 2L, 3L),
                    Arrays.asList(1L, 2L, 3L),
                    8L, "Some medications fail");

            List<MedicationOrder> orders = Arrays.asList(mockOrder, mockOrder2, mockOrder3);
            List<MedicationSchedule> schedules = Arrays.asList(mockSchedule, mockSchedule2, mockSchedule3);

            // Simulate that order 2 is already administered
            MedicationLog existingLog = TestDataFactory.createMedicationLog(
                    2L, mockOrder2, mockUser, MedicationStatus.ADMINISTERED, OffsetDateTime.now());

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findAllById(anyList())).thenReturn(orders);
            when(medicationScheduleRepository.findAllById(anyList())).thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.singletonList(existingLog));
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            when(userRepository.findById(8L)).thenReturn(Optional.of(mockWitness));
            when(medicationLogRepository.save(any(MedicationLog.class))).thenReturn(mockLog);

            // When
            BatchAdministerResponse response = batchService.batchAdminister(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTotal()).isEqualTo(3);
            assertThat(response.getAdministered()).isEqualTo(2);
            assertThat(response.getFailed()).isEqualTo(1);
            assertThat(response.getLogs()).hasSize(3);

            // Verify failed log
            boolean hasFailure = response.getLogs().stream()
                    .anyMatch(log -> log.getStatus().equals(MedicationStatus.FAILED));
            assertThat(hasFailure).isTrue();

            verify(medicationLogRepository, times(2)).save(any(MedicationLog.class));
        }

        @Test
        @DisplayName("TC04 - Success: Batch with single medication")
        void batchAdminister_SingleMedication_ShouldSucceed() {
            // Given
            BatchAdministerRequest request = TestDataFactory.createBatchAdministerRequest(
                    "MP-20260717-0001", residentId,
                    Collections.singletonList(1L),
                    Collections.singletonList(1L),
                    null, "Single medication");

            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findAllById(anyList())).thenReturn(orders);
            when(medicationScheduleRepository.findAllById(anyList())).thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            when(medicationLogRepository.save(any(MedicationLog.class))).thenReturn(mockLog);

            // When
            BatchAdministerResponse response = batchService.batchAdminister(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTotal()).isEqualTo(1);
            assertThat(response.getAdministered()).isEqualTo(1);
            assertThat(response.getFailed()).isEqualTo(0);
        }

        @Test
        @DisplayName("TC05 - Error: Invalid session")
        void batchAdminister_InvalidSession_ShouldThrowException() {
            // Given
            BatchAdministerRequest request = TestDataFactory.createBatchAdministerRequest(
                    null, residentId, // No session ID
                    Arrays.asList(1L, 2L, 3L),
                    Arrays.asList(1L, 2L, 3L),
                    8L, "Invalid session");

            // When & Then
            assertThatThrownBy(() -> batchService.batchAdminister(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_INVALID_SESSION);
        }

        @Test
        @DisplayName("TC06 - Error: Resident not found")
        void batchAdminister_ResidentNotFound_ShouldThrowException() {
            // Given
            Long nonExistentResidentId = 999L;
            BatchAdministerRequest request = TestDataFactory.createBatchAdministerRequest(
                    "MP-20260717-0001", nonExistentResidentId,
                    Arrays.asList(1L, 2L, 3L),
                    Arrays.asList(1L, 2L, 3L),
                    8L, "Resident not found");

            when(residentRepository.findById(nonExistentResidentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> batchService.batchAdminister(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_RESIDENT_NOT_FOUND);
        }

        @Test
        @DisplayName("TC07 - Error: Order not found")
        void batchAdminister_OrderNotFound_ShouldThrowException() {
            // Given
            BatchAdministerRequest request = TestDataFactory.createBatchAdministerRequest(
                    "MP-20260717-0001", residentId,
                    Arrays.asList(999L, 2L, 3L), // 999 doesn't exist
                    Arrays.asList(1L, 2L, 3L),
                    8L, "Order not found");

            List<MedicationOrder> orders = Arrays.asList(mockOrder2, mockOrder3); // Only 2 orders found

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findAllById(anyList())).thenReturn(orders);

            // When & Then
            assertThatThrownBy(() -> batchService.batchAdminister(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_BATCH_ORDER_NOT_FOUND);
        }

        @Test
        @DisplayName("TC08 - Error: Schedule not found")
        void batchAdminister_ScheduleNotFound_ShouldThrowException() {
            // Given
            BatchAdministerRequest request = TestDataFactory.createBatchAdministerRequest(
                    "MP-20260717-0001", residentId,
                    Arrays.asList(1L, 2L, 3L),
                    Arrays.asList(999L, 2L, 3L), // 999 doesn't exist
                    8L, "Schedule not found");

            List<MedicationOrder> orders = Arrays.asList(mockOrder, mockOrder2, mockOrder3);
            List<MedicationSchedule> schedules = Arrays.asList(mockSchedule2, mockSchedule3); // Only 2 schedules found

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findAllById(anyList())).thenReturn(orders);
            when(medicationScheduleRepository.findAllById(anyList())).thenReturn(schedules);

            // When & Then
            assertThatThrownBy(() -> batchService.batchAdminister(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_BATCH_SCHEDULE_NOT_FOUND);
        }

        @Test
        @DisplayName("TC09 - Error: Witness required for controlled substances")
        void batchAdminister_WitnessRequired_ShouldThrowException() {
            // Given
            // Create controlled substance orders
            MedicationOrder controlledOrder1 = TestDataFactory.createMedicationOrderEntity(
                    2L, mockResident, mockPrescriber, MedicationStatus.ACTIVE, true,
                    "Morphine", "5 mg", "ORAL", "Every 6 hours");
            MedicationOrder controlledOrder2 = TestDataFactory.createMedicationOrderEntity(
                    4L, mockResident, mockPrescriber, MedicationStatus.ACTIVE, true,
                    "Oxycodone", "10 mg", "ORAL", "Every 4 hours");

            BatchAdministerRequest request = TestDataFactory.createBatchAdministerRequest(
                    "MP-20260717-0001", residentId,
                    Arrays.asList(2L, 4L),
                    Arrays.asList(2L, 4L),
                    null, // No witness
                    "Controlled substances without witness");

            List<MedicationOrder> orders = Arrays.asList(controlledOrder1, controlledOrder2);
            List<MedicationSchedule> schedules = Arrays.asList(
                    TestDataFactory.createMedicationSchedule(2L, controlledOrder1, LocalTime.of(8, 0), true),
                    TestDataFactory.createMedicationSchedule(4L, controlledOrder2, LocalTime.of(12, 0), true)
            );

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findAllById(anyList())).thenReturn(orders);
            when(medicationScheduleRepository.findAllById(anyList())).thenReturn(schedules);

            // When & Then
            assertThatThrownBy(() -> batchService.batchAdminister(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_BATCH_WITNESS_REQUIRED);
        }

        @Test
        @DisplayName("TC10 - Error: Already administered")
        void batchAdminister_AlreadyAdministered_ShouldThrowException() {
            // Given
            BatchAdministerRequest request = TestDataFactory.createBatchAdministerRequest(
                    "MP-20260717-0001", residentId,
                    Arrays.asList(1L, 2L, 3L),
                    Arrays.asList(1L, 2L, 3L),
                    8L, "Already administered");

            List<MedicationOrder> orders = Arrays.asList(mockOrder, mockOrder2, mockOrder3);
            List<MedicationSchedule> schedules = Arrays.asList(mockSchedule, mockSchedule2, mockSchedule3);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findAllById(anyList())).thenReturn(orders);
            when(medicationScheduleRepository.findAllById(anyList())).thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Arrays.asList(mockLog,
                            TestDataFactory.createMedicationLog(2L, mockOrder2, mockUser, MedicationStatus.ADMINISTERED, OffsetDateTime.now()),
                            TestDataFactory.createMedicationLog(3L, mockOrder3, mockUser, MedicationStatus.ADMINISTERED, OffsetDateTime.now())
                    ));

            // When & Then
            assertThatThrownBy(() -> batchService.batchAdminister(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_BATCH_ALREADY_ADMINISTERED);
        }

        @Test
        @DisplayName("TC11 - Error: Empty batch request")
        void batchAdminister_EmptyBatch_ShouldThrowException() {
            // Given - but the controller validates this, service may not be called
            // This test verifies the service handles empty lists gracefully
            BatchAdministerRequest request = TestDataFactory.createBatchAdministerRequest(
                    "MP-20260717-0001", residentId,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    8L, "Empty batch");

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findAllById(anyList())).thenReturn(Collections.emptyList());
            when(medicationScheduleRepository.findAllById(anyList())).thenReturn(Collections.emptyList());
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            // When
            BatchAdministerResponse response = batchService.batchAdminister(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTotal()).isEqualTo(0);
            assertThat(response.getAdministered()).isEqualTo(0);
            assertThat(response.getFailed()).isEqualTo(0);
        }
    }

    // ==================== API 17: Regenerate Schedules Tests ====================

    @Nested
    @DisplayName("API 17: Regenerate Schedules - Tests")
    class RegenerateSchedulesTests {

        @Test
        @DisplayName("TC12 - Success: Regenerate schedules with multiple times")
        void regenerateSchedules_MultipleTimes_ShouldSucceed() {
            // Given
            RegenerateSchedulesRequest request = TestDataFactory.createRegenerateSchedulesRequest(
                    Arrays.asList("08:00:00", "16:00:00", "00:00:00"));

            List<MedicationSchedule> existingSchedules = Arrays.asList(mockSchedule, mockSchedule2);

            when(medicationOrderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
            when(medicationScheduleRepository.findByOrderIdAndIsActive(orderId))
                    .thenReturn(existingSchedules);
            when(medicationScheduleRepository.save(any(MedicationSchedule.class)))
                    .thenReturn(mockSchedule);

            // When
            RegenerateSchedulesResponse response = batchService.regenerateSchedules(
                    facilityId, orderId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getOrderId()).isEqualTo(orderId);
            assertThat(response.getSchedules()).hasSize(3);

            // Verify old schedules were deactivated
            verify(medicationScheduleRepository, times(2)).save(any(MedicationSchedule.class));
            // Verify new schedules were created (3 times)
            verify(medicationScheduleRepository, times(3)).save(any(MedicationSchedule.class));
        }

        @Test
        @DisplayName("TC13 - Success: Regenerate schedules with single time")
        void regenerateSchedules_SingleTime_ShouldSucceed() {
            // Given
            RegenerateSchedulesRequest request = TestDataFactory.createRegenerateSchedulesRequest(
                    Collections.singletonList("08:00:00"));

            List<MedicationSchedule> existingSchedules = Collections.singletonList(mockSchedule);

            when(medicationOrderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
            when(medicationScheduleRepository.findByOrderIdAndIsActive(orderId))
                    .thenReturn(existingSchedules);
            when(medicationScheduleRepository.save(any(MedicationSchedule.class)))
                    .thenReturn(mockSchedule);

            // When
            RegenerateSchedulesResponse response = batchService.regenerateSchedules(
                    facilityId, orderId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getSchedules()).hasSize(1);

            verify(medicationScheduleRepository, times(1)).save(any(MedicationSchedule.class));
        }

        @Test
        @DisplayName("TC14 - Success: No existing schedules")
        void regenerateSchedules_NoExistingSchedules_ShouldCreateNew() {
            // Given
            RegenerateSchedulesRequest request = TestDataFactory.createRegenerateSchedulesRequest(
                    Arrays.asList("08:00:00", "16:00:00"));

            when(medicationOrderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
            when(medicationScheduleRepository.findByOrderIdAndIsActive(orderId))
                    .thenReturn(Collections.emptyList());
            when(medicationScheduleRepository.save(any(MedicationSchedule.class)))
                    .thenReturn(mockSchedule);

            // When
            RegenerateSchedulesResponse response = batchService.regenerateSchedules(
                    facilityId, orderId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getSchedules()).hasSize(2);

            verify(medicationScheduleRepository, never()).save(any(MedicationSchedule.class));
            verify(medicationScheduleRepository, times(2)).save(any(MedicationSchedule.class));
        }

        @Test
        @DisplayName("TC15 - Error: Order not found")
        void regenerateSchedules_OrderNotFound_ShouldThrowException() {
            // Given
            Long nonExistentOrderId = 999L;
            RegenerateSchedulesRequest request = TestDataFactory.createRegenerateSchedulesRequest(
                    Arrays.asList("08:00:00", "16:00:00"));

            when(medicationOrderRepository.findById(nonExistentOrderId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> batchService.regenerateSchedules(
                    facilityId, nonExistentOrderId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_REGENERATE_ORDER_NOT_FOUND);
        }

        @Test
        @DisplayName("TC16 - Error: Order not active")
        void regenerateSchedules_OrderNotActive_ShouldThrowException() {
            // Given
            MedicationOrder discontinuedOrder = TestDataFactory.createDiscontinuedMedicationOrder(
                    orderId, mockResident, mockPrescriber);
            RegenerateSchedulesRequest request = TestDataFactory.createRegenerateSchedulesRequest(
                    Arrays.asList("08:00:00", "16:00:00"));

            when(medicationOrderRepository.findById(orderId)).thenReturn(Optional.of(discontinuedOrder));

            // When & Then
            assertThatThrownBy(() -> batchService.regenerateSchedules(
                    facilityId, orderId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_REGENERATE_ORDER_NOT_ACTIVE);
        }

        @Test
        @DisplayName("TC17 - Error: Empty scheduled times")
        void regenerateSchedules_EmptyTimes_ShouldThrowException() {
            // Given
            RegenerateSchedulesRequest request = TestDataFactory.createRegenerateSchedulesRequest(
                    Collections.emptyList());

            when(medicationOrderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

            // When & Then
            assertThatThrownBy(() -> batchService.regenerateSchedules(
                    facilityId, orderId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_REGENERATE_NO_TIMES);
        }

        @Test
        @DisplayName("TC18 - Error: Invalid time format")
        void regenerateSchedules_InvalidTimeFormat_ShouldThrowException() {
            // Given
            RegenerateSchedulesRequest request = TestDataFactory.createRegenerateSchedulesRequest(
                    Arrays.asList("invalid", "08:00:00"));

            when(medicationOrderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

            // When & Then
            assertThatThrownBy(() -> batchService.regenerateSchedules(
                    facilityId, orderId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_REGENERATE_INVALID_TIME);
        }

        @Test
        @DisplayName("TC19 - Error: Null scheduled times")
        void regenerateSchedules_NullTimes_ShouldThrowException() {
            // Given
            RegenerateSchedulesRequest request = TestDataFactory.createRegenerateSchedulesRequest(null);

            when(medicationOrderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

            // When & Then
            assertThatThrownBy(() -> batchService.regenerateSchedules(
                    facilityId, orderId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_REGENERATE_NO_TIMES);
        }
    }
}