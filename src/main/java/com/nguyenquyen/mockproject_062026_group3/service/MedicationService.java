package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.dto.MedicationDtos;
import com.nguyenquyen.mockproject_062026_group3.entity.MedicationLog;
import com.nguyenquyen.mockproject_062026_group3.entity.MedicationOrder;
import com.nguyenquyen.mockproject_062026_group3.entity.MedicationSchedule;
import com.nguyenquyen.mockproject_062026_group3.entity.Resident;
import com.nguyenquyen.mockproject_062026_group3.entity.User;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.repository.MedicationLogRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.MedicationOrderRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.MedicationScheduleRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.ResidentRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class MedicationService {

    private static final String ORDER_STATUS_ACTIVE = "ACTIVE";
    private static final String LOG_STATUS_ADMINISTERED = "ADMINISTERED";
    private static final String LOG_STATUS_REFUSED = "REFUSED";
    private static final String LOG_STATUS_HELD = "HELD";

    @Autowired
    private MedicationOrderRepository medicationOrderRepository;

    @Autowired
    private MedicationScheduleRepository medicationScheduleRepository;

    @Autowired
    private MedicationLogRepository medicationLogRepository;

    @Autowired
    private ResidentRepository residentRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<MedicationDtos.MedicationOrderResponse> getMedicationOrders(
            Long residentId,
            String status,
            String search) {
        return medicationOrderRepository.findAll().stream()
                .filter(order -> !Boolean.TRUE.equals(order.getIsDeleted()))
                .filter(order -> residentId == null || Objects.equals(order.getResident().getId(), residentId))
                .filter(order -> status == null || status.isBlank()
                        || "ALL".equalsIgnoreCase(status)
                        || order.getStatus().equalsIgnoreCase(status))
                .filter(order -> search == null || search.isBlank()
                        || order.getDrugName().toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT)))
                .map(MedicationDtos.MedicationOrderResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public MedicationDtos.MedicationOrderResponse getMedicationOrderById(Long id) {
        return MedicationDtos.MedicationOrderResponse.fromEntity(getOrder(id));
    }

    @Transactional
    public MedicationDtos.MedicationOrderResponse createMedicationOrder(
            MedicationDtos.MedicationOrderCreateRequest request) {
        validateRequired(request.getResidentId());
        validateText(request.getDrugName());
        validateText(request.getDosage());
        validateText(request.getRoute());
        validateText(request.getFrequency());
        validateRequired(request.getPrescribedBy());

        Resident resident = residentRepository.findById(request.getResidentId())
                .orElseThrow(() -> new AppException(ErrorCode.RESIDENT_NOT_FOUND));
        User prescribedBy = userRepository.findById(request.getPrescribedBy())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        MedicationOrder order = MedicationOrder.builder()
                .resident(resident)
                .drugName(request.getDrugName().trim())
                .dosage(request.getDosage().trim())
                .route(normalizeCode(request.getRoute()))
                .frequency(request.getFrequency().trim())
                .isControlledSubstance(Boolean.TRUE.equals(request.getIsControlledSubstance()))
                .status(ORDER_STATUS_ACTIVE)
                .prescribedBy(prescribedBy)
                .isDeleted(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        return MedicationDtos.MedicationOrderResponse.fromEntity(medicationOrderRepository.save(order));
    }

    @Transactional
    public MedicationDtos.MedicationOrderResponse updateMedicationOrder(
            Long id,
            MedicationDtos.MedicationOrderUpdateRequest request) {
        MedicationOrder order = getOrder(id);

        if (request.getDrugName() != null) {
            validateText(request.getDrugName());
            order.setDrugName(request.getDrugName().trim());
        }
        if (request.getDosage() != null) {
            validateText(request.getDosage());
            order.setDosage(request.getDosage().trim());
        }
        if (request.getRoute() != null) {
            validateText(request.getRoute());
            order.setRoute(normalizeCode(request.getRoute()));
        }
        if (request.getFrequency() != null) {
            validateText(request.getFrequency());
            order.setFrequency(request.getFrequency().trim());
        }
        if (request.getIsControlledSubstance() != null) {
            order.setIsControlledSubstance(request.getIsControlledSubstance());
        }
        if (request.getStatus() != null) {
            order.setStatus(validateOrderStatus(request.getStatus()));
        }

        order.setUpdatedAt(OffsetDateTime.now());
        return MedicationDtos.MedicationOrderResponse.fromEntity(medicationOrderRepository.save(order));
    }

    @Transactional
    public MedicationDtos.MedicationOrderResponse deleteMedicationOrder(Long id) {
        MedicationOrder order = getOrder(id);
        order.setIsDeleted(true);
        order.setUpdatedAt(OffsetDateTime.now());
        return MedicationDtos.MedicationOrderResponse.fromEntity(medicationOrderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<MedicationDtos.MedicationScheduleResponse> getMedicationSchedules(
            Long orderId,
            Long residentId,
            Boolean isActive,
            LocalDate date) {
        return medicationScheduleRepository.findAll().stream()
                .filter(schedule -> orderId == null || Objects.equals(schedule.getOrder().getId(), orderId))
                .filter(schedule -> residentId == null
                        || Objects.equals(schedule.getOrder().getResident().getId(), residentId))
                .filter(schedule -> isActive == null || Objects.equals(schedule.getIsActive(), isActive))
                .map(MedicationDtos.MedicationScheduleResponse::fromEntity)
                .toList();
    }

    @Transactional
    public MedicationDtos.MedicationScheduleResponse createMedicationSchedule(
            MedicationDtos.MedicationScheduleCreateRequest request) {
        validateRequired(request.getOrderId());
        if (request.getScheduledTime() == null) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }

        MedicationSchedule schedule = MedicationSchedule.builder()
                .order(getOrder(request.getOrderId()))
                .scheduledTime(request.getScheduledTime())
                .isActive(request.getIsActive() == null || request.getIsActive())
                .build();

        return MedicationDtos.MedicationScheduleResponse.fromEntity(medicationScheduleRepository.save(schedule));
    }

    @Transactional
    public MedicationDtos.MedicationScheduleResponse updateMedicationSchedule(
            Long id,
            MedicationDtos.MedicationScheduleUpdateRequest request) {
        MedicationSchedule schedule = medicationScheduleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (request.getScheduledTime() != null) {
            schedule.setScheduledTime(request.getScheduledTime());
        }
        if (request.getIsActive() != null) {
            schedule.setIsActive(request.getIsActive());
        }

        return MedicationDtos.MedicationScheduleResponse.fromEntity(medicationScheduleRepository.save(schedule));
    }

    @Transactional(readOnly = true)
    public List<MedicationDtos.TodayMedicationTaskResponse> getTodayMedicationTasks(
            Long residentId,
            LocalDate date,
            String status) {
        return medicationScheduleRepository.findAll().stream()
                .filter(schedule -> Boolean.TRUE.equals(schedule.getIsActive()))
                .filter(schedule -> ORDER_STATUS_ACTIVE.equalsIgnoreCase(schedule.getOrder().getStatus()))
                .filter(schedule -> !Boolean.TRUE.equals(schedule.getOrder().getIsDeleted()))
                .filter(schedule -> residentId == null
                        || Objects.equals(schedule.getOrder().getResident().getId(), residentId))
                .map(MedicationDtos.TodayMedicationTaskResponse::fromSchedule)
                .filter(task -> status == null || status.isBlank()
                        || task.getStatus().equalsIgnoreCase(status))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MedicationDtos.MedicationLogResponse> getMedicationLogs(
            Long orderId,
            Long residentId,
            String status,
            LocalDate fromDate,
            LocalDate toDate) {
        return medicationLogRepository.findAll().stream()
                .filter(log -> orderId == null || Objects.equals(log.getOrder().getId(), orderId))
                .filter(log -> residentId == null || Objects.equals(log.getOrder().getResident().getId(), residentId))
                .filter(log -> status == null || status.isBlank() || log.getStatus().equalsIgnoreCase(status))
                .filter(log -> fromDate == null || !log.getLoggedAt().toLocalDate().isBefore(fromDate))
                .filter(log -> toDate == null || !log.getLoggedAt().toLocalDate().isAfter(toDate))
                .map(MedicationDtos.MedicationLogResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public MedicationDtos.MedicationLogResponse getMedicationLogById(Long id) {
        MedicationLog log = medicationLogRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        return MedicationDtos.MedicationLogResponse.fromEntity(log);
    }

    @Transactional
    public MedicationDtos.MedicationLogResponse createMedicationLog(
            MedicationDtos.MedicationLogCreateRequest request) {
        validateRequired(request.getOrderId());
        validateText(request.getStatus());
        validateRequired(request.getAdministeredBy());

        return MedicationDtos.MedicationLogResponse.fromEntity(saveMedicationLog(
                request.getOrderId(),
                validateLogStatus(request.getStatus()),
                request.getIsClinicallyJustified(),
                request.getOverrideReason(),
                request.getAdministeredBy(),
                request.getWitnessedBy(),
                request.getLoggedAt()));
    }

    @Transactional
    public MedicationDtos.MedicationLogResponse recordMissedMedication(
            MedicationDtos.MedicationExceptionLogRequest request) {
        validateRequired(request.getOrderId());
        validateRequired(request.getAdministeredBy());
        validateText(request.getOverrideReason());
        return MedicationDtos.MedicationLogResponse.fromEntity(saveMedicationLog(
                request.getOrderId(),
                LOG_STATUS_HELD,
                false,
                request.getOverrideReason(),
                request.getAdministeredBy(),
                request.getWitnessedBy(),
                request.getLoggedAt()));
    }

    @Transactional
    public MedicationDtos.MedicationLogResponse recordRefusedMedication(
            MedicationDtos.MedicationExceptionLogRequest request) {
        validateRequired(request.getOrderId());
        validateRequired(request.getAdministeredBy());
        validateText(request.getOverrideReason());
        return MedicationDtos.MedicationLogResponse.fromEntity(saveMedicationLog(
                request.getOrderId(),
                LOG_STATUS_REFUSED,
                false,
                request.getOverrideReason(),
                request.getAdministeredBy(),
                request.getWitnessedBy(),
                request.getLoggedAt()));
    }

    @Transactional(readOnly = true)
    public MedicationDtos.VerifyMedicationResponse verifyMedicationBeforeAdministration(
            Long orderId,
            MedicationDtos.VerifyMedicationRequest request) {
        MedicationOrder order = getOrder(orderId);
        boolean residentMatches = request.getResidentId() != null
                && Objects.equals(order.getResident().getId(), request.getResidentId());
        boolean drugMatches = request.getDrugName() == null
                || order.getDrugName().equalsIgnoreCase(request.getDrugName().trim());
        boolean verified = residentMatches && drugMatches;

        return MedicationDtos.VerifyMedicationResponse.builder()
                .verified(verified)
                .orderId(order.getId())
                .residentId(order.getResident().getId())
                .drugName(order.getDrugName())
                .requiresOverride(!verified)
                .build();
    }

    private MedicationLog saveMedicationLog(
            Long orderId,
            String status,
            Boolean isClinicallyJustified,
            String overrideReason,
            Long administeredBy,
            Long witnessedBy,
            OffsetDateTime loggedAt) {
        User administeredUser = userRepository.findById(administeredBy)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        User witnessedUser = witnessedBy == null ? null : userRepository.findById(witnessedBy)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        MedicationLog log = MedicationLog.builder()
                .order(getOrder(orderId))
                .status(status)
                .isClinicallyJustified(Boolean.TRUE.equals(isClinicallyJustified))
                .overrideReason(overrideReason)
                .administeredBy(administeredUser)
                .witnessedBy(witnessedUser)
                .loggedAt(loggedAt == null ? OffsetDateTime.now() : loggedAt)
                .build();

        return medicationLogRepository.save(log);
    }

    private MedicationOrder getOrder(Long id) {
        return medicationOrderRepository.findById(id)
                .filter(order -> !Boolean.TRUE.equals(order.getIsDeleted()))
                .orElseThrow(() -> new AppException(ErrorCode.MEDICATION_ORDER_NOT_FOUND));
    }

    private String validateOrderStatus(String value) {
        String status = normalizeCode(value);
        if (!List.of("ACTIVE", "DISCONTINUED", "ON_HOLD").contains(status)) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }
        return status;
    }

    private String validateLogStatus(String value) {
        String status = normalizeCode(value);
        if ("ADMINISTERED".equals(status) || "Administered".equals(value)) {
            return LOG_STATUS_ADMINISTERED;
        }
        if ("REFUSED".equals(status) || "Refused".equals(value)) {
            return LOG_STATUS_REFUSED;
        }
        if ("HELD".equals(status) || "MISSED".equals(status) || "Missed".equals(value)) {
            return LOG_STATUS_HELD;
        }
        if ("NOT_AVAILABLE".equals(status)) {
            return "NOT_AVAILABLE";
        }
        throw new AppException(ErrorCode.INVALID_PARAMETER);
    }

    private String normalizeCode(String value) {
        validateText(value);
        return value.trim().toUpperCase(Locale.ROOT).replace(" ", "_");
    }

    private void validateText(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }
    }

    private void validateRequired(Object value) {
        if (value == null) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }
    }
}
