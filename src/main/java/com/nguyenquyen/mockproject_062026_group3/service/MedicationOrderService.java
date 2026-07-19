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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MedicationOrderService {

    private final ResidentRepository residentRepository;
    private final MedicationOrderRepository medicationOrderRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final MedicationLogRepository medicationLogRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final int TIME_WINDOW_MINUTES = 30;

    public MedicationOrderListResponse getMedicationOrders(Long facilityId, GetMedicationOrdersRequest request) {
        log.info("Getting medication orders for facility: {}, residentId: {}, status: {}, search: {}",
                facilityId, request.getResidentId(), request.getStatus(), request.getSearch());

        int page = request.getPage() != null ? request.getPage() : 1;
        int limit = request.getLimit() != null ? request.getLimit() : 20;
        Pageable pageable = PageRequest.of(page - 1, limit);

        Page<MedicationOrder> orderPage;

        if (request.getResidentId() != null) {
            orderPage = medicationOrderRepository.findByResidentIdAndStatusWithPagination(
                    request.getResidentId(),
                    request.getStatus() != null ? request.getStatus() : MedicationStatus.ACTIVE,
                    pageable);
        } else {
            List<Resident> residents = residentRepository.findByFacilityIdAndStatus(facilityId, MedicationStatus.ACTIVE);
            List<Long> residentIds = residents.stream().map(Resident::getId).collect(Collectors.toList());

            if (residentIds.isEmpty()) {
                return MedicationOrderListResponse.builder()
                        .medicationOrders(Collections.emptyList())
                        .metadata(MedicationOrderListResponse.PaginationMetadata.builder()
                                .currentPage(page)
                                .totalPage(0)
                                .currentLimit(limit)
                                .hasNext(false)
                                .hasPrevious(false)
                                .build())
                        .build();
            }

            orderPage = medicationOrderRepository.findByResidentIdInAndStatusWithPagination(
                    residentIds,
                    request.getStatus() != null ? request.getStatus() : MedicationStatus.ACTIVE,
                    pageable);
        }

        List<MedicationOrderResponse> orderResponses = orderPage.getContent().stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());

        if (request.getSearch() != null && !request.getSearch().isEmpty()) {
            String searchLower = request.getSearch().toLowerCase();
            orderResponses = orderResponses.stream()
                    .filter(o -> o.getDrugName().toLowerCase().contains(searchLower))
                    .collect(Collectors.toList());
        }

        int totalPages = orderPage.getTotalPages();

        return MedicationOrderListResponse.builder()
                .medicationOrders(orderResponses)
                .metadata(MedicationOrderListResponse.PaginationMetadata.builder()
                        .currentPage(page)
                        .totalPage(totalPages)
                        .currentLimit(limit)
                        .hasNext(page < totalPages)
                        .hasPrevious(page > 1)
                        .build())
                .build();
    }

    private MedicationOrderResponse convertToOrderResponse(MedicationOrder order) {
        Resident resident = order.getResident();
        User prescriber = order.getPrescribedBy();

        String roomNumber = "N/A";
        String bedNumber = "N/A";
        if (resident != null) {
            Bed bed = resident.getBed();
            if (bed != null) {
                bedNumber = bed.getBedNumber();
                Room room = bed.getRoom();
                if (room != null) {
                    roomNumber = room.getRoomNumber();
                }
            }
        }

        String lastAdministeredAt = null;
        String lastAdministeredBy = null;

        List<MedicationLog> logs = medicationLogRepository
                .findByOrderIdInAndStatus(Collections.singletonList(order.getId()), MedicationStatus.ADMINISTERED);
        if (!logs.isEmpty()) {
            MedicationLog lastLog = logs.stream()
                    .max(Comparator.comparing(MedicationLog::getLoggedAt))
                    .orElse(null);
            if (lastLog != null) {
                lastAdministeredAt = lastLog.getLoggedAt().format(DATE_TIME_FORMATTER);
                if (lastLog.getAdministeredBy() != null) {
                    lastAdministeredBy = getUserDisplayName(lastLog.getAdministeredBy());
                }
            }
        }

        boolean allergyConflict = false;

        return MedicationOrderResponse.builder()
                .id(order.getId())
                .resident(MedicationOrderResponse.ResidentInfo.builder()
                        .id(resident != null ? resident.getId() : null)
                        .displayName(resident != null ? resident.getFirstName() + " " + resident.getLastName() : "N/A")
                        .roomNumber(roomNumber)
                        .bedNumber(bedNumber)
                        .build())
                .drugName(order.getDrugName())
                .dosage(order.getDosage())
                .route(order.getRoute())
                .frequency(order.getFrequency())
                .isControlledSubstance(order.getIsControlledSubstance())
                .status(order.getStatus())
                .prescribedBy(MedicationOrderResponse.PrescriberInfo.builder()
                        .id(prescriber != null ? prescriber.getId() : null)
                        .displayName(prescriber != null ? getUserDisplayName(prescriber) : "N/A")
                        .licenseNumber(prescriber != null ? prescriber.getLicenseNumber() : null)
                        .build())
                .lastAdministeredAt(lastAdministeredAt)
                .lastAdministeredBy(lastAdministeredBy)
                .allergyConflict(allergyConflict)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public MedicationOrderDetailResponse getMedicationOrderDetail(Long facilityId, Long orderId) {
        log.info("Getting medication order detail for order: {}, facility: {}", orderId, facilityId);

        MedicationOrder order = medicationOrderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.MAR_ORDER_NOT_FOUND));

        Resident resident = order.getResident();
        User prescriber = order.getPrescribedBy();

        List<MedicationSchedule> schedules = medicationScheduleRepository
                .findByOrderIdAndIsActive(orderId);

        LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
        OffsetDateTime sevenDaysAgoDateTime = sevenDaysAgo.atStartOfDay().atOffset(java.time.ZoneOffset.UTC);
        List<MedicationLog> logs = medicationLogRepository
                .findByOrderIdAndLoggedAtAfter(orderId, sevenDaysAgoDateTime);

        String roomNumber = "N/A";
        String bedNumber = "N/A";
        if (resident != null) {
            Bed bed = resident.getBed();
            if (bed != null) {
                bedNumber = bed.getBedNumber();
                Room room = bed.getRoom();
                if (room != null) {
                    roomNumber = room.getRoomNumber();
                }
            }
        }

        boolean allergyConflict = false;

        List<MedicationOrderDetailResponse.ScheduleInfo> scheduleInfos = schedules.stream()
                .map(s -> MedicationOrderDetailResponse.ScheduleInfo.builder()
                        .id(s.getId())
                        .scheduledTime(s.getScheduledTime().format(TIME_FORMATTER))
                        .isActive(s.getIsActive())
                        .build())
                .collect(Collectors.toList());

        List<MedicationOrderDetailResponse.RecentLog> recentLogs = logs.stream()
                .map(l -> {
                    Long scheduleId = null;
                    for (MedicationSchedule s : schedules) {
                        if (l.getLoggedAt().toLocalTime().equals(s.getScheduledTime())) {
                            scheduleId = s.getId();
                            break;
                        }
                    }

                    return MedicationOrderDetailResponse.RecentLog.builder()
                            .logId(l.getId())
                            .scheduleId(scheduleId)
                            .status(l.getStatus())
                            .administeredBy(l.getAdministeredBy() != null ?
                                    MedicationOrderDetailResponse.RecentLog.AdminBy.builder()
                                            .id(l.getAdministeredBy().getId())
                                            .displayName(getUserDisplayName(l.getAdministeredBy()))
                                            .build() : null)
                            .loggedAt(l.getLoggedAt().format(DATE_TIME_FORMATTER))
                            .build();
                })
                .collect(Collectors.toList());

        return MedicationOrderDetailResponse.builder()
                .id(order.getId())
                .resident(MedicationOrderDetailResponse.ResidentInfo.builder()
                        .id(resident != null ? resident.getId() : null)
                        .displayName(resident != null ? resident.getFirstName() + " " + resident.getLastName() : "N/A")
                        .roomNumber(roomNumber)
                        .bedNumber(bedNumber)
                        .build())
                .drugName(order.getDrugName())
                .dosage(order.getDosage())
                .route(order.getRoute())
                .frequency(order.getFrequency())
                .isControlledSubstance(order.getIsControlledSubstance())
                .status(order.getStatus())
                .prescribedBy(MedicationOrderDetailResponse.PrescriberInfo.builder()
                        .id(prescriber != null ? prescriber.getId() : null)
                        .displayName(prescriber != null ? getUserDisplayName(prescriber) : "N/A")
                        .licenseNumber(prescriber != null ? prescriber.getLicenseNumber() : null)
                        .build())
                .prescriberNotes("Take with food if patient complains of GI upset")
                .allergyConflict(allergyConflict)
                .schedules(scheduleInfos)
                .recentLogs(recentLogs)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public CreateMedicationOrderResponse createMedicationOrder(Long facilityId,
                                                               CreateMedicationOrderRequest request) {
        log.info("Creating medication order for resident: {}, drug: {}",
                request.getResidentId(), request.getDrugName());

        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new AppException(ErrorCode.MAR_RESIDENT_NOT_FOUND));

        User prescriber = userRepository.findById(request.getPrescribedBy())
                .orElseThrow(() -> new AppException(ErrorCode.MAR_PRESCRIBER_NOT_FOUND));

        List<MedicationOrder> existingOrders = medicationOrderRepository
                .findByResidentIdAndDrugNameAndStatus(
                        request.getResidentId(),
                        request.getDrugName(),
                        MedicationStatus.ACTIVE);
        if (!existingOrders.isEmpty()) {
            throw new AppException(ErrorCode.MAR_ORDER_DUPLICATE);
        }

        if (request.getScheduledTimes() == null || request.getScheduledTimes().isEmpty()) {
            throw new AppException(ErrorCode.MAR_INVALID_SCHEDULE_TIME);
        }

        MedicationOrder order = MedicationOrder.builder()
                .drugName(request.getDrugName())
                .dosage(request.getDosage())
                .route(request.getRoute())
                .frequency(request.getFrequency())
                .isControlledSubstance(request.getIsControlledSubstance() != null ?
                        request.getIsControlledSubstance() : false)
                .status(MedicationStatus.ACTIVE)
                .resident(resident)
                .prescribedBy(prescriber)
                .isDeleted(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        MedicationOrder savedOrder = medicationOrderRepository.save(order);

        List<CreateMedicationOrderResponse.ScheduleInfo> scheduleInfos = new ArrayList<>();
        for (String timeStr : request.getScheduledTimes()) {
            LocalTime scheduledTime = LocalTime.parse(timeStr, TIME_FORMATTER);

            MedicationSchedule schedule = MedicationSchedule.builder()
                    .order(savedOrder)
                    .scheduledTime(scheduledTime)
                    .isActive(true)
                    .build();

            MedicationSchedule savedSchedule = medicationScheduleRepository.save(schedule);

            scheduleInfos.add(CreateMedicationOrderResponse.ScheduleInfo.builder()
                    .id(savedSchedule.getId())
                    .scheduledTime(savedSchedule.getScheduledTime().format(TIME_FORMATTER))
                    .isActive(savedSchedule.getIsActive())
                    .build());
        }

        return CreateMedicationOrderResponse.builder()
                .id(savedOrder.getId())
                .residentId(savedOrder.getResident().getId())
                .drugName(savedOrder.getDrugName())
                .dosage(savedOrder.getDosage())
                .route(savedOrder.getRoute())
                .frequency(savedOrder.getFrequency())
                .isControlledSubstance(savedOrder.getIsControlledSubstance())
                .status(savedOrder.getStatus())
                .prescribedBy(savedOrder.getPrescribedBy().getId())
                .schedules(scheduleInfos)
                .createdAt(savedOrder.getCreatedAt())
                .build();
    }

    public DiscontinueMedicationOrderResponse discontinueMedicationOrder(
            Long facilityId, Long orderId, DiscontinueMedicationOrderRequest request) {
        log.info("Discontinuing medication order: {}, facility: {}", orderId, facilityId);

        MedicationOrder order = medicationOrderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.MAR_ORDER_NOT_FOUND));

        if (MedicationStatus.DISCONTINUED.equals(order.getStatus())) {
            throw new AppException(ErrorCode.MAR_ORDER_ALREADY_DISCONTINUED);
        }

        List<MedicationSchedule> activeSchedules = medicationScheduleRepository
                .findByOrderIdAndIsActive(orderId);
        if (!activeSchedules.isEmpty()) {
            List<Long> orderIds = Collections.singletonList(orderId);
            List<MedicationLog> todayLogs = medicationLogRepository
                    .findByOrderIdInAndLoggedAtDate(orderIds, LocalDate.now());

            int administeredCount = (int) todayLogs.stream()
                    .filter(l -> MedicationStatus.ADMINISTERED.equals(l.getStatus()))
                    .count();

            if (administeredCount < activeSchedules.size()) {
                throw new AppException(ErrorCode.MAR_ORDER_HAS_PENDING_DOSES);
            }
        }

        User currentUser = userRepository.findById(1L)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        order.setStatus(MedicationStatus.DISCONTINUED);
        order.setUpdatedAt(OffsetDateTime.now());
        medicationOrderRepository.save(order);

        for (MedicationSchedule schedule : activeSchedules) {
            schedule.setIsActive(false);
            medicationScheduleRepository.save(schedule);
        }

        return DiscontinueMedicationOrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .discontinuedAt(OffsetDateTime.now())
                .discontinuedBy(DiscontinueMedicationOrderResponse.DiscontinuedBy.builder()
                        .id(currentUser.getId())
                        .displayName(getUserDisplayName(currentUser))
                        .build())
                .discontinueReason(request.getDiscontinueReason())
                .build();
    }

    public PendingMedicationResponse getResidentPendingMedications(Long facilityId,
                                                                   Long residentId,
                                                                   String time) {
        log.info("Getting pending medications for resident: {}, facility: {}", residentId, facilityId);

        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new AppException(ErrorCode.MAR_RESIDENT_NOT_FOUND));

        List<MedicationOrder> orders = medicationOrderRepository
                .findByResidentIdAndStatus(residentId, MedicationStatus.ACTIVE);

        if (orders.isEmpty()) {
            throw new AppException(ErrorCode.MAR_RESIDENT_NO_ACTIVE_ORDERS);
        }

        List<Long> orderIds = orders.stream().map(MedicationOrder::getId).collect(Collectors.toList());

        List<MedicationSchedule> schedules = medicationScheduleRepository
                .findByOrderIdInAndIsActiveTrue(orderIds);

        if (schedules.isEmpty()) {
            return PendingMedicationResponse.builder()
                    .residentId(residentId)
                    .pendingMedications(Collections.emptyList())
                    .build();
        }

        List<MedicationLog> todayLogs = medicationLogRepository
                .findByOrderIdInAndLoggedAtDate(orderIds, LocalDate.now());

        Set<Long> loggedOrderIds = todayLogs.stream()
                .map(log -> log.getOrder().getId())
                .collect(Collectors.toSet());

        LocalTime currentTime = time != null ?
                LocalTime.parse(time, TIME_FORMATTER) : LocalTime.now();

        List<PendingMedicationResponse.PendingMedication> pendingMedications = new ArrayList<>();

        for (MedicationOrder order : orders) {
            if (loggedOrderIds.contains(order.getId())) continue;

            List<MedicationSchedule> orderSchedules = schedules.stream()
                    .filter(s -> s.getOrder().getId().equals(order.getId()))
                    .collect(Collectors.toList());

            for (MedicationSchedule schedule : orderSchedules) {
                if (!schedule.getIsActive()) continue;

                LocalTime scheduledTime = schedule.getScheduledTime();
                LocalTime windowStart = scheduledTime.minusMinutes(TIME_WINDOW_MINUTES);
                LocalTime windowEnd = scheduledTime.plusMinutes(TIME_WINDOW_MINUTES);

                if (currentTime.isAfter(scheduledTime.minusHours(2)) &&
                        currentTime.isBefore(scheduledTime.plusHours(2))) {

                    boolean isOverdue = currentTime.isAfter(windowEnd);
                    int minutesUntilDue = (int) windowEnd.minusMinutes(currentTime.toSecondOfDay() / 60).toSecondOfDay() / 60;

                    pendingMedications.add(PendingMedicationResponse.PendingMedication.builder()
                            .orderId(order.getId())
                            .scheduleId(schedule.getId())
                            .drugName(order.getDrugName())
                            .scheduledTime(scheduledTime.format(TIME_FORMATTER))
                            .timeWindowStart(windowStart.format(TIME_FORMATTER))
                            .timeWindowEnd(windowEnd.format(TIME_FORMATTER))
                            .isOverdue(isOverdue)
                            .minutesUntilDue(Math.max(minutesUntilDue, 0))
                            .requiresWitness(order.getIsControlledSubstance())
                            .build());
                }
            }
        }

        pendingMedications.sort(Comparator.comparing(
                PendingMedicationResponse.PendingMedication::getScheduledTime));

        return PendingMedicationResponse.builder()
                .residentId(residentId)
                .pendingMedications(pendingMedications)
                .build();
    }

    private String getUserDisplayName(User user) {
        if (user == null) return null;
        String name = user.getFirstName() + " " + user.getLastName();
        if (user.getRole() != null) {
            name += " (" + user.getRole().getRoleName() + ")";
        }
        return name;
    }
}