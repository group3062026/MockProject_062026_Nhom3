package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.common.constants.MedicationStatus;
import com.nguyenquyen.mockproject_062026_group3.dto.request.MarExportRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.MedicationAuditRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.PhiAccessLogRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.response.MarExportResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.response.MedicationAuditResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.response.PhiAccessLogResponse;
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
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final PhiAccessLogRepository phiAccessLogRepository;
    private final MedicationLogRepository medicationLogRepository;
    private final MedicationOrderRepository medicationOrderRepository;
    private final ResidentRepository residentRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String CSV_HEADER = "Date,Resident,Medication,Status,Administered By,Witness,Override Reason,Timestamp\n";

    public MedicationAuditResponse getMedicationAuditLog(Long facilityId, MedicationAuditRequest request) {
        log.info("Getting medication audit log for facility: {}, residentId: {}, orderId: {}, action: {}, dateRange: {} to {}",
                facilityId, request.getResidentId(), request.getOrderId(), request.getAction(),
                request.getStartDate(), request.getEndDate());

        int page = request.getPage() != null ? request.getPage() : 1;
        int limit = request.getLimit() != null ? request.getLimit() : 50;
        Pageable pageable = PageRequest.of(page - 1, limit);

        OffsetDateTime startDateTime = request.getStartDate().atStartOfDay().atOffset(java.time.ZoneOffset.UTC);
        OffsetDateTime endDateTime = request.getEndDate().atTime(23, 59, 59).atOffset(java.time.ZoneOffset.UTC);

        Page<AuditLog> auditLogPage;

        if (request.getResidentId() != null) {
            List<MedicationOrder> orders = medicationOrderRepository
                    .findByResidentIdAndStatus(request.getResidentId(), MedicationStatus.ACTIVE);
            List<Long> orderIds = orders.stream().map(MedicationOrder::getId).collect(Collectors.toList());

            if (orderIds.isEmpty()) {
                return buildEmptyAuditResponse(page, limit);
            }

            auditLogPage = auditLogRepository.findByTableNameAndRecordIdInAndPerformedAtBetween(
                    "medication_logs",
                    orderIds.stream().map(String::valueOf).collect(Collectors.toList()),
                    startDateTime,
                    endDateTime,
                    pageable);

        } else if (request.getOrderId() != null) {
            auditLogPage = auditLogRepository.findByTableNameAndRecordIdAndPerformedAtBetween(
                    "medication_logs",
                    String.valueOf(request.getOrderId()),
                    startDateTime,
                    endDateTime,
                    pageable);

        } else {
            List<Resident> residents = residentRepository.findByFacilityIdAndStatus(facilityId, MedicationStatus.ACTIVE);
            List<Long> residentIds = residents.stream().map(Resident::getId).collect(Collectors.toList());

            if (residentIds.isEmpty()) {
                return buildEmptyAuditResponse(page, limit);
            }

            List<MedicationOrder> orders = medicationOrderRepository
                    .findByResidentIdInAndStatus(residentIds, MedicationStatus.ACTIVE);
            List<Long> orderIds = orders.stream().map(MedicationOrder::getId).collect(Collectors.toList());

            if (orderIds.isEmpty()) {
                return buildEmptyAuditResponse(page, limit);
            }

            auditLogPage = auditLogRepository.findByTableNameAndRecordIdInAndPerformedAtBetween(
                    "medication_logs",
                    orderIds.stream().map(String::valueOf).collect(Collectors.toList()),
                    startDateTime,
                    endDateTime,
                    pageable);
        }

        List<AuditLog> filteredLogs = auditLogPage.getContent();
        if (request.getAction() != null && !request.getAction().isEmpty()) {
            filteredLogs = filteredLogs.stream()
                    .filter(log -> log.getAction().equalsIgnoreCase(request.getAction()))
                    .collect(Collectors.toList());
        }

        List<MedicationAuditResponse.AuditLog> auditLogs = filteredLogs.stream()
                .map(this::convertToAuditLogResponse)
                .collect(Collectors.toList());

        int total = auditLogPage.getTotalPages();
        boolean hasNext = page < total;
        boolean hasPrevious = page > 1;

        return MedicationAuditResponse.builder()
                .total((int) auditLogPage.getTotalElements())
                .page(page)
                .limit(limit)
                .logs(auditLogs)
                .build();
    }

    private MedicationAuditResponse buildEmptyAuditResponse(int page, int limit) {
        return MedicationAuditResponse.builder()
                .total(0)
                .page(page)
                .limit(limit)
                .logs(Collections.emptyList())
                .build();
    }

    private MedicationAuditResponse.AuditLog convertToAuditLogResponse(AuditLog auditLog) {
        return MedicationAuditResponse.AuditLog.builder()
                .id(auditLog.getId())
                .tableName(auditLog.getTableName())
                .recordId(auditLog.getRecordId())
                .action(auditLog.getAction())
                .oldData(auditLog.getOldData())
                .newData(auditLog.getNewData())
                .performedBy(auditLog.getPerformedBy() != null ?
                        MedicationAuditResponse.AuditLog.PerformedBy.builder()
                                .id(auditLog.getPerformedBy().getId())
                                .displayName(getUserDisplayName(auditLog.getPerformedBy()))
                                .build() : null)
                .performedAt(auditLog.getPerformedAt().format(DATE_TIME_FORMATTER))
                .ipAddress(auditLog.getIpAddress())
                .build();
    }

    public PhiAccessLogResponse getPhiAccessLog(Long facilityId, PhiAccessLogRequest request) {
        log.info("Getting PHI access log for facility: {}, residentId: {}, accessType: {}, dateRange: {} to {}",
                facilityId, request.getResidentId(), request.getAccessType(),
                request.getStartDate(), request.getEndDate());

        OffsetDateTime startDateTime = request.getStartDate().atStartOfDay().atOffset(java.time.ZoneOffset.UTC);
        OffsetDateTime endDateTime = request.getEndDate().atTime(23, 59, 59).atOffset(java.time.ZoneOffset.UTC);

        List<PhiAccessLog> logs;

        if (request.getAccessType() != null && !request.getAccessType().isEmpty()) {
            logs = phiAccessLogRepository.findByRecordIdAndAccessTypeAndAccessedAtBetween(
                    String.valueOf(request.getResidentId()),
                    request.getAccessType(),
                    startDateTime,
                    endDateTime);
        } else {
            logs = phiAccessLogRepository.findByRecordIdAndAccessedAtBetween(
                    String.valueOf(request.getResidentId()),
                    startDateTime,
                    endDateTime);
        }

        if (logs.isEmpty()) {
            throw new AppException(ErrorCode.MAR_PHI_ACCESS_NOT_FOUND);
        }

        logs = logs.stream()
                .filter(log -> {
                    try {
                        Long residentId = Long.parseLong(log.getRecordId());
                        Resident resident = residentRepository.findById(residentId).orElse(null);
                        if (resident != null && resident.getBed() != null && resident.getBed().getRoom() != null) {
                            return resident.getBed().getRoom().getFacility().getId().equals(facilityId);
                        }
                    } catch (Exception e) {
                    }
                    return false;
                })
                .collect(Collectors.toList());

        List<PhiAccessLogResponse.PhiAccessLog> phiLogs = logs.stream()
                .map(this::convertToPhiAccessLogResponse)
                .collect(Collectors.toList());

        return PhiAccessLogResponse.builder()
                .logs(phiLogs)
                .build();
    }

    private PhiAccessLogResponse.PhiAccessLog convertToPhiAccessLogResponse(PhiAccessLog phiLog) {
        return PhiAccessLogResponse.PhiAccessLog.builder()
                .id(phiLog.getId())
                .tableName(phiLog.getTableName())
                .recordId(phiLog.getRecordId())
                .accessedBy(phiLog.getAccessedBy() != null ?
                        PhiAccessLogResponse.PhiAccessLog.AccessedBy.builder()
                                .id(phiLog.getAccessedBy().getId())
                                .displayName(getUserDisplayName(phiLog.getAccessedBy()))
                                .build() : null)
                .accessType(phiLog.getAccessType())
                .accessReason(phiLog.getAccessReason())
                .ipAddress(phiLog.getIpAddress())
                .accessedAt(phiLog.getAccessedAt().format(DATE_TIME_FORMATTER))
                .build();
    }

    public byte[] exportMarAuditReport(Long facilityId, MarExportRequest request) {
        log.info("Exporting MAR audit report for facility: {}, residentId: {}, dateRange: {} to {}",
                facilityId, request.getResidentId(), request.getStartDate(), request.getEndDate());

        try {
            StringBuilder csvContent = new StringBuilder();
            csvContent.append(CSV_HEADER);

            List<Resident> residents;
            if (request.getResidentId() != null) {
                Resident resident = residentRepository.findById(request.getResidentId())
                        .orElseThrow(() -> new AppException(ErrorCode.MAR_RESIDENT_NOT_FOUND));
                residents = Collections.singletonList(resident);
            } else {
                residents = residentRepository.findByFacilityIdAndStatus(facilityId, MedicationStatus.ACTIVE);
            }

            if (residents.isEmpty()) {
                return csvContent.toString().getBytes();
            }

            List<Long> residentIds = residents.stream().map(Resident::getId).collect(Collectors.toList());
            List<MedicationOrder> orders = medicationOrderRepository
                    .findByResidentIdInAndStatus(residentIds, MedicationStatus.ACTIVE);

            if (orders.isEmpty()) {
                return csvContent.toString().getBytes();
            }

            List<Long> orderIds = orders.stream().map(MedicationOrder::getId).collect(Collectors.toList());

            OffsetDateTime startDateTime = request.getStartDate().atStartOfDay().atOffset(java.time.ZoneOffset.UTC);
            OffsetDateTime endDateTime = request.getEndDate().atTime(23, 59, 59).atOffset(java.time.ZoneOffset.UTC);

            List<MedicationLog> logs = medicationLogRepository
                    .findByOrderIdInAndLoggedAtBetween(orderIds, startDateTime, endDateTime);

            for (MedicationLog log : logs) {
                MedicationOrder order = log.getOrder();
                Resident resident = order.getResident();

                String row = String.format("%s,%s,%s,%s,%s,%s,%s,%s\n",
                        log.getLoggedAt().format(DATE_FORMATTER),
                        resident != null ? resident.getFirstName() + " " + resident.getLastName() : "N/A",
                        order != null ? order.getDrugName() : "N/A",
                        log.getStatus() != null ? log.getStatus() : "N/A",
                        log.getAdministeredBy() != null ? getUserDisplayName(log.getAdministeredBy()) : "N/A",
                        log.getWitnessedBy() != null ? getUserDisplayName(log.getWitnessedBy()) : "",
                        log.getOverrideReason() != null ? log.getOverrideReason() : "",
                        log.getLoggedAt().format(DATE_TIME_FORMATTER)
                );

                csvContent.append(row);
            }

            return csvContent.toString().getBytes();

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error exporting MAR audit report", e);
            throw new AppException(ErrorCode.MAR_AUDIT_EXPORT_FAILED);
        }
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