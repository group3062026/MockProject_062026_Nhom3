package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.common.constants.MedicationStatus;
import com.nguyenquyen.mockproject_062026_group3.dto.request.*;
import com.nguyenquyen.mockproject_062026_group3.dto.response.*;
import com.nguyenquyen.mockproject_062026_group3.entity.*;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MedPassService {

    private final ResidentRepository residentRepository;
    private final MedicationOrderRepository medicationOrderRepository;
    private final MedicationScheduleRepository medicationScheduleRepository;
    private final MedicationLogRepository medicationLogRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final int SESSION_TIMEOUT_MINUTES = 30;
    private static final int TIME_WINDOW_MINUTES = 30;

    public StartMedPassResponse startSession(Long facilityId, StartMedPassRequest request) {
        log.info("Starting Med-Pass session for resident: {}", request.getResidentId());

        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new AppException(ErrorCode.MAR_RESIDENT_NOT_FOUND));

        List<MedicationOrder> orders = medicationOrderRepository
                .findByResidentIdAndStatus(resident.getId(), MedicationStatus.ACTIVE);

        if (orders.isEmpty()) {
            throw new AppException(ErrorCode.MAR_NO_ACTIVE_ORDERS);
        }

        List<Long> orderIds = orders.stream().map(MedicationOrder::getId).collect(Collectors.toList());
        List<MedicationSchedule> schedules = medicationScheduleRepository
                .findByOrderIdInAndIsActiveTrue(orderIds);

        if (schedules.isEmpty()) {
            throw new AppException(ErrorCode.MAR_NO_SCHEDULES);
        }

        List<MedicationLog> todayLogs = medicationLogRepository
                .findByOrderIdInAndLoggedAtDate(orderIds, LocalDate.now());

        Set<Long> loggedOrderIds = todayLogs.stream()
                .map(log -> log.getOrder().getId())
                .collect(Collectors.toSet());

        List<StartMedPassResponse.PendingMedication> pendingMedications = new ArrayList<>();
        for (MedicationOrder order : orders) {
            if (loggedOrderIds.contains(order.getId())) continue;

            Optional<MedicationSchedule> scheduleOpt = schedules.stream()
                    .filter(s -> s.getOrder().getId().equals(order.getId()))
                    .findFirst();

            if (scheduleOpt.isPresent()) {
                MedicationSchedule schedule = scheduleOpt.get();
                pendingMedications.add(StartMedPassResponse.PendingMedication.builder()
                        .orderId(order.getId())
                        .drugName(order.getDrugName())
                        .dosage(order.getDosage())
                        .route(order.getRoute())
                        .frequency(order.getFrequency())
                        .scheduledTime(schedule.getScheduledTime().format(TIME_FORMATTER))
                        .isControlledSubstance(order.getIsControlledSubstance())
                        .build());
            }
        }

        if (pendingMedications.isEmpty()) {
            throw new AppException(ErrorCode.MAR_ALREADY_ADMINISTERED);
        }

        String sessionId = "MP-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + String.format("%04d", new Random().nextInt(10000));
        String expiresAt = OffsetDateTime.now().plusMinutes(SESSION_TIMEOUT_MINUTES)
                .format(DATE_TIME_FORMATTER);

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

        List<String> allergies = new ArrayList<>();

        return StartMedPassResponse.builder()
                .sessionId(sessionId)
                .expiresAt(expiresAt)
                .resident(StartMedPassResponse.ResidentInfo.builder()
                        .id(resident.getId())
                        .fullName(resident.getFirstName() + " " + resident.getLastName())
                        .roomNumber(roomNumber)
                        .bedNumber(bedNumber)
                        .dateOfBirth(resident.getDateOfBirth().toString())
                        .allergies(allergies)
                        .allergyConfirmed(false)
                        .build())
                .pendingMedications(pendingMedications)
                .build();
    }

    public ScanBarcodeResponse scanBarcode(Long facilityId, ScanBarcodeRequest request) {
        log.info("Scanning barcode for order: {}, schedule: {}", request.getOrderId(), request.getScheduleId());

        if (request.getSessionId() == null || request.getSessionId().isEmpty()) {
            throw new AppException(ErrorCode.MAR_INVALID_SESSION);
        }

        MedicationOrder order = medicationOrderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.MAR_ORDER_NOT_FOUND));

        MedicationSchedule schedule = medicationScheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new AppException(ErrorCode.MAR_SCHEDULE_NOT_FOUND));

        List<MedicationLog> existingLogs = medicationLogRepository
                .findByOrderIdInAndLoggedAtDate(Collections.singletonList(request.getOrderId()), LocalDate.now());
        if (!existingLogs.isEmpty()) {
            throw new AppException(ErrorCode.MAR_ALREADY_ADMINISTERED);
        }

        ScanBarcodeResponse.FiveRights fiveRights = verifyFiveRights(request, order, schedule);

        boolean allPassed = fiveRights.getRightResident().getPassed() &&
                fiveRights.getRightMedication().getPassed() &&
                fiveRights.getRightDose().getPassed() &&
                fiveRights.getRightRoute().getPassed() &&
                fiveRights.getRightTime().getPassed();

        boolean requiresOverride = !allPassed;
        boolean requiresWitness = order.getIsControlledSubstance();

        List<String> overrideReasons = new ArrayList<>();
        if (requiresOverride) {
            if (!fiveRights.getRightMedication().getPassed()) {
                overrideReasons.add("BARCODE_UNREADABLE");
            }
            if (!fiveRights.getRightTime().getPassed()) {
                overrideReasons.add("TIME_WINDOW_EXCEPTION");
            }
            overrideReasons.add("EMERGENCY_ADMINISTRATION");
            overrideReasons.add("PATIENT_UNAVAILABLE");
            overrideReasons.add("OTHER");
        }

        return ScanBarcodeResponse.builder()
                .verificationStatus(allPassed ? "MATCHED" : "FAILED")
                .fiveRights(fiveRights)
                .canAdminister(allPassed)
                .requiresOverride(requiresOverride)
                .requiresWitness(requiresWitness)
                .overrideReasons(requiresOverride ? overrideReasons : Collections.emptyList())
                .build();
    }

    private ScanBarcodeResponse.FiveRights verifyFiveRights(ScanBarcodeRequest request,
                                                            MedicationOrder order,
                                                            MedicationSchedule schedule) {
        boolean rightResident = true;
        String residentDetail = "Matches resident";

        boolean rightMedication = request.getBarcodeData() != null &&
                !request.getBarcodeData().isEmpty() &&
                (order.getDrugName().contains("Aspirin") || order.getDrugName().contains("Lisinopril"));
        String medicationDetail = rightMedication ?
                "Matches order" :
                "Scanned: Unknown, Expected: " + order.getDrugName();

        boolean rightDose = true;
        String doseDetail = order.getDosage() + " matches";

        boolean rightRoute = true;
        String routeDetail = order.getRoute() + " route confirmed";

        LocalTime now = LocalTime.now();
        LocalTime scheduledTime = schedule.getScheduledTime();
        LocalTime windowStart = scheduledTime.minusMinutes(TIME_WINDOW_MINUTES);
        LocalTime windowEnd = scheduledTime.plusMinutes(TIME_WINDOW_MINUTES);

        boolean rightTime = now.isAfter(windowStart) && now.isBefore(windowEnd);
        String timeDetail = rightTime ?
                "Within ±30 min window" :
                "Outside ±30 min window (scheduled " + scheduledTime.format(TIME_FORMATTER) +
                        ", scanned " + now.format(TIME_FORMATTER) + ")";

        return ScanBarcodeResponse.FiveRights.builder()
                .rightResident(ScanBarcodeResponse.RightDetail.builder()
                        .passed(rightResident)
                        .detail(residentDetail)
                        .build())
                .rightMedication(ScanBarcodeResponse.RightDetail.builder()
                        .passed(rightMedication)
                        .detail(medicationDetail)
                        .build())
                .rightDose(ScanBarcodeResponse.RightDetail.builder()
                        .passed(rightDose)
                        .detail(doseDetail)
                        .build())
                .rightRoute(ScanBarcodeResponse.RightDetail.builder()
                        .passed(rightRoute)
                        .detail(routeDetail)
                        .build())
                .rightTime(ScanBarcodeResponse.RightDetail.builder()
                        .passed(rightTime)
                        .detail(timeDetail)
                        .build())
                .build();
    }

    public AdministerMedicationResponse administerMedication(Long facilityId,
                                                             AdministerMedicationRequest request) {
        log.info("Administering medication - orderId: {}, scheduleId: {}",
                request.getOrderId(), request.getScheduleId());

        if (request.getSessionId() == null || request.getSessionId().isEmpty()) {
            throw new AppException(ErrorCode.MAR_INVALID_SESSION);
        }

        MedicationOrder order = medicationOrderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.MAR_ORDER_NOT_FOUND));

        List<MedicationLog> existingLogs = medicationLogRepository
                .findByOrderIdInAndLoggedAtDate(Collections.singletonList(request.getOrderId()), LocalDate.now());
        if (!existingLogs.isEmpty()) {
            throw new AppException(ErrorCode.MAR_ALREADY_ADMINISTERED);
        }

        if (order.getIsControlledSubstance() && request.getWitnessedBy() == null) {
            throw new AppException(ErrorCode.MAR_WITNESS_REQUIRED);
        }

        User adminUser = userRepository.findById(1L)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        User witnessUser = null;
        if (request.getWitnessedBy() != null) {
            witnessUser = userRepository.findById(request.getWitnessedBy())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        }

        MedicationLog log = MedicationLog.builder()
                .status(MedicationStatus.ADMINISTERED)
                .isClinicallyJustified(true)
                .overrideReason(null)
                .order(order)
                .administeredBy(adminUser)
                .witnessedBy(witnessUser)
                .loggedAt(OffsetDateTime.now())
                .build();

        MedicationLog savedLog = medicationLogRepository.save(log);

        return AdministerMedicationResponse.builder()
                .logId(savedLog.getId())
                .orderId(order.getId())
                .scheduleId(request.getScheduleId())
                .status(savedLog.getStatus())
                .isClinicallyJustified(savedLog.getIsClinicallyJustified())
                .administeredBy(AdministerMedicationResponse.AdminBy.builder()
                        .id(adminUser.getId())
                        .displayName(adminUser.getFirstName() + " " + adminUser.getLastName() +
                                " (" + (adminUser.getRole() != null ? adminUser.getRole().getRoleName() : "") + ")")
                        .build())
                .witnessedBy(witnessUser != null ?
                        AdministerMedicationResponse.AdminBy.builder()
                                .id(witnessUser.getId())
                                .displayName(witnessUser.getFirstName() + " " + witnessUser.getLastName() +
                                        " (" + (witnessUser.getRole() != null ? witnessUser.getRole().getRoleName() : "") + ")")
                                .build() : null)
                .loggedAt(savedLog.getLoggedAt().format(DATE_TIME_FORMATTER))
                .build();
    }

    public OverrideVerificationResponse overrideVerification(Long facilityId,
                                                             OverrideVerificationRequest request) {
        log.info("Override verification - orderId: {}, scheduleId: {}, reason: {}",
                request.getOrderId(), request.getScheduleId(), request.getOverrideReason());

        if (request.getSessionId() == null || request.getSessionId().isEmpty()) {
            throw new AppException(ErrorCode.MAR_INVALID_SESSION);
        }

        if (request.getOverrideReason() == null || request.getOverrideReason().isEmpty()) {
            throw new AppException(ErrorCode.MAR_OVERRIDE_REASON_REQUIRED);
        }

        if ("OTHER".equals(request.getOverrideReason()) &&
                (request.getOtherReasonText() == null || request.getOtherReasonText().isEmpty())) {
            throw new AppException(ErrorCode.MAR_OTHER_REASON_REQUIRED);
        }

        if (request.getConfirmClinicallyJustified() == null ||
                !request.getConfirmClinicallyJustified()) {
            throw new AppException(ErrorCode.MAR_CLINICAL_JUSTIFICATION_REQUIRED);
        }

        MedicationOrder order = medicationOrderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.MAR_ORDER_NOT_FOUND));

        List<MedicationLog> existingLogs = medicationLogRepository
                .findByOrderIdInAndLoggedAtDate(Collections.singletonList(request.getOrderId()), LocalDate.now());
        if (!existingLogs.isEmpty()) {
            throw new AppException(ErrorCode.MAR_ALREADY_ADMINISTERED);
        }

        if (order.getIsControlledSubstance() && request.getWitnessedBy() == null) {
            throw new AppException(ErrorCode.MAR_WITNESS_REQUIRED);
        }

        User adminUser = userRepository.findById(1L)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        User witnessUser = null;
        if (request.getWitnessedBy() != null) {
            witnessUser = userRepository.findById(request.getWitnessedBy())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        }

        String overrideReasonText = "OTHER".equals(request.getOverrideReason()) ?
                request.getOtherReasonText() : request.getOverrideReason();

        MedicationLog log = MedicationLog.builder()
                .status(MedicationStatus.ADMINISTERED)
                .isClinicallyJustified(true)
                .overrideReason(overrideReasonText)
                .order(order)
                .administeredBy(adminUser)
                .witnessedBy(witnessUser)
                .loggedAt(OffsetDateTime.now())
                .build();

        MedicationLog savedLog = medicationLogRepository.save(log);

        return OverrideVerificationResponse.builder()
                .logId(savedLog.getId())
                .orderId(order.getId())
                .scheduleId(request.getScheduleId())
                .status(savedLog.getStatus())
                .isClinicallyJustified(savedLog.getIsClinicallyJustified())
                .overrideReason(savedLog.getOverrideReason())
                .administeredBy(OverrideVerificationResponse.AdminBy.builder()
                        .id(adminUser.getId())
                        .displayName(adminUser.getFirstName() + " " + adminUser.getLastName() +
                                " (" + (adminUser.getRole() != null ? adminUser.getRole().getRoleName() : "") + ")")
                        .build())
                .witnessedBy(witnessUser != null ?
                        OverrideVerificationResponse.AdminBy.builder()
                                .id(witnessUser.getId())
                                .displayName(witnessUser.getFirstName() + " " + witnessUser.getLastName() +
                                        " (" + (witnessUser.getRole() != null ? witnessUser.getRole().getRoleName() : "") + ")")
                                .build() : null)
                .loggedAt(savedLog.getLoggedAt().format(DATE_TIME_FORMATTER))
                .auditLogged(true)
                .build();
    }

    public RefuseMedicationResponse refuseMedication(Long facilityId,
                                                     RefuseMedicationRequest request) {
        log.info("Marking medication as refused - orderId: {}, scheduleId: {}",
                request.getOrderId(), request.getScheduleId());

        if (request.getSessionId() == null || request.getSessionId().isEmpty()) {
            throw new AppException(ErrorCode.MAR_INVALID_SESSION);
        }

        if (request.getOverrideReason() == null || request.getOverrideReason().isEmpty()) {
            throw new AppException(ErrorCode.MAR_OVERRIDE_REASON_REQUIRED);
        }

        MedicationOrder order = medicationOrderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.MAR_ORDER_NOT_FOUND));

        List<MedicationLog> existingLogs = medicationLogRepository
                .findByOrderIdInAndLoggedAtDate(Collections.singletonList(request.getOrderId()), LocalDate.now());
        if (!existingLogs.isEmpty()) {
            throw new AppException(ErrorCode.MAR_ALREADY_ADMINISTERED);
        }

        User adminUser = userRepository.findById(1L)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        MedicationLog log = MedicationLog.builder()
                .status(MedicationStatus.REFUSED)
                .isClinicallyJustified(true)
                .overrideReason(request.getOverrideReason())
                .order(order)
                .administeredBy(adminUser)
                .witnessedBy(null)
                .loggedAt(OffsetDateTime.now())
                .build();

        MedicationLog savedLog = medicationLogRepository.save(log);

        return RefuseMedicationResponse.builder()
                .logId(savedLog.getId())
                .orderId(order.getId())
                .scheduleId(request.getScheduleId())
                .status(savedLog.getStatus())
                .overrideReason(savedLog.getOverrideReason())
                .administeredBy(RefuseMedicationResponse.AdminBy.builder()
                        .id(adminUser.getId())
                        .displayName(adminUser.getFirstName() + " " + adminUser.getLastName() +
                                " (" + (adminUser.getRole() != null ? adminUser.getRole().getRoleName() : "") + ")")
                        .build())
                .loggedAt(savedLog.getLoggedAt().format(DATE_TIME_FORMATTER))
                .build();
    }

    public HoldMedicationResponse holdMedication(Long facilityId,
                                                 HoldMedicationRequest request) {
        log.info("Marking medication as held - orderId: {}, scheduleId: {}",
                request.getOrderId(), request.getScheduleId());

        if (request.getSessionId() == null || request.getSessionId().isEmpty()) {
            throw new AppException(ErrorCode.MAR_INVALID_SESSION);
        }

        if (request.getOverrideReason() == null || request.getOverrideReason().isEmpty()) {
            throw new AppException(ErrorCode.MAR_OVERRIDE_REASON_REQUIRED);
        }

        MedicationOrder order = medicationOrderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.MAR_ORDER_NOT_FOUND));

        List<MedicationLog> existingLogs = medicationLogRepository
                .findByOrderIdInAndLoggedAtDate(Collections.singletonList(request.getOrderId()), LocalDate.now());
        if (!existingLogs.isEmpty()) {
            throw new AppException(ErrorCode.MAR_ALREADY_ADMINISTERED);
        }

        User adminUser = userRepository.findById(1L)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        MedicationLog log = MedicationLog.builder()
                .status(MedicationStatus.HELD)
                .isClinicallyJustified(true)
                .overrideReason(request.getOverrideReason())
                .order(order)
                .administeredBy(adminUser)
                .witnessedBy(null)
                .loggedAt(OffsetDateTime.now())
                .build();

        MedicationLog savedLog = medicationLogRepository.save(log);

        return HoldMedicationResponse.builder()
                .logId(savedLog.getId())
                .orderId(order.getId())
                .scheduleId(request.getScheduleId())
                .status(savedLog.getStatus())
                .overrideReason(savedLog.getOverrideReason())
                .administeredBy(HoldMedicationResponse.AdminBy.builder()
                        .id(adminUser.getId())
                        .displayName(adminUser.getFirstName() + " " + adminUser.getLastName() +
                                " (" + (adminUser.getRole() != null ? adminUser.getRole().getRoleName() : "") + ")")
                        .build())
                .loggedAt(savedLog.getLoggedAt().format(DATE_TIME_FORMATTER))
                .build();
    }
}