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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class MarService {

    private final ResidentRepository residentRepository;
    private final MedicationOrderRepository medicationOrderRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final MedicationLogRepository medicationLogRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int TIME_WINDOW_MINUTES = 30;

    public MarDashboardResponse getDashboard(Long facilityId, MarDashboardRequest request) {
        log.info("Getting dashboard for facility: {}, shift: {}, date: {}",
                facilityId, request.getShift(), request.getDate());

        LocalDate targetDate = request.getDate() != null ? request.getDate() : LocalDate.now();
        String shift = request.getShift() != null ? request.getShift() : getCurrentShift();

        List<Resident> residents = residentRepository.findByFacilityIdAndStatus(facilityId, MedicationStatus.ACTIVE);
        if (residents.isEmpty()) {
            log.warn("No active residents found for facility: {}", facilityId);
            return buildEmptyDashboard(shift, targetDate);
        }

        List<Long> residentIds = residents.stream().map(Resident::getId).collect(Collectors.toList());
        List<MedicationOrder> activeOrders = medicationOrderRepository
                .findByResidentIdInAndStatus(residentIds, MedicationStatus.ACTIVE);

        if (activeOrders.isEmpty()) {
            log.warn("No active orders found for facility: {}", facilityId);
            return buildEmptyDashboard(shift, targetDate);
        }

        List<Long> orderIds = activeOrders.stream().map(MedicationOrder::getId).collect(Collectors.toList());
        List<MedicationSchedule> todaySchedules = medicationScheduleRepository
                .findByOrderIdInAndIsActiveTrue(orderIds);

        List<MedicationLog> todayLogs = medicationLogRepository
                .findByOrderIdInAndLoggedAtDate(orderIds, targetDate);

        MarDashboardResponse.DashboardSummary summary = buildSummary(todaySchedules, todayLogs);
        List<MarDashboardResponse.AllergyAlert> allergyAlerts = buildAllergyAlerts(residents);
        List<MarDashboardResponse.MedPassItem> medPassList = buildMedPassList(
                residents, activeOrders, todaySchedules, todayLogs);
        String shiftHandoffNotes = getShiftHandoffNotes(facilityId, targetDate, shift);

        return MarDashboardResponse.builder()
                .shift(shift)
                .date(targetDate.format(DATE_FORMATTER))
                .summary(summary)
                .globalAllergyAlerts(allergyAlerts)
                .medPassList(medPassList)
                .shiftHandoffNotes(shiftHandoffNotes)
                .build();
    }

    private MarDashboardResponse buildEmptyDashboard(String shift, LocalDate date) {
        return MarDashboardResponse.builder()
                .shift(shift)
                .date(date.format(DATE_FORMATTER))
                .summary(MarDashboardResponse.DashboardSummary.builder()
                        .pending(0).completed(0).overdue(0).held(0).notAvailable(0).build())
                .globalAllergyAlerts(Collections.emptyList())
                .medPassList(Collections.emptyList())
                .shiftHandoffNotes("")
                .build();
    }

    private String getCurrentShift() {
        LocalTime now = LocalTime.now();
        if (now.isAfter(LocalTime.of(6, 0)) && now.isBefore(LocalTime.of(14, 0))) {
            return MedicationStatus.DAY;
        } else if (now.isAfter(LocalTime.of(14, 0)) && now.isBefore(LocalTime.of(22, 0))) {
            return MedicationStatus.EVENING;
        } else {
            return MedicationStatus.NIGHT;
        }
    }

    private MarDashboardResponse.DashboardSummary buildSummary(
            List<MedicationSchedule> schedules, List<MedicationLog> logs) {

        int totalScheduled = schedules.size();
        int completed = (int) logs.stream()
                .filter(log -> MedicationStatus.ADMINISTERED.equals(log.getStatus()))
                .count();
        int held = (int) logs.stream()
                .filter(log -> MedicationStatus.HELD.equals(log.getStatus()))
                .count();
        int refused = (int) logs.stream()
                .filter(log -> MedicationStatus.REFUSED.equals(log.getStatus()))
                .count();
        int notAvailable = (int) logs.stream()
                .filter(log -> MedicationStatus.NOT_AVAILABLE.equals(log.getStatus()))
                .count();

        LocalTime now = LocalTime.now();
        int overdue = 0;
        Set<Long> loggedOrderIds = logs.stream()
                .map(log -> log.getOrder().getId())
                .collect(Collectors.toSet());

        for (MedicationSchedule schedule : schedules) {
            if (loggedOrderIds.contains(schedule.getOrder().getId())) continue;
            LocalTime scheduledTime = schedule.getScheduledTime();
            LocalTime windowEnd = scheduledTime.plusMinutes(TIME_WINDOW_MINUTES);
            if (now.isAfter(windowEnd)) {
                overdue++;
            }
        }

        int pending = totalScheduled - completed - held - refused - notAvailable - overdue;

        return MarDashboardResponse.DashboardSummary.builder()
                .pending(Math.max(pending, 0))
                .completed(completed)
                .overdue(overdue)
                .held(held)
                .notAvailable(notAvailable)
                .build();
    }

    private List<MarDashboardResponse.AllergyAlert> buildAllergyAlerts(List<Resident> residents) {
        return Collections.emptyList();
    }

    private List<MarDashboardResponse.MedPassItem> buildMedPassList(
            List<Resident> residents,
            List<MedicationOrder> orders,
            List<MedicationSchedule> schedules,
            List<MedicationLog> logs) {

        Map<Long, MedicationLog> logMap = logs.stream()
                .collect(Collectors.toMap(
                        log -> log.getOrder().getId(),
                        log -> log,
                        (l1, l2) -> l1
                ));

        Map<Long, List<MedicationSchedule>> scheduleMap = schedules.stream()
                .collect(Collectors.groupingBy(s -> s.getOrder().getId()));

        List<MarDashboardResponse.MedPassItem> items = new ArrayList<>();

        for (Resident resident : residents) {
            List<MedicationOrder> residentOrders = orders.stream()
                    .filter(o -> o.getResident().getId().equals(resident.getId()))
                    .collect(Collectors.toList());

            if (residentOrders.isEmpty()) continue;

            MarDashboardResponse.NextMedication nextMed = null;
            String status = MedicationStatus.COMPLETED;

            for (MedicationOrder order : residentOrders) {
                List<MedicationSchedule> orderSchedules = scheduleMap.getOrDefault(order.getId(), Collections.emptyList());
                if (orderSchedules.isEmpty()) continue;

                MedicationLog log = logMap.get(order.getId());

                for (MedicationSchedule schedule : orderSchedules) {
                    if (!schedule.getIsActive()) continue;

                    LocalTime scheduledTime = schedule.getScheduledTime();
                    LocalTime now = LocalTime.now();
                    LocalTime windowStart = scheduledTime.minusMinutes(TIME_WINDOW_MINUTES);
                    LocalTime windowEnd = scheduledTime.plusMinutes(TIME_WINDOW_MINUTES);

                    boolean isAdministered = log != null &&
                            MedicationStatus.ADMINISTERED.equals(log.getStatus()) &&
                            log.getLoggedAt().toLocalDate().equals(LocalDate.now());

                    if (!isAdministered) {
                        if (now.isAfter(windowEnd)) {
                            status = MedicationStatus.OVERDUE;
                        } else if (now.isAfter(windowStart)) {
                            status = MedicationStatus.DUE_SOON;
                        } else {
                            status = MedicationStatus.SCHEDULED;
                        }

                        nextMed = MarDashboardResponse.NextMedication.builder()
                                .orderId(order.getId())
                                .drugName(order.getDrugName())
                                .scheduledTime(scheduledTime.format(TIME_FORMATTER))
                                .timeWindowStart(windowStart.format(TIME_FORMATTER))
                                .timeWindowEnd(windowEnd.format(TIME_FORMATTER))
                                .build();
                        break;
                    }
                }
                if (nextMed != null) break;
            }

            if (nextMed == null) continue;

            String roomNumber = "N/A";
            String bedNumber = "N/A";
            Bed bed = resident.getBed();
            if (bed != null) {
                bedNumber = bed.getBedNumber();
                Room room = bed.getRoom();
                if (room != null) {
                    roomNumber = room.getRoomNumber();
                }
            }

            items.add(MarDashboardResponse.MedPassItem.builder()
                    .residentId(resident.getId())
                    .residentName(resident.getFirstName() + " " + resident.getLastName())
                    .roomNumber(roomNumber)
                    .bedNumber(bedNumber)
                    .status(status)
                    .nextMedication(nextMed)
                    .hasUnconfirmedAllergy(false)
                    .build());
        }

        return items;
    }

    private String getShiftHandoffNotes(Long facilityId, LocalDate date, String shift) {
        return "";
    }

    public MarResidentResponse getResidentMar(Long facilityId, Long residentId,
                                              String dateRange, String statusFilter,
                                              LocalDate startDate, LocalDate endDate) {
        log.info("Getting MAR for resident: {}, facility: {}", residentId, facilityId);

        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new AppException(ErrorCode.MAR_RESIDENT_NOT_FOUND));

        if (startDate == null && endDate == null) {
            startDate = LocalDate.now().minusDays(7);
            endDate = LocalDate.now();
        } else if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new AppException(ErrorCode.MAR_INVALID_DATE_RANGE);
        }

        List<MedicationOrder> orders = medicationOrderRepository
                .findByResidentIdAndStatus(residentId, MedicationStatus.ACTIVE);

        if (orders.isEmpty()) {
            throw new AppException(ErrorCode.MAR_NO_ACTIVE_ORDERS);
        }

        List<Long> orderIds = orders.stream().map(MedicationOrder::getId).collect(Collectors.toList());
        List<MedicationSchedule> schedules = medicationScheduleRepository
                .findByOrderIdInAndIsActiveTrue(orderIds);

        if (schedules.isEmpty()) {
            throw new AppException(ErrorCode.MAR_NO_SCHEDULES);
        }

        OffsetDateTime startDateTime = startDate.atStartOfDay().atOffset(java.time.ZoneOffset.UTC);
        OffsetDateTime endDateTime = endDate.atTime(23, 59, 59).atOffset(java.time.ZoneOffset.UTC);

        List<MedicationLog> logs = medicationLogRepository
                .findByOrderIdInAndLoggedAtBetween(orderIds, startDateTime, endDateTime);

        Map<Long, MedicationLog> logMap = logs.stream()
                .collect(Collectors.toMap(
                        log -> log.getOrder().getId(),
                        log -> log,
                        (l1, l2) -> l1
                ));

        Map<Long, List<MedicationSchedule>> scheduleMap = schedules.stream()
                .collect(Collectors.groupingBy(s -> s.getOrder().getId()));

        List<MarResidentResponse.MedicationGrid> grid = new ArrayList<>();

        int totalScheduled = 0;
        int administered = 0;
        int held = 0;
        int refused = 0;
        int notAvailable = 0;
        int overrides = 0;

        for (MedicationOrder order : orders) {
            List<MedicationSchedule> orderSchedules = scheduleMap.getOrDefault(order.getId(), Collections.emptyList());

            List<MarResidentResponse.DayDetail> days = new ArrayList<>();
            for (MedicationSchedule schedule : orderSchedules) {
                if (!schedule.getIsActive()) continue;

                LocalDate currentDate = startDate;
                while (!currentDate.isAfter(endDate)) {
                    final LocalDate finalDate = currentDate;

                    MedicationLog log = logMap.values().stream()
                            .filter(l -> l.getOrder().getId().equals(order.getId()) &&
                                    l.getLoggedAt().toLocalDate().equals(finalDate))
                            .findFirst()
                            .orElse(null);

                    String status = MedicationStatus.SCHEDULED;
                    String overrideReason = null;
                    MarResidentResponse.AdminBy adminBy = null;
                    MarResidentResponse.AdminBy witnessBy = null;
                    String loggedAt = null;

                    if (log != null) {
                        status = log.getStatus();
                        overrideReason = log.getOverrideReason();
                        if (log.getAdministeredBy() != null) {
                            adminBy = MarResidentResponse.AdminBy.builder()
                                    .id(log.getAdministeredBy().getId())
                                    .displayName(getUserDisplayName(log.getAdministeredBy()))
                                    .build();
                        }
                        if (log.getWitnessedBy() != null) {
                            witnessBy = MarResidentResponse.AdminBy.builder()
                                    .id(log.getWitnessedBy().getId())
                                    .displayName(getUserDisplayName(log.getWitnessedBy()))
                                    .build();
                        }
                        loggedAt = log.getLoggedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

                        switch (status) {
                            case MedicationStatus.ADMINISTERED:
                                administered++;
                                if (overrideReason != null && !overrideReason.isEmpty()) overrides++;
                                break;
                            case MedicationStatus.HELD:
                                held++;
                                break;
                            case MedicationStatus.REFUSED:
                                refused++;
                                break;
                            case MedicationStatus.NOT_AVAILABLE:
                                notAvailable++;
                                break;
                        }
                        totalScheduled++;
                    } else {
                        if (!currentDate.isBefore(LocalDate.now())) {
                            totalScheduled++;
                        }
                    }

                    if (statusFilter != null && !statusFilter.equals("ALL")) {
                        if (!statusFilter.equals(status) &&
                                !(statusFilter.equals("OVERRIDE") && overrideReason != null && !overrideReason.isEmpty())) {
                            currentDate = currentDate.plusDays(1);
                            continue;
                        }
                    }

                    days.add(MarResidentResponse.DayDetail.builder()
                            .date(currentDate.format(DATE_FORMATTER))
                            .scheduleId(schedule.getId())
                            .scheduledTime(schedule.getScheduledTime().format(TIME_FORMATTER))
                            .status(status)
                            .administeredBy(adminBy)
                            .witnessedBy(witnessBy)
                            .loggedAt(loggedAt)
                            .overrideReason(overrideReason)
                            .build());

                    currentDate = currentDate.plusDays(1);
                }
            }

            grid.add(MarResidentResponse.MedicationGrid.builder()
                    .orderId(order.getId())
                    .drugName(order.getDrugName())
                    .dosage(order.getDosage())
                    .route(order.getRoute())
                    .frequency(order.getFrequency())
                    .days(days)
                    .build());
        }

        String roomNumber = "N/A";
        String bedNumber = "N/A";
        Bed bed = resident.getBed();
        if (bed != null) {
            bedNumber = bed.getBedNumber();
            Room room = bed.getRoom();
            if (room != null) {
                roomNumber = room.getRoomNumber();
            }
        }

        return MarResidentResponse.builder()
                .resident(MarResidentResponse.ResidentInfo.builder()
                        .id(resident.getId())
                        .fullName(resident.getFirstName() + " " + resident.getLastName())
                        .roomNumber(roomNumber)
                        .bedNumber(bedNumber)
                        .build())
                .dateRange(MarResidentResponse.DateRange.builder()
                        .start(startDate.format(DATE_FORMATTER))
                        .end(endDate.format(DATE_FORMATTER))
                        .build())
                .medicationGrid(grid)
                .summaryStats(MarResidentResponse.SummaryStats.builder()
                        .totalScheduled(totalScheduled)
                        .administered(administered)
                        .held(held)
                        .refused(refused)
                        .notAvailable(notAvailable)
                        .overrides(overrides)
                        .build())
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

    public byte[] printMar(Long facilityId, Long residentId, LocalDate startDate, LocalDate endDate) {
        log.info("Printing MAR for resident: {}, facility: {}", residentId, facilityId);

        try {
            MarResidentResponse marData = getResidentMar(facilityId, residentId, null, null, startDate, endDate);
            return generatePdf(marData);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error generating PDF for resident: {}", residentId, e);
            throw new AppException(ErrorCode.MAR_PDF_GENERATION_FAILED);
        }
    }

    private byte[] generatePdf(MarResidentResponse marData) {
        log.warn("PDF generation not yet implemented");
        return new byte[0];
    }

    public MarScheduleShiftResponse getShiftSchedule(Long facilityId, String shift, LocalDate date) {
        log.info("Getting shift schedule for facility: {}, shift: {}, date: {}", facilityId, shift, date);

        List<Resident> residents = residentRepository.findByFacilityIdAndStatus(facilityId, MedicationStatus.ACTIVE);
        if (residents.isEmpty()) {
            return MarScheduleShiftResponse.builder()
                    .shift(shift)
                    .date(date.format(DATE_FORMATTER))
                    .schedule(Collections.emptyList())
                    .build();
        }

        List<Long> residentIds = residents.stream().map(Resident::getId).collect(Collectors.toList());

        List<MedicationOrder> orders = medicationOrderRepository
                .findByResidentIdInAndStatus(residentIds, MedicationStatus.ACTIVE);
        if (orders.isEmpty()) {
            return MarScheduleShiftResponse.builder()
                    .shift(shift)
                    .date(date.format(DATE_FORMATTER))
                    .schedule(Collections.emptyList())
                    .build();
        }

        List<Long> orderIds = orders.stream().map(MedicationOrder::getId).collect(Collectors.toList());

        List<MedicationSchedule> schedules = medicationScheduleRepository
                .findByOrderIdInAndIsActiveTrue(orderIds);

        Map<Long, List<MedicationOrder>> ordersByResident = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getResident().getId()));

        Map<Long, List<MedicationSchedule>> schedulesByOrder = schedules.stream()
                .collect(Collectors.groupingBy(s -> s.getOrder().getId()));

        List<MedicationLog> todayLogs = medicationLogRepository
                .findByOrderIdInAndLoggedAtDate(orderIds, date);
        Map<Long, MedicationLog> logMap = todayLogs.stream()
                .collect(Collectors.toMap(
                        log -> log.getOrder().getId(),
                        log -> log,
                        (l1, l2) -> l1
                ));

        List<MarScheduleShiftResponse.ShiftScheduleItem> items = new ArrayList<>();

        for (Resident resident : residents) {
            List<MedicationOrder> residentOrders = ordersByResident.getOrDefault(resident.getId(), Collections.emptyList());
            if (residentOrders.isEmpty()) continue;

            String roomNumber = "N/A";
            Bed bed = resident.getBed();
            if (bed != null) {
                Room room = bed.getRoom();
                if (room != null) {
                    roomNumber = room.getRoomNumber();
                }
            }

            List<MarScheduleShiftResponse.ShiftMedication> medications = new ArrayList<>();

            for (MedicationOrder order : residentOrders) {
                List<MedicationSchedule> orderSchedules = schedulesByOrder.getOrDefault(order.getId(), Collections.emptyList());

                for (MedicationSchedule schedule : orderSchedules) {
                    if (!schedule.getIsActive()) continue;
                    if (!isInShift(schedule.getScheduledTime(), shift)) continue;

                    MedicationLog log = logMap.get(order.getId());
                    String status = log != null ? log.getStatus() : MedicationStatus.SCHEDULED;

                    medications.add(MarScheduleShiftResponse.ShiftMedication.builder()
                            .orderId(order.getId())
                            .scheduleId(schedule.getId())
                            .drugName(order.getDrugName())
                            .dosage(order.getDosage())
                            .scheduledTime(schedule.getScheduledTime().format(TIME_FORMATTER))
                            .isActive(schedule.getIsActive())
                            .status(status)
                            .build());
                }
            }

            if (!medications.isEmpty()) {
                items.add(MarScheduleShiftResponse.ShiftScheduleItem.builder()
                        .residentId(resident.getId())
                        .residentName(resident.getFirstName() + " " + resident.getLastName())
                        .roomNumber(roomNumber)
                        .medications(medications)
                        .build());
            }
        }

        return MarScheduleShiftResponse.builder()
                .shift(shift)
                .date(date.format(DATE_FORMATTER))
                .schedule(items)
                .build();
    }

    private boolean isInShift(LocalTime time, String shift) {
        switch (shift.toUpperCase()) {
            case MedicationStatus.DAY:
                return time.isAfter(LocalTime.of(6, 0)) && time.isBefore(LocalTime.of(14, 0));
            case MedicationStatus.EVENING:
                return time.isAfter(LocalTime.of(14, 0)) && time.isBefore(LocalTime.of(22, 0));
            case MedicationStatus.NIGHT:
                return time.isAfter(LocalTime.of(22, 0)) || time.isBefore(LocalTime.of(6, 0));
            default:
                return true;
        }
    }
}