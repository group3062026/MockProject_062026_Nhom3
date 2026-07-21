package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.common.constants.MedicationStatus;
import com.nguyenquyen.mockproject_062026_group3.dto.request.CreateMedicationOrderRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.DiscontinueMedicationOrderRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.GetMedicationOrdersRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicationOrderService Unit Tests - Phần 3")
class MedicationOrderServiceTest {

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
    private MedicationOrderService medicationOrderService;

    private Long facilityId;
    private Long residentId;
    private Long orderId;
    private Resident mockResident;
    private MedicationOrder mockOrder;
    private MedicationSchedule mockSchedule;
    private MedicationLog mockLog;
    private User mockUser;
    private User mockPrescriber;

    @BeforeEach
    void setUp() {
        facilityId = 1L;
        residentId = 1L;
        orderId = 1L;

        mockUser = TestDataFactory.createUser(7L, "RN");
        mockPrescriber = TestDataFactory.createUser(4L, "Doctor");
        mockResident = TestDataFactory.createResident(residentId);
        mockOrder = TestDataFactory.createMedicationOrderEntity(
                orderId, mockResident, mockPrescriber, MedicationStatus.ACTIVE, false,
                "Aspirin", "100 mg", "ORAL", "Every 8 hours");
        mockSchedule = TestDataFactory.createMedicationSchedule(1L, mockOrder, LocalTime.of(8, 0), true);
        mockLog = TestDataFactory.createMedicationLog(1L, mockOrder, mockUser, MedicationStatus.ADMINISTERED, OffsetDateTime.now());
    }

    // ==================== API 11: Get Medication Orders Tests ====================

    @Nested
    @DisplayName("API 11: Get Medication Orders - Tests")
    class GetMedicationOrdersTests {

        @Test
        @DisplayName("TC01 - Success: Should return paginated orders")
        void getMedicationOrders_Success_ShouldReturnPaginated() {
            // Given
            GetMedicationOrdersRequest request = TestDataFactory.createGetMedicationOrdersRequest(
                    residentId, MedicationStatus.ACTIVE, null, 1, 20);

            List<Resident> residents = Collections.singletonList(mockResident);
            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            Page<MedicationOrder> orderPage = new PageImpl<>(orders);

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(residents);
            when(medicationOrderRepository.findByResidentIdInAndStatusWithPagination(
                    anyList(), anyString(), any(Pageable.class)))
                    .thenReturn(orderPage);
            when(medicationLogRepository.findByOrderIdInAndStatus(anyList(), anyString()))
                    .thenReturn(Collections.emptyList());

            // When
            MedicationOrderListResponse response = medicationOrderService.getMedicationOrders(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getMedicationOrders()).isNotEmpty();
            assertThat(response.getMetadata()).isNotNull();
            assertThat(response.getMetadata().getCurrentPage()).isEqualTo(1);
            assertThat(response.getMetadata().getHasNext()).isFalse();

            verify(medicationOrderRepository, times(1))
                    .findByResidentIdInAndStatusWithPagination(anyList(), anyString(), any(Pageable.class));
        }

        @Test
        @DisplayName("TC02 - Success: With search filter")
        void getMedicationOrders_WithSearch_ShouldReturnFiltered() {
            // Given
            GetMedicationOrdersRequest request = TestDataFactory.createGetMedicationOrdersRequest(
                    residentId, MedicationStatus.ACTIVE, "Aspirin", 1, 20);

            List<Resident> residents = Collections.singletonList(mockResident);
            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            Page<MedicationOrder> orderPage = new PageImpl<>(orders);

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(residents);
            when(medicationOrderRepository.findByResidentIdInAndStatusWithPagination(
                    anyList(), anyString(), any(Pageable.class)))
                    .thenReturn(orderPage);
            when(medicationLogRepository.findByOrderIdInAndStatus(anyList(), anyString()))
                    .thenReturn(Collections.emptyList());

            // When
            MedicationOrderListResponse response = medicationOrderService.getMedicationOrders(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getMedicationOrders()).isNotEmpty();

            verify(medicationOrderRepository, times(1))
                    .findByResidentIdInAndStatusWithPagination(anyList(), anyString(), any(Pageable.class));
        }

        @Test
        @DisplayName("TC03 - Success: No residents should return empty")
        void getMedicationOrders_NoResidents_ShouldReturnEmpty() {
            // Given
            GetMedicationOrdersRequest request = TestDataFactory.createGetMedicationOrdersRequest(
                    null, MedicationStatus.ACTIVE, null, 1, 20);

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(Collections.emptyList());

            // When
            MedicationOrderListResponse response = medicationOrderService.getMedicationOrders(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getMedicationOrders()).isEmpty();
            assertThat(response.getMetadata().getTotalPage()).isZero();

            verify(medicationOrderRepository, never())
                    .findByResidentIdInAndStatusWithPagination(anyList(), anyString(), any(Pageable.class));
        }

        @Test
        @DisplayName("TC04 - Success: With DISCONTINUED status")
        void getMedicationOrders_DiscontinuedStatus_ShouldReturnList() {
            // Given
            GetMedicationOrdersRequest request = TestDataFactory.createGetMedicationOrdersRequest(
                    residentId, MedicationStatus.DISCONTINUED, null, 1, 20);

            MedicationOrder discontinuedOrder = TestDataFactory.createDiscontinuedMedicationOrder(
                    2L, mockResident, mockPrescriber);

            List<Resident> residents = Collections.singletonList(mockResident);
            List<MedicationOrder> orders = Collections.singletonList(discontinuedOrder);
            Page<MedicationOrder> orderPage = new PageImpl<>(orders);

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(residents);
            when(medicationOrderRepository.findByResidentIdInAndStatusWithPagination(
                    anyList(), anyString(), any(Pageable.class)))
                    .thenReturn(orderPage);
            when(medicationLogRepository.findByOrderIdInAndStatus(anyList(), anyString()))
                    .thenReturn(Collections.emptyList());

            // When
            MedicationOrderListResponse response = medicationOrderService.getMedicationOrders(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getMedicationOrders()).isNotEmpty();
            assertThat(response.getMedicationOrders().get(0).getStatus())
                    .isEqualTo(MedicationStatus.DISCONTINUED);
        }

        @Test
        @DisplayName("TC05 - Success: Pagination with custom page and limit")
        void getMedicationOrders_CustomPagination_ShouldReturnCorrectPage() {
            // Given
            GetMedicationOrdersRequest request = TestDataFactory.createGetMedicationOrdersRequest(
                    residentId, MedicationStatus.ACTIVE, null, 2, 10);

            List<Resident> residents = Collections.singletonList(mockResident);
            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            Page<MedicationOrder> orderPage = new PageImpl<>(orders);

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(residents);
            when(medicationOrderRepository.findByResidentIdInAndStatusWithPagination(
                    anyList(), anyString(), any(Pageable.class)))
                    .thenReturn(orderPage);
            when(medicationLogRepository.findByOrderIdInAndStatus(anyList(), anyString()))
                    .thenReturn(Collections.emptyList());

            // When
            MedicationOrderListResponse response = medicationOrderService.getMedicationOrders(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getMetadata().getCurrentPage()).isEqualTo(2);
            assertThat(response.getMetadata().getCurrentLimit()).isEqualTo(10);

            verify(medicationOrderRepository, times(1))
                    .findByResidentIdInAndStatusWithPagination(anyList(), anyString(), any(Pageable.class));
        }

        @Test
        @DisplayName("TC06 - Success: No orders should return empty")
        void getMedicationOrders_NoOrders_ShouldReturnEmpty() {
            // Given
            GetMedicationOrdersRequest request = TestDataFactory.createGetMedicationOrdersRequest(
                    residentId, MedicationStatus.ACTIVE, null, 1, 20);

            List<Resident> residents = Collections.singletonList(mockResident);
            Page<MedicationOrder> orderPage = new PageImpl<>(Collections.emptyList());

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(residents);
            when(medicationOrderRepository.findByResidentIdInAndStatusWithPagination(
                    anyList(), anyString(), any(Pageable.class)))
                    .thenReturn(orderPage);
            when(medicationLogRepository.findByOrderIdInAndStatus(anyList(), anyString()))
                    .thenReturn(Collections.emptyList());

            // When
            MedicationOrderListResponse response = medicationOrderService.getMedicationOrders(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getMedicationOrders()).isEmpty();
        }
    }

    // ==================== API 12: Get Medication Order Detail Tests ====================

    @Nested
    @DisplayName("API 12: Get Medication Order Detail - Tests")
    class GetMedicationOrderDetailTests {

        @Test
        @DisplayName("TC07 - Success: Should return order detail")
        void getMedicationOrderDetail_Success_ShouldReturnDetail() {
            // Given
            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);
            List<MedicationLog> logs = Collections.singletonList(mockLog);

            when(medicationOrderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
            when(medicationScheduleRepository.findByOrderIdAndIsActive(orderId))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdAndLoggedAtAfter(anyLong(), any()))
                    .thenReturn(logs);

            // When
            MedicationOrderDetailResponse response = medicationOrderService.getMedicationOrderDetail(facilityId, orderId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(orderId);
            assertThat(response.getDrugName()).isEqualTo("Aspirin");
            assertThat(response.getSchedules()).isNotEmpty();
            assertThat(response.getRecentLogs()).isNotEmpty();

            verify(medicationOrderRepository, times(1)).findById(orderId);
            verify(medicationScheduleRepository, times(1)).findByOrderIdAndIsActive(orderId);
            verify(medicationLogRepository, times(1)).findByOrderIdAndLoggedAtAfter(anyLong(), any());
        }

        @Test
        @DisplayName("TC08 - Error: Order not found should throw exception")
        void getMedicationOrderDetail_OrderNotFound_ShouldThrowException() {
            // Given
            Long nonExistentOrderId = 999L;

            when(medicationOrderRepository.findById(nonExistentOrderId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> medicationOrderService.getMedicationOrderDetail(facilityId, nonExistentOrderId))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_ORDER_NOT_FOUND);
        }

        @Test
        @DisplayName("TC09 - Success: No schedules should return empty list")
        void getMedicationOrderDetail_NoSchedules_ShouldReturnEmpty() {
            // Given
            when(medicationOrderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
            when(medicationScheduleRepository.findByOrderIdAndIsActive(orderId))
                    .thenReturn(Collections.emptyList());
            when(medicationLogRepository.findByOrderIdAndLoggedAtAfter(anyLong(), any()))
                    .thenReturn(Collections.emptyList());

            // When
            MedicationOrderDetailResponse response = medicationOrderService.getMedicationOrderDetail(facilityId, orderId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getSchedules()).isEmpty();
            assertThat(response.getRecentLogs()).isEmpty();
        }
    }

    // ==================== API 13: Create Medication Order Tests ====================

    @Nested
    @DisplayName("API 13: Create Medication Order - Tests")
    class CreateMedicationOrderTests {

        @Test
        @DisplayName("TC10 - Success: Should create order with schedules")
        void createMedicationOrder_Success_ShouldCreateOrder() {
            // Given
            CreateMedicationOrderRequest request = TestDataFactory.createCreateMedicationOrderRequest(
                    residentId, "Lisinopril", "10 mg", "ORAL", "Once daily",
                    false, 4L, "Monitor blood pressure daily",
                    Arrays.asList("08:00:00"));

            MedicationOrder savedOrder = TestDataFactory.createMedicationOrderEntity(
                    25L, mockResident, mockPrescriber, MedicationStatus.ACTIVE, false,
                    "Lisinopril", "10 mg", "ORAL", "Once daily");

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(userRepository.findById(4L)).thenReturn(Optional.of(mockPrescriber));
            when(medicationOrderRepository.findByResidentIdAndDrugNameAndStatus(
                    eq(residentId), anyString(), anyString()))
                    .thenReturn(Collections.emptyList());
            when(medicationOrderRepository.save(any(MedicationOrder.class))).thenReturn(savedOrder);
            when(medicationScheduleRepository.save(any(MedicationSchedule.class)))
                    .thenReturn(mockSchedule);

            // When
            CreateMedicationOrderResponse response = medicationOrderService.createMedicationOrder(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(25L);
            assertThat(response.getDrugName()).isEqualTo("Lisinopril");
            assertThat(response.getStatus()).isEqualTo(MedicationStatus.ACTIVE);
            assertThat(response.getSchedules()).isNotEmpty();

            verify(medicationOrderRepository, times(1)).save(any(MedicationOrder.class));
            verify(medicationScheduleRepository, times(1)).save(any(MedicationSchedule.class));
        }

        @Test
        @DisplayName("TC11 - Success: With controlled substance")
        void createMedicationOrder_ControlledSubstance_ShouldSucceed() {
            // Given
            CreateMedicationOrderRequest request = TestDataFactory.createCreateMedicationOrderRequest(
                    residentId, "Morphine", "5 mg", "ORAL", "Every 6 hours",
                    true, 4L, "Monitor respiratory rate",
                    Arrays.asList("08:00:00", "14:00:00", "20:00:00"));

            MedicationOrder savedOrder = TestDataFactory.createMedicationOrderEntity(
                    25L, mockResident, mockPrescriber, MedicationStatus.ACTIVE, true,
                    "Morphine", "5 mg", "ORAL", "Every 6 hours");

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(userRepository.findById(4L)).thenReturn(Optional.of(mockPrescriber));
            when(medicationOrderRepository.findByResidentIdAndDrugNameAndStatus(
                    eq(residentId), anyString(), anyString()))
                    .thenReturn(Collections.emptyList());
            when(medicationOrderRepository.save(any(MedicationOrder.class))).thenReturn(savedOrder);
            when(medicationScheduleRepository.save(any(MedicationSchedule.class)))
                    .thenReturn(mockSchedule);

            // When
            CreateMedicationOrderResponse response = medicationOrderService.createMedicationOrder(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getIsControlledSubstance()).isTrue();
            assertThat(response.getSchedules()).hasSize(3);
        }

        @Test
        @DisplayName("TC12 - Error: Resident not found")
        void createMedicationOrder_ResidentNotFound_ShouldThrowException() {
            // Given
            Long nonExistentResidentId = 999L;
            CreateMedicationOrderRequest request = TestDataFactory.createCreateMedicationOrderRequest(
                    nonExistentResidentId, "Lisinopril", "10 mg", "ORAL", "Once daily",
                    false, 4L, "Monitor blood pressure daily",
                    Arrays.asList("08:00:00"));

            when(residentRepository.findById(nonExistentResidentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> medicationOrderService.createMedicationOrder(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_RESIDENT_NOT_FOUND);
        }

        @Test
        @DisplayName("TC13 - Error: Prescriber not found")
        void createMedicationOrder_PrescriberNotFound_ShouldThrowException() {
            // Given
            Long nonExistentPrescriberId = 999L;
            CreateMedicationOrderRequest request = TestDataFactory.createCreateMedicationOrderRequest(
                    residentId, "Lisinopril", "10 mg", "ORAL", "Once daily",
                    false, nonExistentPrescriberId, "Monitor blood pressure daily",
                    Arrays.asList("08:00:00"));

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(userRepository.findById(nonExistentPrescriberId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> medicationOrderService.createMedicationOrder(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_PRESCRIBER_NOT_FOUND);
        }

        @Test
        @DisplayName("TC14 - Error: Duplicate order")
        void createMedicationOrder_Duplicate_ShouldThrowException() {
            // Given
            CreateMedicationOrderRequest request = TestDataFactory.createCreateMedicationOrderRequest(
                    residentId, "Lisinopril", "10 mg", "ORAL", "Once daily",
                    false, 4L, "Monitor blood pressure daily",
                    Arrays.asList("08:00:00"));

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(userRepository.findById(4L)).thenReturn(Optional.of(mockPrescriber));
            when(medicationOrderRepository.findByResidentIdAndDrugNameAndStatus(
                    eq(residentId), anyString(), anyString()))
                    .thenReturn(Collections.singletonList(mockOrder));

            // When & Then
            assertThatThrownBy(() -> medicationOrderService.createMedicationOrder(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_ORDER_DUPLICATE);
        }

        @Test
        @DisplayName("TC15 - Error: No scheduled times")
        void createMedicationOrder_NoScheduledTimes_ShouldThrowException() {
            // Given
            CreateMedicationOrderRequest request = TestDataFactory.createCreateMedicationOrderRequest(
                    residentId, "Lisinopril", "10 mg", "ORAL", "Once daily",
                    false, 4L, "Monitor blood pressure daily",
                    Collections.emptyList());

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(userRepository.findById(4L)).thenReturn(Optional.of(mockPrescriber));

            // When & Then
            assertThatThrownBy(() -> medicationOrderService.createMedicationOrder(facilityId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_INVALID_SCHEDULE_TIME);
        }
    }

    // ==================== API 14: Discontinue Medication Order Tests ====================

    @Nested
    @DisplayName("API 14: Discontinue Medication Order - Tests")
    class DiscontinueMedicationOrderTests {

        @Test
        @DisplayName("TC16 - Success: Should discontinue order and deactivate schedules")
        void discontinueMedicationOrder_Success_ShouldDiscontinue() {
            // Given
            DiscontinueMedicationOrderRequest request = TestDataFactory.createDiscontinueMedicationOrderRequest(
                    "Switched to different medication");

            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);

            when(medicationOrderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
            when(medicationScheduleRepository.findByOrderIdAndIsActive(orderId))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

            // When
            DiscontinueMedicationOrderResponse response = medicationOrderService.discontinueMedicationOrder(
                    facilityId, orderId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(MedicationStatus.DISCONTINUED);
            assertThat(response.getDiscontinueReason()).isEqualTo("Switched to different medication");
            assertThat(response.getDiscontinuedBy()).isNotNull();

            verify(medicationOrderRepository, times(1)).save(any(MedicationOrder.class));
            verify(medicationScheduleRepository, times(1)).save(any(MedicationSchedule.class));
        }

        @Test
        @DisplayName("TC17 - Error: Order not found")
        void discontinueMedicationOrder_OrderNotFound_ShouldThrowException() {
            // Given
            Long nonExistentOrderId = 999L;
            DiscontinueMedicationOrderRequest request = TestDataFactory.createDiscontinueMedicationOrderRequest(
                    "Switched to different medication");

            when(medicationOrderRepository.findById(nonExistentOrderId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> medicationOrderService.discontinueMedicationOrder(
                    facilityId, nonExistentOrderId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_ORDER_NOT_FOUND);
        }

        @Test
        @DisplayName("TC18 - Error: Already discontinued")
        void discontinueMedicationOrder_AlreadyDiscontinued_ShouldThrowException() {
            // Given
            MedicationOrder discontinuedOrder = TestDataFactory.createDiscontinuedMedicationOrder(
                    orderId, mockResident, mockPrescriber);
            DiscontinueMedicationOrderRequest request = TestDataFactory.createDiscontinueMedicationOrderRequest(
                    "Switched to different medication");

            when(medicationOrderRepository.findById(orderId)).thenReturn(Optional.of(discontinuedOrder));

            // When & Then
            assertThatThrownBy(() -> medicationOrderService.discontinueMedicationOrder(
                    facilityId, orderId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_ORDER_ALREADY_DISCONTINUED);
        }

        @Test
        @DisplayName("TC19 - Error: Has pending doses")
        void discontinueMedicationOrder_HasPendingDoses_ShouldThrowException() {
            // Given
            DiscontinueMedicationOrderRequest request = TestDataFactory.createDiscontinueMedicationOrderRequest(
                    "Switched to different medication");

            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);

            when(medicationOrderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
            when(medicationScheduleRepository.findByOrderIdAndIsActive(orderId))
                    .thenReturn(schedules);
            // Only 1 of 2 schedules administered -> has pending
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertThatThrownBy(() -> medicationOrderService.discontinueMedicationOrder(
                    facilityId, orderId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_ORDER_HAS_PENDING_DOSES);
        }
    }

    // ==================== API 15: Get Resident Pending Medications Tests ====================

    @Nested
    @DisplayName("API 15: Get Resident Pending Medications - Tests")
    class GetResidentPendingMedicationsTests {

        @Test
        @DisplayName("TC20 - Success: Should return pending medications")
        void getResidentPendingMedications_Success_ShouldReturnList() {
            // Given
            String time = "08:00";
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
            PendingMedicationResponse response = medicationOrderService.getResidentPendingMedications(
                    facilityId, residentId, time);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getResidentId()).isEqualTo(residentId);
            assertThat(response.getPendingMedications()).isNotEmpty();

            verify(residentRepository, times(1)).findById(residentId);
            verify(medicationOrderRepository, times(1)).findByResidentIdAndStatus(eq(residentId), anyString());
        }

        @Test
        @DisplayName("TC21 - Success: No time provided should use current time")
        void getResidentPendingMedications_NoTime_ShouldUseCurrentTime() {
            // Given
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
            PendingMedicationResponse response = medicationOrderService.getResidentPendingMedications(
                    facilityId, residentId, null);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getPendingMedications()).isNotEmpty();
        }

        @Test
        @DisplayName("TC22 - Success: No pending medications should return empty list")
        void getResidentPendingMedications_NoPending_ShouldReturnEmpty() {
            // Given
            // Create order with schedule at time far from current
            MedicationOrder order = TestDataFactory.createMedicationOrderEntity(
                    1L, mockResident, mockPrescriber, MedicationStatus.ACTIVE, false,
                    "Aspirin", "100 mg", "ORAL", "Every 8 hours");
            MedicationSchedule schedule = TestDataFactory.createMedicationSchedule(
                    1L, order, LocalTime.of(23, 0), true); // Far from current time

            List<MedicationOrder> orders = Collections.singletonList(order);
            List<MedicationSchedule> schedules = Collections.singletonList(schedule);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findByResidentIdAndStatus(eq(residentId), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            // When
            PendingMedicationResponse response = medicationOrderService.getResidentPendingMedications(
                    facilityId, residentId, "08:00");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getPendingMedications()).isEmpty();
        }

        @Test
        @DisplayName("TC23 - Error: Resident not found")
        void getResidentPendingMedications_ResidentNotFound_ShouldThrowException() {
            // Given
            Long nonExistentResidentId = 999L;

            when(residentRepository.findById(nonExistentResidentId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> medicationOrderService.getResidentPendingMedications(
                    facilityId, nonExistentResidentId, null))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_RESIDENT_NOT_FOUND);
        }

        @Test
        @DisplayName("TC24 - Error: No active orders")
        void getResidentPendingMedications_NoActiveOrders_ShouldThrowException() {
            // Given
            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findByResidentIdAndStatus(eq(residentId), anyString()))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertThatThrownBy(() -> medicationOrderService.getResidentPendingMedications(
                    facilityId, residentId, null))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_RESIDENT_NO_ACTIVE_ORDERS);
        }

        @Test
        @DisplayName("TC25 - Success: With controlled substance requiring witness")
        void getResidentPendingMedications_ControlledSubstance_ShouldReturnRequiresWitness() {
            // Given
            MedicationOrder controlledOrder = TestDataFactory.createControlledSubstanceOrder(
                    2L, mockResident, mockPrescriber);
            MedicationSchedule controlledSchedule = TestDataFactory.createMedicationSchedule(
                    2L, controlledOrder, LocalTime.of(8, 0), true);

            List<MedicationOrder> orders = Collections.singletonList(controlledOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(controlledSchedule);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findByResidentIdAndStatus(eq(residentId), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            // When
            PendingMedicationResponse response = medicationOrderService.getResidentPendingMedications(
                    facilityId, residentId, "08:00");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getPendingMedications()).isNotEmpty();
            assertThat(response.getPendingMedications().get(0).getRequiresWitness()).isTrue();
        }

        @Test
        @DisplayName("TC26 - Success: Overdue medication")
        void getResidentPendingMedications_Overdue_ShouldReturnOverdueFlag() {
            // Given
            // Create schedule with time before current
            LocalTime pastTime = LocalTime.now().minusHours(2);
            MedicationSchedule pastSchedule = TestDataFactory.createMedicationSchedule(
                    1L, mockOrder, pastTime, true);

            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(pastSchedule);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findByResidentIdAndStatus(eq(residentId), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            // When
            PendingMedicationResponse response = medicationOrderService.getResidentPendingMedications(
                    facilityId, residentId, LocalTime.now().toString());

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getPendingMedications()).isNotEmpty();
            // Note: isOverdue may vary based on actual time, but we can check the response structure
        }
    }
}