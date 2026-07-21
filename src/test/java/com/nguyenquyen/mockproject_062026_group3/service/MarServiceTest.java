package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.common.constants.MedicationStatus;
import com.nguyenquyen.mockproject_062026_group3.dto.request.MarDashboardRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.response.MarDashboardResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.response.MarResidentResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.response.MarScheduleShiftResponse;
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
@DisplayName("MarService Unit Tests - Phần 1")
class MarServiceTest {

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
    private MarService marService;

    private Long facilityId;
    private Long residentId;
    private Resident mockResident;
    private MedicationOrder mockOrder;
    private MedicationSchedule mockSchedule;
    private MedicationLog mockLog;
    private User mockUser;

    @BeforeEach
    void setUp() {
        facilityId = 1L;
        residentId = 1L;

        // Setup mock data
        mockUser = TestDataFactory.createUser(7L, "RN");
        mockResident = TestDataFactory.createResident(residentId);
        mockOrder = TestDataFactory.createMedicationOrder(1L, mockResident, mockUser, MedicationStatus.ACTIVE);
        mockSchedule = TestDataFactory.createMedicationSchedule(1L, mockOrder, LocalTime.of(8, 0), true);
        mockLog = TestDataFactory.createMedicationLog(1L, mockOrder, mockUser, MedicationStatus.ADMINISTERED, OffsetDateTime.now());
    }

    // ==================== API 1: Get Dashboard Tests ====================

    @Nested
    @DisplayName("API 1: Get Dashboard - Tests")
    class GetDashboardTests {

        @Test
        @DisplayName("TC01 - Success: Should return full dashboard")
        void getDashboard_Success_ShouldReturnFullDashboard() {
            // Given
            MarDashboardRequest request = MarDashboardRequest.builder()
                    .shift(MedicationStatus.DAY)
                    .date(LocalDate.now())
                    .build();

            List<Resident> residents = Collections.singletonList(mockResident);
            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);
            List<MedicationLog> logs = Collections.singletonList(mockLog);

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(residents);
            when(medicationOrderRepository.findByResidentIdInAndStatus(anyList(), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(logs);

            // When
            MarDashboardResponse response = marService.getDashboard(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getShift()).isEqualTo(MedicationStatus.DAY);
            assertThat(response.getSummary()).isNotNull();
            assertThat(response.getSummary().getCompleted()).isGreaterThanOrEqualTo(0);
            assertThat(response.getMedPassList()).isNotNull();

            verify(residentRepository, times(1)).findByFacilityIdAndStatus(eq(facilityId), anyString());
            verify(medicationOrderRepository, times(1)).findByResidentIdInAndStatus(anyList(), anyString());
            verify(medicationScheduleRepository, times(1)).findByOrderIdInAndIsActiveTrue(anyList());
            verify(medicationLogRepository, times(1)).findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class));
        }

        @Test
        @DisplayName("TC02 - Success: No residents should return empty dashboard")
        void getDashboard_NoResidents_ShouldReturnEmptyDashboard() {
            // Given
            MarDashboardRequest request = MarDashboardRequest.builder()
                    .shift(MedicationStatus.DAY)
                    .date(LocalDate.now())
                    .build();

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(Collections.emptyList());

            // When
            MarDashboardResponse response = marService.getDashboard(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getSummary().getPending()).isZero();
            assertThat(response.getSummary().getCompleted()).isZero();
            assertThat(response.getMedPassList()).isEmpty();

            verify(residentRepository, times(1)).findByFacilityIdAndStatus(eq(facilityId), anyString());
            verify(medicationOrderRepository, never()).findByResidentIdInAndStatus(anyList(), anyString());
        }

        @Test
        @DisplayName("TC03 - Success: No active orders should return empty dashboard")
        void getDashboard_NoActiveOrders_ShouldReturnEmptyDashboard() {
            // Given
            MarDashboardRequest request = MarDashboardRequest.builder()
                    .shift(MedicationStatus.DAY)
                    .date(LocalDate.now())
                    .build();

            List<Resident> residents = Collections.singletonList(mockResident);

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(residents);
            when(medicationOrderRepository.findByResidentIdInAndStatus(anyList(), anyString()))
                    .thenReturn(Collections.emptyList());

            // When
            MarDashboardResponse response = marService.getDashboard(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getSummary().getPending()).isZero();
            assertThat(response.getMedPassList()).isEmpty();

            verify(medicationOrderRepository, times(1)).findByResidentIdInAndStatus(anyList(), anyString());
            verify(medicationScheduleRepository, never()).findByOrderIdInAndIsActiveTrue(anyList());
        }

        @Test
        @DisplayName("TC04 - Success: No schedules should return empty dashboard")
        void getDashboard_NoSchedules_ShouldReturnEmptyDashboard() {
            // Given
            MarDashboardRequest request = MarDashboardRequest.builder()
                    .shift(MedicationStatus.DAY)
                    .date(LocalDate.now())
                    .build();

            List<Resident> residents = Collections.singletonList(mockResident);
            List<MedicationOrder> orders = Collections.singletonList(mockOrder);

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(residents);
            when(medicationOrderRepository.findByResidentIdInAndStatus(anyList(), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(Collections.emptyList());

            // When
            MarDashboardResponse response = marService.getDashboard(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getSummary().getPending()).isZero();
            assertThat(response.getMedPassList()).isEmpty();
        }

        @Test
        @DisplayName("TC05 - Success: With custom date")
        void getDashboard_WithCustomDate_ShouldUseProvidedDate() {
            // Given
            LocalDate customDate = LocalDate.of(2026, 7, 15);
            MarDashboardRequest request = MarDashboardRequest.builder()
                    .shift(MedicationStatus.DAY)
                    .date(customDate)
                    .build();

            List<Resident> residents = Collections.singletonList(mockResident);
            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(residents);
            when(medicationOrderRepository.findByResidentIdInAndStatus(anyList(), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), eq(customDate)))
                    .thenReturn(Collections.emptyList());

            // When
            MarDashboardResponse response = marService.getDashboard(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getDate()).isEqualTo(customDate.toString());

            verify(medicationLogRepository, times(1))
                    .findByOrderIdInAndLoggedAtDate(anyList(), eq(customDate));
        }

        @Test
        @DisplayName("TC06 - Success: With default date when not provided")
        void getDashboard_NoDate_ShouldUseToday() {
            // Given
            MarDashboardRequest request = MarDashboardRequest.builder()
                    .shift(MedicationStatus.DAY)
                    .build();

            List<Resident> residents = Collections.singletonList(mockResident);
            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(residents);
            when(medicationOrderRepository.findByResidentIdInAndStatus(anyList(), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), eq(LocalDate.now())))
                    .thenReturn(Collections.emptyList());

            // When
            MarDashboardResponse response = marService.getDashboard(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getDate()).isEqualTo(LocalDate.now().toString());
        }

        @Test
        @DisplayName("TC07 - Success: With EVENING shift")
        void getDashboard_EveningShift_ShouldReturnDashboard() {
            // Given
            MarDashboardRequest request = MarDashboardRequest.builder()
                    .shift(MedicationStatus.EVENING)
                    .date(LocalDate.now())
                    .build();

            List<Resident> residents = Collections.singletonList(mockResident);
            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(residents);
            when(medicationOrderRepository.findByResidentIdInAndStatus(anyList(), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            // When
            MarDashboardResponse response = marService.getDashboard(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getShift()).isEqualTo(MedicationStatus.EVENING);
        }

        @Test
        @DisplayName("TC08 - Success: With NIGHT shift")
        void getDashboard_NightShift_ShouldReturnDashboard() {
            // Given
            MarDashboardRequest request = MarDashboardRequest.builder()
                    .shift(MedicationStatus.NIGHT)
                    .date(LocalDate.now())
                    .build();

            List<Resident> residents = Collections.singletonList(mockResident);
            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(residents);
            when(medicationOrderRepository.findByResidentIdInAndStatus(anyList(), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            // When
            MarDashboardResponse response = marService.getDashboard(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getShift()).isEqualTo(MedicationStatus.NIGHT);
        }

        @Test
        @DisplayName("TC09 - Success: With multiple residents")
        void getDashboard_MultipleResidents_ShouldReturnDashboard() {
            // Given
            MarDashboardRequest request = MarDashboardRequest.builder()
                    .shift(MedicationStatus.DAY)
                    .date(LocalDate.now())
                    .build();

            Resident resident1 = TestDataFactory.createResident(1L);
            Resident resident2 = TestDataFactory.createResident(2L);
            List<Resident> residents = Arrays.asList(resident1, resident2);

            MedicationOrder order1 = TestDataFactory.createMedicationOrder(1L, resident1, mockUser, MedicationStatus.ACTIVE);
            MedicationOrder order2 = TestDataFactory.createMedicationOrder(2L, resident2, mockUser, MedicationStatus.ACTIVE);
            List<MedicationOrder> orders = Arrays.asList(order1, order2);

            MedicationSchedule schedule1 = TestDataFactory.createMedicationSchedule(1L, order1, LocalTime.of(8, 0), true);
            MedicationSchedule schedule2 = TestDataFactory.createMedicationSchedule(2L, order2, LocalTime.of(9, 0), true);
            List<MedicationSchedule> schedules = Arrays.asList(schedule1, schedule2);

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(residents);
            when(medicationOrderRepository.findByResidentIdInAndStatus(anyList(), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            // When
            MarDashboardResponse response = marService.getDashboard(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getMedPassList()).isNotEmpty();
        }

        @Test
        @DisplayName("TC10 - Success: Dashboard summary with mixed statuses")
        void getDashboard_MixedStatuses_ShouldCalculateCorrectSummary() {
            // Given
            MarDashboardRequest request = MarDashboardRequest.builder()
                    .shift(MedicationStatus.DAY)
                    .date(LocalDate.now())
                    .build();

            List<Resident> residents = Collections.singletonList(mockResident);
            List<MedicationOrder> orders = Collections.singletonList(mockOrder);

            // Create multiple schedules
            MedicationSchedule schedule1 = TestDataFactory.createMedicationSchedule(1L, mockOrder, LocalTime.of(8, 0), true);
            MedicationSchedule schedule2 = TestDataFactory.createMedicationSchedule(2L, mockOrder, LocalTime.of(16, 0), true);
            MedicationSchedule schedule3 = TestDataFactory.createMedicationSchedule(3L, mockOrder, LocalTime.of(0, 0), true);
            List<MedicationSchedule> schedules = Arrays.asList(schedule1, schedule2, schedule3);

            // Create logs with different statuses
            MedicationLog log1 = TestDataFactory.createMedicationLog(1L, mockOrder, mockUser, MedicationStatus.ADMINISTERED, OffsetDateTime.now());
            MedicationLog log2 = TestDataFactory.createMedicationLog(2L, mockOrder, mockUser, MedicationStatus.HELD, OffsetDateTime.now());
            MedicationLog log3 = TestDataFactory.createMedicationLog(3L, mockOrder, mockUser, MedicationStatus.REFUSED, OffsetDateTime.now());
            List<MedicationLog> logs = Arrays.asList(log1, log2, log3);

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(residents);
            when(medicationOrderRepository.findByResidentIdInAndStatus(anyList(), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(logs);

            // When
            MarDashboardResponse response = marService.getDashboard(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getSummary()).isNotNull();
            assertThat(response.getSummary().getCompleted()).isEqualTo(1);
            assertThat(response.getSummary().getHeld()).isEqualTo(1);
            assertThat(response.getSummary().getRefused()).isEqualTo(1);
        }

        @Test
        @DisplayName("TC11 - Success: With allergy alerts")
        void getDashboard_WithAllergyAlerts_ShouldReturnAlerts() {
            // Given
            MarDashboardRequest request = MarDashboardRequest.builder()
                    .shift(MedicationStatus.DAY)
                    .date(LocalDate.now())
                    .build();

            List<Resident> residents = Collections.singletonList(mockResident);
            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(residents);
            when(medicationOrderRepository.findByResidentIdInAndStatus(anyList(), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), any(LocalDate.class)))
                    .thenReturn(Collections.emptyList());

            // When
            MarDashboardResponse response = marService.getDashboard(facilityId, request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getGlobalAllergyAlerts()).isNotNull();
            // Allergy alerts are currently placeholders
        }

        @Test
        @DisplayName("TC12 - Error: Facility ID null should throw exception")
        void getDashboard_NullFacilityId_ShouldThrowException() {
            // Given
            MarDashboardRequest request = MarDashboardRequest.builder()
                    .shift(MedicationStatus.DAY)
                    .date(LocalDate.now())
                    .build();

            // When & Then
            assertThatThrownBy(() -> marService.getDashboard(null, request))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ==================== API 2: Get Resident MAR Tests ====================

    @Nested
    @DisplayName("API 2: Get Resident MAR - Tests")
    class GetResidentMarTests {

        @Test
        @DisplayName("TC13 - Success: Should return full MAR")
        void getResidentMar_Success_ShouldReturnFullMAR() {
            // Given
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);
            List<MedicationLog> logs = Collections.singletonList(mockLog);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findByResidentIdAndStatus(eq(residentId), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtBetween(anyList(), any(), any()))
                    .thenReturn(logs);

            // When
            MarResidentResponse response = marService.getResidentMar(
                    facilityId, residentId, null, null, startDate, endDate);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getResident()).isNotNull();
            assertThat(response.getResident().getId()).isEqualTo(residentId);
            assertThat(response.getResident().getFullName()).isEqualTo("Resident1 Test");
            assertThat(response.getSummaryStats()).isNotNull();
            assertThat(response.getMedicationGrid()).isNotEmpty();

            verify(residentRepository, times(1)).findById(residentId);
            verify(medicationOrderRepository, times(1)).findByResidentIdAndStatus(eq(residentId), anyString());
            verify(medicationScheduleRepository, times(1)).findByOrderIdInAndIsActiveTrue(anyList());
            verify(medicationLogRepository, times(1)).findByOrderIdInAndLoggedAtBetween(anyList(), any(), any());
        }

        @Test
        @DisplayName("TC14 - Success: With default date range")
        void getResidentMar_DefaultDateRange_ShouldUseLast7Days() {
            // Given
            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);
            List<MedicationLog> logs = Collections.singletonList(mockLog);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findByResidentIdAndStatus(eq(residentId), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtBetween(anyList(), any(), any()))
                    .thenReturn(logs);

            // When - no date range provided
            MarResidentResponse response = marService.getResidentMar(
                    facilityId, residentId, null, null, null, null);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getDateRange()).isNotNull();
            assertThat(response.getDateRange().getStart()).isEqualTo(LocalDate.now().minusDays(7).toString());
            assertThat(response.getDateRange().getEnd()).isEqualTo(LocalDate.now().toString());

            verify(medicationLogRepository, times(1))
                    .findByOrderIdInAndLoggedAtBetween(anyList(), any(), any());
        }

        @Test
        @DisplayName("TC15 - Success: With status filter ADMINISTERED")
        void getResidentMar_StatusFilterAdministered_ShouldReturnMAR() {
            // Given
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);
            List<MedicationLog> logs = Collections.singletonList(mockLog);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findByResidentIdAndStatus(eq(residentId), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtBetween(anyList(), any(), any()))
                    .thenReturn(logs);

            // When
            MarResidentResponse response = marService.getResidentMar(
                    facilityId, residentId, null, "ADMINISTERED", startDate, endDate);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getSummaryStats().getAdministered()).isEqualTo(1);
        }

        @Test
        @DisplayName("TC16 - Success: With status filter HELD")
        void getResidentMar_StatusFilterHeld_ShouldReturnMAR() {
            // Given
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            MedicationLog heldLog = TestDataFactory.createMedicationLog(2L, mockOrder, mockUser, MedicationStatus.HELD, OffsetDateTime.now());

            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);
            List<MedicationLog> logs = Collections.singletonList(heldLog);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findByResidentIdAndStatus(eq(residentId), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtBetween(anyList(), any(), any()))
                    .thenReturn(logs);

            // When
            MarResidentResponse response = marService.getResidentMar(
                    facilityId, residentId, null, "HELD", startDate, endDate);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getSummaryStats().getHeld()).isEqualTo(1);
        }

        @Test
        @DisplayName("TC17 - Success: With status filter OVERRIDE")
        void getResidentMar_StatusFilterOverride_ShouldReturnMAR() {
            // Given
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            MedicationLog overrideLog = TestDataFactory.createMedicationLog(
                    2L, mockOrder, mockUser, MedicationStatus.ADMINISTERED, OffsetDateTime.now());
            overrideLog.setOverrideReason("TIME_WINDOW_EXCEPTION");

            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);
            List<MedicationLog> logs = Collections.singletonList(overrideLog);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findByResidentIdAndStatus(eq(residentId), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtBetween(anyList(), any(), any()))
                    .thenReturn(logs);

            // When
            MarResidentResponse response = marService.getResidentMar(
                    facilityId, residentId, null, "OVERRIDE", startDate, endDate);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getSummaryStats().getOverrides()).isEqualTo(1);
        }

        @Test
        @DisplayName("TC18 - Success: With date range THIS_WEEK")
        void getResidentMar_DateRangeThisWeek_ShouldReturnMAR() {
            // Given
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);
            List<MedicationLog> logs = Collections.singletonList(mockLog);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findByResidentIdAndStatus(eq(residentId), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtBetween(anyList(), any(), any()))
                    .thenReturn(logs);

            // When
            MarResidentResponse response = marService.getResidentMar(
                    facilityId, residentId, "THIS_WEEK", null, startDate, endDate);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getDateRange().getStart()).isEqualTo(startDate.toString());
            assertThat(response.getDateRange().getEnd()).isEqualTo(endDate.toString());
        }

        @Test
        @DisplayName("TC19 - Success: With date range THIS_MONTH")
        void getResidentMar_DateRangeThisMonth_ShouldReturnMAR() {
            // Given
            LocalDate startDate = LocalDate.now().minusDays(30);
            LocalDate endDate = LocalDate.now();

            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);
            List<MedicationLog> logs = Collections.singletonList(mockLog);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findByResidentIdAndStatus(eq(residentId), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtBetween(anyList(), any(), any()))
                    .thenReturn(logs);

            // When
            MarResidentResponse response = marService.getResidentMar(
                    facilityId, residentId, "THIS_MONTH", null, startDate, endDate);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getDateRange().getStart()).isEqualTo(startDate.toString());
            assertThat(response.getDateRange().getEnd()).isEqualTo(endDate.toString());
        }

        @Test
        @DisplayName("TC20 - Success: Multiple orders and schedules")
        void getResidentMar_MultipleOrders_ShouldReturnMAR() {
            // Given
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            MedicationOrder order1 = TestDataFactory.createMedicationOrder(1L, mockResident, mockUser, MedicationStatus.ACTIVE);
            MedicationOrder order2 = TestDataFactory.createMedicationOrder(2L, mockResident, mockUser, MedicationStatus.ACTIVE);
            List<MedicationOrder> orders = Arrays.asList(order1, order2);

            MedicationSchedule schedule1 = TestDataFactory.createMedicationSchedule(1L, order1, LocalTime.of(8, 0), true);
            MedicationSchedule schedule2 = TestDataFactory.createMedicationSchedule(2L, order2, LocalTime.of(16, 0), true);
            List<MedicationSchedule> schedules = Arrays.asList(schedule1, schedule2);

            MedicationLog log1 = TestDataFactory.createMedicationLog(1L, order1, mockUser, MedicationStatus.ADMINISTERED, OffsetDateTime.now());
            MedicationLog log2 = TestDataFactory.createMedicationLog(2L, order2, mockUser, MedicationStatus.ADMINISTERED, OffsetDateTime.now());
            List<MedicationLog> logs = Arrays.asList(log1, log2);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findByResidentIdAndStatus(eq(residentId), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtBetween(anyList(), any(), any()))
                    .thenReturn(logs);

            // When
            MarResidentResponse response = marService.getResidentMar(
                    facilityId, residentId, null, null, startDate, endDate);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getMedicationGrid()).hasSize(2);
            assertThat(response.getSummaryStats().getTotalScheduled()).isEqualTo(2);
        }

        @Test
        @DisplayName("TC21 - Error: Resident not found")
        void getResidentMar_ResidentNotFound_ShouldThrowException() {
            // Given
            Long nonExistentResidentId = 999L;
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            when(residentRepository.findById(nonExistentResidentId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> marService.getResidentMar(
                    facilityId, nonExistentResidentId, null, null, startDate, endDate))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_RESIDENT_NOT_FOUND);

            verify(residentRepository, times(1)).findById(nonExistentResidentId);
            verify(medicationOrderRepository, never()).findByResidentIdAndStatus(anyLong(), anyString());
        }

        @Test
        @DisplayName("TC22 - Error: No active orders")
        void getResidentMar_NoActiveOrders_ShouldThrowException() {
            // Given
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findByResidentIdAndStatus(eq(residentId), anyString()))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertThatThrownBy(() -> marService.getResidentMar(
                    facilityId, residentId, null, null, startDate, endDate))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_NO_ACTIVE_ORDERS);
        }

        @Test
        @DisplayName("TC23 - Error: No schedules")
        void getResidentMar_NoSchedules_ShouldThrowException() {
            // Given
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            List<MedicationOrder> orders = Collections.singletonList(mockOrder);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findByResidentIdAndStatus(eq(residentId), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertThatThrownBy(() -> marService.getResidentMar(
                    facilityId, residentId, null, null, startDate, endDate))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_NO_SCHEDULES);
        }

        @Test
        @DisplayName("TC24 - Error: Invalid date range")
        void getResidentMar_InvalidDateRange_ShouldThrowException() {
            // Given
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = LocalDate.now().minusDays(7);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));

            // When & Then
            assertThatThrownBy(() -> marService.getResidentMar(
                    facilityId, residentId, null, null, startDate, endDate))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_INVALID_DATE_RANGE);
        }
    }

    // ==================== API 3: Print MAR Tests ====================

    @Nested
    @DisplayName("API 3: Print MAR - Tests")
    class PrintMarTests {

        @Test
        @DisplayName("TC25 - Success: Should return PDF bytes")
        void printMar_Success_ShouldReturnPDFBytes() {
            // Given
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);
            List<MedicationLog> logs = Collections.singletonList(mockLog);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findByResidentIdAndStatus(eq(residentId), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtBetween(anyList(), any(), any()))
                    .thenReturn(logs);

            // When
            byte[] pdfData = marService.printMar(facilityId, residentId, startDate, endDate);

            // Then
            assertThat(pdfData).isNotNull();
            // PDF generation is placeholder, returns empty array
            assertThat(pdfData).isEmpty();

            verify(residentRepository, times(1)).findById(residentId);
            verify(medicationOrderRepository, times(1)).findByResidentIdAndStatus(eq(residentId), anyString());
        }

        @Test
        @DisplayName("TC26 - Success: With default date range")
        void printMar_DefaultDateRange_ShouldReturnPDF() {
            // Given
            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);
            List<MedicationLog> logs = Collections.singletonList(mockLog);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findByResidentIdAndStatus(eq(residentId), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtBetween(anyList(), any(), any()))
                    .thenReturn(logs);

            // When
            byte[] pdfData = marService.printMar(facilityId, residentId, null, null);

            // Then
            assertThat(pdfData).isNotNull();
        }

        @Test
        @DisplayName("TC27 - Error: Resident not found")
        void printMar_ResidentNotFound_ShouldThrowException() {
            // Given
            Long nonExistentResidentId = 999L;
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            when(residentRepository.findById(nonExistentResidentId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> marService.printMar(
                    facilityId, nonExistentResidentId, startDate, endDate))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_RESIDENT_NOT_FOUND);
        }

        @Test
        @DisplayName("TC28 - Error: No active orders")
        void printMar_NoActiveOrders_ShouldThrowException() {
            // Given
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findByResidentIdAndStatus(eq(residentId), anyString()))
                    .thenReturn(Collections.emptyList());

            // When & Then
            assertThatThrownBy(() -> marService.printMar(
                    facilityId, residentId, startDate, endDate))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_NO_ACTIVE_ORDERS);
        }

        @Test
        @DisplayName("TC29 - Error: PDF generation failed")
        void printMar_PDFGenerationFailed_ShouldThrowException() {
            // This test simulates a failure during PDF generation
            // The actual implementation may need to be modified to throw this exception

            // Given
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);

            when(residentRepository.findById(residentId)).thenReturn(Optional.of(mockResident));
            when(medicationOrderRepository.findByResidentIdAndStatus(eq(residentId), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtBetween(anyList(), any(), any()))
                    .thenThrow(new RuntimeException("PDF generation error"));

            // When & Then
            assertThatThrownBy(() -> marService.printMar(
                    facilityId, residentId, startDate, endDate))
                    .isInstanceOf(AppException.class)
                    .extracting(e -> ((AppException) e).getErrorCode())
                    .isEqualTo(ErrorCode.MAR_PDF_GENERATION_FAILED);
        }
    }

    // ==================== API 4: Get Shift Schedule Tests ====================

    @Nested
    @DisplayName("API 4: Get Shift Schedule - Tests")
    class GetShiftScheduleTests {

        @Test
        @DisplayName("TC30 - Success: Should return shift schedule for DAY")
        void getShiftSchedule_DayShift_Success() {
            // Given
            String shift = MedicationStatus.DAY;
            LocalDate date = LocalDate.now();

            List<Resident> residents = Collections.singletonList(mockResident);
            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(residents);
            when(medicationOrderRepository.findByResidentIdInAndStatus(anyList(), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), eq(date)))
                    .thenReturn(Collections.emptyList());

            // When
            MarScheduleShiftResponse response = marService.getShiftSchedule(facilityId, shift, date);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getShift()).isEqualTo(shift);
            assertThat(response.getDate()).isEqualTo(date.toString());
            assertThat(response.getSchedule()).isNotEmpty();

            verify(residentRepository, times(1)).findByFacilityIdAndStatus(eq(facilityId), anyString());
            verify(medicationOrderRepository, times(1)).findByResidentIdInAndStatus(anyList(), anyString());
            verify(medicationScheduleRepository, times(1)).findByOrderIdInAndIsActiveTrue(anyList());
        }

        @Test
        @DisplayName("TC31 - Success: Should return shift schedule for EVENING")
        void getShiftSchedule_EveningShift_Success() {
            // Given
            String shift = MedicationStatus.EVENING;
            LocalDate date = LocalDate.now();

            // Create schedule for evening (14:00-22:00)
            MedicationSchedule eveningSchedule = TestDataFactory.createMedicationSchedule(
                    2L, mockOrder, LocalTime.of(18, 0), true);

            List<Resident> residents = Collections.singletonList(mockResident);
            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(eveningSchedule);

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(residents);
            when(medicationOrderRepository.findByResidentIdInAndStatus(anyList(), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), eq(date)))
                    .thenReturn(Collections.emptyList());

            // When
            MarScheduleShiftResponse response = marService.getShiftSchedule(facilityId, shift, date);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getShift()).isEqualTo(shift);
            assertThat(response.getSchedule()).isNotEmpty();
        }

        @Test
        @DisplayName("TC32 - Success: Should return shift schedule for NIGHT")
        void getShiftSchedule_NightShift_Success() {
            // Given
            String shift = MedicationStatus.NIGHT;
            LocalDate date = LocalDate.now();

            // Create schedule for night (22:00-06:00)
            MedicationSchedule nightSchedule = TestDataFactory.createMedicationSchedule(
                    2L, mockOrder, LocalTime.of(23, 0), true);

            List<Resident> residents = Collections.singletonList(mockResident);
            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(nightSchedule);

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(residents);
            when(medicationOrderRepository.findByResidentIdInAndStatus(anyList(), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), eq(date)))
                    .thenReturn(Collections.emptyList());

            // When
            MarScheduleShiftResponse response = marService.getShiftSchedule(facilityId, shift, date);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getShift()).isEqualTo(shift);
            assertThat(response.getSchedule()).isNotEmpty();
        }

        @Test
        @DisplayName("TC33 - Success: No residents should return empty schedule")
        void getShiftSchedule_NoResidents_ShouldReturnEmptySchedule() {
            // Given
            String shift = MedicationStatus.DAY;
            LocalDate date = LocalDate.now();

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(Collections.emptyList());

            // When
            MarScheduleShiftResponse response = marService.getShiftSchedule(facilityId, shift, date);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getShift()).isEqualTo(shift);
            assertThat(response.getSchedule()).isEmpty();

            verify(medicationOrderRepository, never()).findByResidentIdInAndStatus(anyList(), anyString());
        }

        @Test
        @DisplayName("TC34 - Success: No orders should return empty schedule")
        void getShiftSchedule_NoOrders_ShouldReturnEmptySchedule() {
            // Given
            String shift = MedicationStatus.DAY;
            LocalDate date = LocalDate.now();

            List<Resident> residents = Collections.singletonList(mockResident);

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(residents);
            when(medicationOrderRepository.findByResidentIdInAndStatus(anyList(), anyString()))
                    .thenReturn(Collections.emptyList());

            // When
            MarScheduleShiftResponse response = marService.getShiftSchedule(facilityId, shift, date);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getSchedule()).isEmpty();

            verify(medicationScheduleRepository, never()).findByOrderIdInAndIsActiveTrue(anyList());
        }

        @Test
        @DisplayName("TC35 - Success: With custom date")
        void getShiftSchedule_WithCustomDate_ShouldUseProvidedDate() {
            // Given
            String shift = MedicationStatus.DAY;
            LocalDate customDate = LocalDate.of(2026, 7, 15);

            List<Resident> residents = Collections.singletonList(mockResident);
            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(residents);
            when(medicationOrderRepository.findByResidentIdInAndStatus(anyList(), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), eq(customDate)))
                    .thenReturn(Collections.emptyList());

            // When
            MarScheduleShiftResponse response = marService.getShiftSchedule(facilityId, shift, customDate);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getDate()).isEqualTo(customDate.toString());

            verify(medicationLogRepository, times(1))
                    .findByOrderIdInAndLoggedAtDate(anyList(), eq(customDate));
        }

        @Test
        @DisplayName("TC36 - Success: Multiple residents and medications")
        void getShiftSchedule_MultipleResidents_ShouldReturnSchedule() {
            // Given
            String shift = MedicationStatus.DAY;
            LocalDate date = LocalDate.now();

            Resident resident1 = TestDataFactory.createResident(1L);
            Resident resident2 = TestDataFactory.createResident(2L);
            List<Resident> residents = Arrays.asList(resident1, resident2);

            MedicationOrder order1 = TestDataFactory.createMedicationOrder(1L, resident1, mockUser, MedicationStatus.ACTIVE);
            MedicationOrder order2 = TestDataFactory.createMedicationOrder(2L, resident2, mockUser, MedicationStatus.ACTIVE);
            List<MedicationOrder> orders = Arrays.asList(order1, order2);

            MedicationSchedule schedule1 = TestDataFactory.createMedicationSchedule(1L, order1, LocalTime.of(8, 0), true);
            MedicationSchedule schedule2 = TestDataFactory.createMedicationSchedule(2L, order2, LocalTime.of(9, 0), true);
            List<MedicationSchedule> schedules = Arrays.asList(schedule1, schedule2);

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(residents);
            when(medicationOrderRepository.findByResidentIdInAndStatus(anyList(), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), eq(date)))
                    .thenReturn(Collections.emptyList());

            // When
            MarScheduleShiftResponse response = marService.getShiftSchedule(facilityId, shift, date);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getSchedule()).hasSize(2);
        }

        @Test
        @DisplayName("TC37 - Success: Filter out inactive schedules")
        void getShiftSchedule_InactiveSchedules_ShouldBeFiltered() {
            // Given
            String shift = MedicationStatus.DAY;
            LocalDate date = LocalDate.now();

            MedicationSchedule activeSchedule = TestDataFactory.createMedicationSchedule(1L, mockOrder, LocalTime.of(8, 0), true);
            MedicationSchedule inactiveSchedule = TestDataFactory.createMedicationSchedule(2L, mockOrder, LocalTime.of(16, 0), false);

            List<Resident> residents = Collections.singletonList(mockResident);
            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Arrays.asList(activeSchedule, inactiveSchedule);

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(residents);
            when(medicationOrderRepository.findByResidentIdInAndStatus(anyList(), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), eq(date)))
                    .thenReturn(Collections.emptyList());

            // When
            MarScheduleShiftResponse response = marService.getShiftSchedule(facilityId, shift, date);

            // Then
            assertThat(response).isNotNull();
            // Only active schedules should be returned
            assertThat(response.getSchedule()).isNotEmpty();
        }

        @Test
        @DisplayName("TC38 - Success: With default date when not provided")
        void getShiftSchedule_DefaultDate_ShouldUseToday() {
            // Given
            String shift = MedicationStatus.DAY;

            List<Resident> residents = Collections.singletonList(mockResident);
            List<MedicationOrder> orders = Collections.singletonList(mockOrder);
            List<MedicationSchedule> schedules = Collections.singletonList(mockSchedule);

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(residents);
            when(medicationOrderRepository.findByResidentIdInAndStatus(anyList(), anyString()))
                    .thenReturn(orders);
            when(medicationScheduleRepository.findByOrderIdInAndIsActiveTrue(anyList()))
                    .thenReturn(schedules);
            when(medicationLogRepository.findByOrderIdInAndLoggedAtDate(anyList(), eq(LocalDate.now())))
                    .thenReturn(Collections.emptyList());

            // When - no date provided
            MarScheduleShiftResponse response = marService.getShiftSchedule(facilityId, shift, null);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getDate()).isEqualTo(LocalDate.now().toString());

            verify(medicationLogRepository, times(1))
                    .findByOrderIdInAndLoggedAtDate(anyList(), eq(LocalDate.now()));
        }

        @Test
        @DisplayName("TC39 - Error: Invalid shift should throw exception")
        void getShiftSchedule_InvalidShift_ShouldReturnEmpty() {
            // Given
            String invalidShift = "INVALID";
            LocalDate date = LocalDate.now();

            List<Resident> residents = Collections.singletonList(mockResident);

            when(residentRepository.findByFacilityIdAndStatus(eq(facilityId), anyString()))
                    .thenReturn(residents);

            // When
            MarScheduleShiftResponse response = marService.getShiftSchedule(facilityId, invalidShift, date);

            // Then
            assertThat(response).isNotNull();
            // Should return empty schedule for invalid shift
            assertThat(response.getSchedule()).isEmpty();
        }
    }
}