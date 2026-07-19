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
public class BatchService {

    private final ResidentRepository residentRepository;
    private final MedicationOrderRepository medicationOrderRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final MedicationLogRepository medicationLogRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final int TIME_WINDOW_MINUTES = 30;

    public BatchAdministerResponse batchAdminister(Long facilityId, BatchAdministerRequest request) {
        log.info("Batch administering medications for resident: {}, count: {}",
                request.getResidentId(), request.getOrderIds().size());

        if (request.getSessionId() == null || request.getSessionId().isEmpty()) {
            throw new AppException(ErrorCode.MAR_INVALID_SESSION);
        }

        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new AppException(ErrorCode.MAR_RESIDENT_NOT_FOUND));

        List<MedicationOrder> orders = medicationOrderRepository
                .findAllById(request.getOrderIds());

        if (orders.size() != request.getOrderIds().size()) {
            throw new AppException(ErrorCode.MAR_BATCH_ORDER_NOT_FOUND);
        }

        List<MedicationSchedule> schedules = medicationScheduleRepository
                .findAllById(request.getScheduleIds());

        if (schedules.size() != request.getScheduleIds().size()) {
            throw new AppException(ErrorCode.MAR_BATCH_SCHEDULE_NOT_FOUND);
        }

        Map<Long, MedicationOrder> orderMap = orders.stream()
                .collect(Collectors.toMap(MedicationOrder::getId, o -> o));

        Map<Long, MedicationSchedule> scheduleMap = schedules.stream()
                .collect(Collectors.toMap(MedicationSchedule::getId, s -> s));

        boolean hasControlledSubstance = orders.stream()
                .anyMatch(MedicationOrder::getIsControlledSubstance);

        if (hasControlledSubstance && request.getWitnessedBy() == null) {
            throw new AppException(ErrorCode.MAR_BATCH_WITNESS_REQUIRED);
        }

        User adminUser = userRepository.findById(1L)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        User witnessUser = null;
        if (request.getWitnessedBy() != null) {
            witnessUser = userRepository.findById(request.getWitnessedBy())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        }

        List<MedicationLog> todayLogs = medicationLogRepository
                .findByOrderIdInAndLoggedAtDate(request.getOrderIds(), LocalDate.now());

        Set<Long> administeredOrderIds = todayLogs.stream()
                .map(log -> log.getOrder().getId())
                .collect(Collectors.toSet());

        List<BatchAdministerResponse.BatchLog> batchLogs = new ArrayList<>();
        int administeredCount = 0;
        int failedCount = 0;

        for (int i = 0; i < request.getOrderIds().size(); i++) {
            Long orderId = request.getOrderIds().get(i);
            Long scheduleId = request.getScheduleIds().get(i);

            MedicationOrder order = orderMap.get(orderId);
            MedicationSchedule schedule = scheduleMap.get(scheduleId);

            BatchAdministerResponse.BatchLog.BatchLogBuilder logBuilder = BatchAdministerResponse.BatchLog.builder()
                    .orderId(orderId)
                    .scheduleId(scheduleId);

            try {
                if (administeredOrderIds.contains(orderId)) {
                    throw new AppException(ErrorCode.MAR_BATCH_ALREADY_ADMINISTERED);
                }

                if (!MedicationStatus.ACTIVE.equals(order.getStatus())) {
                    throw new AppException(ErrorCode.MAR_ORDER_UPDATE_NOT_ALLOWED);
                }

                if (!schedule.getIsActive()) {
                    throw new AppException(ErrorCode.MAR_SCHEDULE_NOT_FOUND);
                }

                LocalTime now = LocalTime.now();
                LocalTime scheduledTime = schedule.getScheduledTime();
                LocalTime windowStart = scheduledTime.minusMinutes(TIME_WINDOW_MINUTES);
                LocalTime windowEnd = scheduledTime.plusMinutes(TIME_WINDOW_MINUTES);

                boolean isInTimeWindow = now.isAfter(windowStart) && now.isBefore(windowEnd);

                MedicationLog log = MedicationLog.builder()
                        .status(MedicationStatus.ADMINISTERED)
                        .isClinicallyJustified(true)
                        .overrideReason(isInTimeWindow ? null : "BATCH_ADMINISTRATION_TIME_WINDOW")
                        .order(order)
                        .administeredBy(adminUser)
                        .witnessedBy(hasControlledSubstance ? witnessUser : null)
                        .loggedAt(OffsetDateTime.now())
                        .build();

                MedicationLog savedLog = medicationLogRepository.save(log);

                logBuilder.status(MedicationStatus.ADMINISTERED)
                        .logId(savedLog.getId())
                        .errorMessage(null);

                administeredCount++;

            } catch (AppException e) {
                log.warn("Failed to administer medication order {}: {}", orderId, e.getMessage());
                logBuilder.status("FAILED")
                        .logId(null)
                        .errorMessage(e.getMessage());
                failedCount++;
            } catch (Exception e) {
                log.error("Unexpected error administering medication order {}: {}", orderId, e.getMessage());
                logBuilder.status("FAILED")
                        .logId(null)
                        .errorMessage("Unexpected error: " + e.getMessage());
                failedCount++;
            }

            batchLogs.add(logBuilder.build());
        }

        return BatchAdministerResponse.builder()
                .total(request.getOrderIds().size())
                .administered(administeredCount)
                .failed(failedCount)
                .logs(batchLogs)
                .build();
    }

    public RegenerateSchedulesResponse regenerateSchedules(Long facilityId,
                                                           Long orderId,
                                                           RegenerateSchedulesRequest request) {
        log.info("Regenerating schedules for order: {}, facility: {}", orderId, facilityId);

        MedicationOrder order = medicationOrderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.MAR_REGENERATE_ORDER_NOT_FOUND));

        if (!MedicationStatus.ACTIVE.equals(order.getStatus())) {
            throw new AppException(ErrorCode.MAR_REGENERATE_ORDER_NOT_ACTIVE);
        }

        List<LocalTime> newTimes = new ArrayList<>();
        for (String timeStr : request.getNewScheduledTimes()) {
            try {
                LocalTime time = LocalTime.parse(timeStr, TIME_FORMATTER);
                newTimes.add(time);
            } catch (Exception e) {
                throw new AppException(ErrorCode.MAR_REGENERATE_INVALID_TIME);
            }
        }

        if (newTimes.isEmpty()) {
            throw new AppException(ErrorCode.MAR_REGENERATE_NO_TIMES);
        }

        List<MedicationSchedule> existingSchedules = medicationScheduleRepository
                .findByOrderIdAndIsActive(orderId);

        for (MedicationSchedule schedule : existingSchedules) {
            schedule.setIsActive(false);
            medicationScheduleRepository.save(schedule);
        }

        List<RegenerateSchedulesResponse.ScheduleInfo> scheduleInfos = new ArrayList<>();
        for (LocalTime time : newTimes) {
            MedicationSchedule newSchedule = MedicationSchedule.builder()
                    .order(order)
                    .scheduledTime(time)
                    .isActive(true)
                    .build();

            MedicationSchedule savedSchedule = medicationScheduleRepository.save(newSchedule);

            scheduleInfos.add(RegenerateSchedulesResponse.ScheduleInfo.builder()
                    .id(savedSchedule.getId())
                    .scheduledTime(savedSchedule.getScheduledTime().format(TIME_FORMATTER))
                    .isActive(savedSchedule.getIsActive())
                    .build());
        }

        order.setUpdatedAt(OffsetDateTime.now());
        medicationOrderRepository.save(order);

        return RegenerateSchedulesResponse.builder()
                .orderId(orderId)
                .schedules(scheduleInfos)
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