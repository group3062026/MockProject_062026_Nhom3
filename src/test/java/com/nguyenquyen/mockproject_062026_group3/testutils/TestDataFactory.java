package com.nguyenquyen.mockproject_062026_group3.testutils;

import com.nguyenquyen.mockproject_062026_group3.common.constants.MedicationStatus;
import com.nguyenquyen.mockproject_062026_group3.dto.request.*;
import com.nguyenquyen.mockproject_062026_group3.dto.response.*;
import com.nguyenquyen.mockproject_062026_group3.entity.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.*;

public class TestDataFactory {
    public static MedicationAuditRequest createMedicationAuditRequest(Long residentId,
                                                                      Long orderId,
                                                                      String action,
                                                                      LocalDate startDate,
                                                                      LocalDate endDate,
                                                                      Integer page,
                                                                      Integer limit) {
        return MedicationAuditRequest.builder()
                .residentId(residentId)
                .orderId(orderId)
                .action(action)
                .startDate(startDate != null ? startDate : LocalDate.now().minusDays(7))
                .endDate(endDate != null ? endDate : LocalDate.now())
                .page(page != null ? page : 1)
                .limit(limit != null ? limit : 50)
                .build();
    }

    public static PhiAccessLogRequest createPhiAccessLogRequest(Long residentId,
                                                                String accessType,
                                                                LocalDate startDate,
                                                                LocalDate endDate) {
        return PhiAccessLogRequest.builder()
                .residentId(residentId)
                .accessType(accessType)
                .startDate(startDate != null ? startDate : LocalDate.now().minusDays(7))
                .endDate(endDate != null ? endDate : LocalDate.now())
                .build();
    }

    public static MarExportRequest createMarExportRequest(Long residentId,
                                                          LocalDate startDate,
                                                          LocalDate endDate) {
        return MarExportRequest.builder()
                .residentId(residentId)
                .startDate(startDate != null ? startDate : LocalDate.now().minusDays(7))
                .endDate(endDate != null ? endDate : LocalDate.now())
                .build();
    }

    // ==================== PHẦN 5: Response Builders ====================

    public static MedicationAuditResponse createMedicationAuditResponse() {
        return MedicationAuditResponse.builder()
                .total(10)
                .page(1)
                .limit(50)
                .logs(createAuditLogs())
                .build();
    }

    public static MedicationAuditResponse createEmptyMedicationAuditResponse() {
        return MedicationAuditResponse.builder()
                .total(0)
                .page(1)
                .limit(50)
                .logs(Collections.emptyList())
                .build();
    }

    public static List<MedicationAuditResponse.AuditLog> createAuditLogs() {
        return Arrays.asList(
                MedicationAuditResponse.AuditLog.builder()
                        .id(1L)
                        .tableName("medication_logs")
                        .recordId("1")
                        .action("INSERT")
                        .oldData(null)
                        .newData("{\"status\":\"ADMINISTERED\",\"orderId\":1,\"administeredBy\":7}")
                        .performedBy(MedicationAuditResponse.AuditLog.PerformedBy.builder()
                                .id(7L)
                                .displayName("Nurse Jane Doe (RN)")
                                .build())
                        .performedAt(OffsetDateTime.now().toString())
                        .ipAddress("192.168.1.100")
                        .build(),
                MedicationAuditResponse.AuditLog.builder()
                        .id(2L)
                        .tableName("medication_orders")
                        .recordId("1")
                        .action("UPDATE")
                        .oldData("{\"status\":\"ACTIVE\"}")
                        .newData("{\"status\":\"DISCONTINUED\"}")
                        .performedBy(MedicationAuditResponse.AuditLog.PerformedBy.builder()
                                .id(4L)
                                .displayName("Dr. Brown")
                                .build())
                        .performedAt(OffsetDateTime.now().minusHours(1).toString())
                        .ipAddress("192.168.1.101")
                        .build()
        );
    }

    public static PhiAccessLogResponse createPhiAccessLogResponse() {
        return PhiAccessLogResponse.builder()
                .logs(createPhiAccessLogs())
                .build();
    }

    public static PhiAccessLogResponse createEmptyPhiAccessLogResponse() {
        return PhiAccessLogResponse.builder()
                .logs(Collections.emptyList())
                .build();
    }

    public static List<PhiAccessLogResponse.PhiAccessLog> createPhiAccessLogs() {
        return Arrays.asList(
                PhiAccessLogResponse.PhiAccessLog.builder()
                        .id(1L)
                        .tableName("medication_orders")
                        .recordId("1")
                        .accessedBy(PhiAccessLogResponse.PhiAccessLog.AccessedBy.builder()
                                .id(7L)
                                .displayName("Nurse Jane Doe (RN)")
                                .build())
                        .accessType("VIEW")
                        .accessReason("Shift handoff review")
                        .ipAddress("192.168.1.100")
                        .accessedAt(OffsetDateTime.now().toString())
                        .build(),
                PhiAccessLogResponse.PhiAccessLog.builder()
                        .id(2L)
                        .tableName("medication_logs")
                        .recordId("1")
                        .accessedBy(PhiAccessLogResponse.PhiAccessLog.AccessedBy.builder()
                                .id(8L)
                                .displayName("Nurse John Smith (LPN)")
                                .build())
                        .accessType("PRINT")
                        .accessReason("MAR print for physician")
                        .ipAddress("192.168.1.102")
                        .accessedAt(OffsetDateTime.now().minusHours(2).toString())
                        .build()
        );
    }

    public static MarExportResponse createMarExportResponse() {
        return MarExportResponse.builder()
                .data(createMarExportRows())
                .build();
    }

    public static MarExportResponse createEmptyMarExportResponse() {
        return MarExportResponse.builder()
                .data(Collections.emptyList())
                .build();
    }

    public static List<MarExportResponse.MarExportRow> createMarExportRows() {
        return Arrays.asList(
                MarExportResponse.MarExportRow.builder()
                        .date(LocalDate.now().toString())
                        .residentName("John Smith")
                        .medication("Aspirin 100 mg")
                        .status(MedicationStatus.ADMINISTERED)
                        .administeredBy("Nurse Jane Doe (RN)")
                        .witnessedBy("")
                        .overrideReason("")
                        .timestamp(OffsetDateTime.now().toString())
                        .build(),
                MarExportResponse.MarExportRow.builder()
                        .date(LocalDate.now().minusDays(1).toString())
                        .residentName("Mary Brown")
                        .medication("Insulin 6 Units")
                        .status(MedicationStatus.HELD)
                        .administeredBy("Nurse John Smith (LPN)")
                        .witnessedBy("")
                        .overrideReason("Patient was in therapy")
                        .timestamp(OffsetDateTime.now().minusDays(1).toString())
                        .build()
        );
    }

    public static String createMarExportCSV() {
        return "Date,Resident,Medication,Status,Administered By,Witness,Override Reason,Timestamp\n" +
                LocalDate.now() + ",John Smith,Aspirin 100 mg,ADMINISTERED,Nurse Jane Doe (RN),,,\n" +
                LocalDate.now().minusDays(1) + ",Mary Brown,Insulin 6 Units,HELD,Nurse John Smith (LPN),,Patient was in therapy,\n";
    }

    // ==================== PHẦN 5: Entity Builders ====================

    public static AuditLog createAuditLog(Long id, String tableName, String recordId,
                                          String action, User performedBy, OffsetDateTime performedAt) {
        return AuditLog.builder()
                .id(id)
                .tableName(tableName)
                .recordId(recordId)
                .action(action)
                .oldData(action.equals("INSERT") ? null : "{\"old\":\"data\"}")
                .newData("{\"new\":\"data\"}")
                .performedBy(performedBy)
                .performedAt(performedAt != null ? performedAt : OffsetDateTime.now())
                .ipAddress("192.168.1." + id)
                .build();
    }

    public static PhiAccessLog createPhiAccessLog(Long id, String tableName, String recordId,
                                                  User accessedBy, String accessType,
                                                  String accessReason, OffsetDateTime accessedAt) {
        return PhiAccessLog.builder()
                .id(id)
                .tableName(tableName)
                .recordId(recordId)
                .accessedBy(accessedBy)
                .accessType(accessType)
                .accessReason(accessReason != null ? accessReason : "Test reason")
                .ipAddress("192.168.1." + id)
                .accessedAt(accessedAt != null ? accessedAt : OffsetDateTime.now())
                .build();
    }

    public static List<AuditLog> createMultipleAuditLogs(User performedBy, int count) {
        List<AuditLog> logs = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            logs.add(createAuditLog((long) i, "medication_logs", String.valueOf(i % 3 + 1),
                    i % 2 == 0 ? "UPDATE" : "INSERT", performedBy, OffsetDateTime.now().minusHours(i)));
        }
        return logs;
    }

    public static List<PhiAccessLog> createMultiplePhiAccessLogs(User accessedBy, int count) {
        List<PhiAccessLog> logs = new ArrayList<>();
        String[] accessTypes = {"VIEW", "PRINT", "EXPORT", "DOWNLOAD"};
        for (int i = 1; i <= count; i++) {
            logs.add(createPhiAccessLog((long) i, "medication_orders", String.valueOf(i % 3 + 1),
                    accessedBy, accessTypes[i % accessTypes.length],
                    "Reason " + i, OffsetDateTime.now().minusHours(i)));
        }
        return logs;
    }

    public static BatchAdministerRequest createBatchAdministerRequest(String sessionId,
                                                                      Long residentId,
                                                                      List<Long> orderIds,
                                                                      List<Long> scheduleIds,
                                                                      Long witnessedBy,
                                                                      String notes) {
        return BatchAdministerRequest.builder()
                .sessionId(sessionId)
                .residentId(residentId)
                .orderIds(orderIds)
                .scheduleIds(scheduleIds)
                .witnessedBy(witnessedBy)
                .notes(notes)
                .build();
    }

    public static BatchAdministerRequest createBatchAdministerRequestWithControlled(String sessionId,
                                                                                    Long residentId,
                                                                                    List<Long> orderIds,
                                                                                    List<Long> scheduleIds,
                                                                                    Long witnessedBy,
                                                                                    String notes) {
        return BatchAdministerRequest.builder()
                .sessionId(sessionId)
                .residentId(residentId)
                .orderIds(orderIds)
                .scheduleIds(scheduleIds)
                .witnessedBy(witnessedBy)
                .notes(notes)
                .build();
    }

    public static RegenerateSchedulesRequest createRegenerateSchedulesRequest(List<String> newTimes) {
        return RegenerateSchedulesRequest.builder()
                .newScheduledTimes(newTimes)
                .build();
    }

    // ==================== PHẦN 4: Response Builders ====================

    public static BatchAdministerResponse createBatchAdministerResponse(int total, int success, int failed) {
        List<BatchAdministerResponse.BatchLog> logs = new ArrayList<>();
        for (int i = 1; i <= total; i++) {
            boolean isSuccess = i <= success;
            logs.add(BatchAdministerResponse.BatchLog.builder()
                    .orderId((long) i)
                    .scheduleId((long) i)
                    .status(isSuccess ? MedicationStatus.ADMINISTERED : MedicationStatus.FAILED)
                    .logId(isSuccess ? (long) (1000 + i) : null)
                    .errorMessage(isSuccess ? null : "Failed to administer medication " + i)
                    .build());
        }
        return BatchAdministerResponse.builder()
                .total(total)
                .administered(success)
                .failed(failed)
                .logs(logs)
                .build();
    }

    public static BatchAdministerResponse createBatchAdministerResponseAllSuccess() {
        return BatchAdministerResponse.builder()
                .total(3)
                .administered(3)
                .failed(0)
                .logs(Arrays.asList(
                        BatchAdministerResponse.BatchLog.builder()
                                .orderId(1L)
                                .scheduleId(1L)
                                .status(MedicationStatus.ADMINISTERED)
                                .logId(1001L)
                                .errorMessage(null)
                                .build(),
                        BatchAdministerResponse.BatchLog.builder()
                                .orderId(2L)
                                .scheduleId(2L)
                                .status(MedicationStatus.ADMINISTERED)
                                .logId(1002L)
                                .errorMessage(null)
                                .build(),
                        BatchAdministerResponse.BatchLog.builder()
                                .orderId(3L)
                                .scheduleId(3L)
                                .status(MedicationStatus.ADMINISTERED)
                                .logId(1003L)
                                .errorMessage(null)
                                .build()
                ))
                .build();
    }

    public static BatchAdministerResponse createBatchAdministerResponsePartialFailure() {
        return BatchAdministerResponse.builder()
                .total(3)
                .administered(2)
                .failed(1)
                .logs(Arrays.asList(
                        BatchAdministerResponse.BatchLog.builder()
                                .orderId(1L)
                                .scheduleId(1L)
                                .status(MedicationStatus.ADMINISTERED)
                                .logId(1001L)
                                .errorMessage(null)
                                .build(),
                        BatchAdministerResponse.BatchLog.builder()
                                .orderId(2L)
                                .scheduleId(2L)
                                .status(MedicationStatus.ADMINISTERED)
                                .logId(1002L)
                                .errorMessage(null)
                                .build(),
                        BatchAdministerResponse.BatchLog.builder()
                                .orderId(3L)
                                .scheduleId(3L)
                                .status(MedicationStatus.FAILED)
                                .logId(null)
                                .errorMessage("Medication already administered today")
                                .build()
                ))
                .build();
    }

    public static RegenerateSchedulesResponse createRegenerateSchedulesResponse() {
        return RegenerateSchedulesResponse.builder()
                .orderId(1L)
                .schedules(Arrays.asList(
                        RegenerateSchedulesResponse.ScheduleInfo.builder()
                                .id(10L)
                                .scheduledTime("08:00:00")
                                .isActive(true)
                                .build(),
                        RegenerateSchedulesResponse.ScheduleInfo.builder()
                                .id(11L)
                                .scheduledTime("16:00:00")
                                .isActive(true)
                                .build(),
                        RegenerateSchedulesResponse.ScheduleInfo.builder()
                                .id(12L)
                                .scheduledTime("00:00:00")
                                .isActive(true)
                                .build()
                ))
                .build();
    }

    public static RegenerateSchedulesResponse createRegenerateSchedulesResponseWithSingleTime() {
        return RegenerateSchedulesResponse.builder()
                .orderId(1L)
                .schedules(Arrays.asList(
                        RegenerateSchedulesResponse.ScheduleInfo.builder()
                                .id(10L)
                                .scheduledTime("08:00:00")
                                .isActive(true)
                                .build()
                ))
                .build();
    }

    // ==================== PHẦN 4: Entity Builders ====================

    public static List<MedicationOrder> createMultipleMedicationOrders(Resident resident, User prescriber, int count) {
        List<MedicationOrder> orders = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            orders.add(MedicationOrder.builder()
                    .id((long) i)
                    .drugName("Medication " + i)
                    .dosage((i * 10) + " mg")
                    .route("ORAL")
                    .frequency("Once Daily")
                    .status(MedicationStatus.ACTIVE)
                    .resident(resident)
                    .prescribedBy(prescriber)
                    .isControlledSubstance(i % 2 == 0)
                    .isDeleted(false)
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build());
        }
        return orders;
    }

    public static List<MedicationSchedule> createMultipleMedicationSchedules(MedicationOrder order, int count) {
        List<MedicationSchedule> schedules = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            schedules.add(MedicationSchedule.builder()
                    .id((long) i)
                    .order(order)
                    .scheduledTime(LocalTime.of(8 + i, 0))
                    .isActive(true)
                    .build());
        }
        return schedules;
    }

    public static List<MedicationSchedule> createMedicationSchedulesForBatch(List<MedicationOrder> orders) {
        List<MedicationSchedule> schedules = new ArrayList<>();
        for (MedicationOrder order : orders) {
            schedules.add(MedicationSchedule.builder()
                    .id(order.getId())
                    .order(order)
                    .scheduledTime(LocalTime.of(8, 0))
                    .isActive(true)
                    .build());
        }
        return schedules;
    }

    public static GetMedicationOrdersRequest createGetMedicationOrdersRequest(Long residentId,
                                                                              String status,
                                                                              String search,
                                                                              Integer page,
                                                                              Integer limit) {
        return GetMedicationOrdersRequest.builder()
                .residentId(residentId)
                .status(status)
                .search(search)
                .page(page != null ? page : 1)
                .limit(limit != null ? limit : 20)
                .build();
    }

    public static CreateMedicationOrderRequest createCreateMedicationOrderRequest(Long residentId,
                                                                                  String drugName,
                                                                                  String dosage,
                                                                                  String route,
                                                                                  String frequency,
                                                                                  Boolean isControlled,
                                                                                  Long prescribedBy,
                                                                                  String prescriberNotes,
                                                                                  List<String> scheduledTimes) {
        return CreateMedicationOrderRequest.builder()
                .residentId(residentId)
                .drugName(drugName != null ? drugName : "Lisinopril")
                .dosage(dosage != null ? dosage : "10 mg")
                .route(route != null ? route : "ORAL")
                .frequency(frequency != null ? frequency : "Once daily")
                .isControlledSubstance(isControlled != null ? isControlled : false)
                .prescribedBy(prescribedBy)
                .prescriberNotes(prescriberNotes != null ? prescriberNotes : "Monitor blood pressure daily")
                .scheduledTimes(scheduledTimes != null ? scheduledTimes : Arrays.asList("08:00:00"))
                .build();
    }

    public static DiscontinueMedicationOrderRequest createDiscontinueMedicationOrderRequest(String reason) {
        return DiscontinueMedicationOrderRequest.builder()
                .discontinueReason(reason != null ? reason : "Switched to different medication")
                .build();
    }

    // ==================== PHẦN 3: Response Builders ====================

    public static MedicationOrderResponse createMedicationOrderResponse() {
        return MedicationOrderResponse.builder()
                .id(1L)
                .resident(MedicationOrderResponse.ResidentInfo.builder()
                        .id(1L)
                        .displayName("John Smith")
                        .roomNumber("208")
                        .bedNumber("B")
                        .build())
                .drugName("Aspirin")
                .dosage("100 mg")
                .route("ORAL")
                .frequency("Every 8 hours")
                .isControlledSubstance(false)
                .status(MedicationStatus.ACTIVE)
                .prescribedBy(MedicationOrderResponse.PrescriberInfo.builder()
                        .id(4L)
                        .displayName("Dr. Brown")
                        .licenseNumber("MD12345")
                        .build())
                .lastAdministeredAt(OffsetDateTime.now().toString())
                .lastAdministeredBy("Nurse Jane Doe (RN)")
                .allergyConflict(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    public static MedicationOrderResponse createMedicationOrderResponseWithControlled() {
        return MedicationOrderResponse.builder()
                .id(2L)
                .resident(MedicationOrderResponse.ResidentInfo.builder()
                        .id(2L)
                        .displayName("Mary Brown")
                        .roomNumber("118")
                        .bedNumber("A")
                        .build())
                .drugName("Morphine")
                .dosage("5 mg")
                .route("ORAL")
                .frequency("Every 6 hours")
                .isControlledSubstance(true)
                .status(MedicationStatus.ACTIVE)
                .prescribedBy(MedicationOrderResponse.PrescriberInfo.builder()
                        .id(5L)
                        .displayName("Dr. Anderson")
                        .licenseNumber("MD67890")
                        .build())
                .lastAdministeredAt(null)
                .lastAdministeredBy(null)
                .allergyConflict(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    public static List<MedicationOrderResponse> createMedicationOrderResponseList() {
        return Arrays.asList(
                createMedicationOrderResponse(),
                createMedicationOrderResponseWithControlled()
        );
    }

    public static MedicationOrderListResponse createMedicationOrderListResponse() {
        return MedicationOrderListResponse.builder()
                .medicationOrders(createMedicationOrderResponseList())
                .metadata(MedicationOrderListResponse.PaginationMetadata.builder()
                        .currentPage(1)
                        .totalPage(2)
                        .currentLimit(20)
                        .hasNext(true)
                        .hasPrevious(false)
                        .build())
                .build();
    }

    public static MedicationOrderDetailResponse createMedicationOrderDetailResponse() {
        return MedicationOrderDetailResponse.builder()
                .id(1L)
                .resident(MedicationOrderDetailResponse.ResidentInfo.builder()
                        .id(1L)
                        .displayName("John Smith")
                        .roomNumber("208")
                        .bedNumber("B")
                        .build())
                .drugName("Aspirin")
                .dosage("100 mg")
                .route("ORAL")
                .frequency("Every 8 hours")
                .isControlledSubstance(false)
                .status(MedicationStatus.ACTIVE)
                .prescribedBy(MedicationOrderDetailResponse.PrescriberInfo.builder()
                        .id(4L)
                        .displayName("Dr. Brown")
                        .licenseNumber("MD12345")
                        .build())
                .prescriberNotes("Take with food if patient complains of GI upset")
                .allergyConflict(false)
                .schedules(createScheduleInfoList())
                .recentLogs(createRecentLogsList())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    public static List<MedicationOrderDetailResponse.ScheduleInfo> createScheduleInfoList() {
        return Arrays.asList(
                MedicationOrderDetailResponse.ScheduleInfo.builder()
                        .id(1L)
                        .scheduledTime("08:00:00")
                        .isActive(true)
                        .build(),
                MedicationOrderDetailResponse.ScheduleInfo.builder()
                        .id(2L)
                        .scheduledTime("16:00:00")
                        .isActive(true)
                        .build()
        );
    }

    public static List<MedicationOrderDetailResponse.RecentLog> createRecentLogsList() {
        return Arrays.asList(
                MedicationOrderDetailResponse.RecentLog.builder()
                        .logId(123456L)
                        .scheduleId(1L)
                        .status(MedicationStatus.ADMINISTERED)
                        .administeredBy(MedicationOrderDetailResponse.RecentLog.AdminBy.builder()
                                .id(7L)
                                .displayName("Nurse Jane Doe (RN)")
                                .build())
                        .loggedAt(OffsetDateTime.now().toString())
                        .build()
        );
    }

    public static CreateMedicationOrderResponse createCreateMedicationOrderResponse() {
        return CreateMedicationOrderResponse.builder()
                .id(25L)
                .residentId(1L)
                .drugName("Lisinopril")
                .dosage("10 mg")
                .route("ORAL")
                .frequency("Once daily")
                .isControlledSubstance(false)
                .status(MedicationStatus.ACTIVE)
                .prescribedBy(4L)
                .schedules(Arrays.asList(
                        CreateMedicationOrderResponse.ScheduleInfo.builder()
                                .id(33L)
                                .scheduledTime("08:00:00")
                                .isActive(true)
                                .build()
                ))
                .createdAt(OffsetDateTime.now())
                .build();
    }

    public static DiscontinueMedicationOrderResponse createDiscontinueMedicationOrderResponse() {
        return DiscontinueMedicationOrderResponse.builder()
                .id(1L)
                .status(MedicationStatus.DISCONTINUED)
                .discontinuedAt(OffsetDateTime.now())
                .discontinuedBy(DiscontinueMedicationOrderResponse.DiscontinuedBy.builder()
                        .id(4L)
                        .displayName("Dr. Brown")
                        .build())
                .discontinueReason("Switched to different medication")
                .build();
    }

    public static PendingMedicationResponse createPendingMedicationResponse() {
        return PendingMedicationResponse.builder()
                .residentId(1L)
                .pendingMedications(Arrays.asList(
                        PendingMedicationResponse.PendingMedication.builder()
                                .orderId(1L)
                                .scheduleId(1L)
                                .drugName("Aspirin")
                                .scheduledTime("08:00:00")
                                .timeWindowStart("07:30:00")
                                .timeWindowEnd("08:30:00")
                                .isOverdue(false)
                                .minutesUntilDue(15)
                                .requiresWitness(false)
                                .build(),
                        PendingMedicationResponse.PendingMedication.builder()
                                .orderId(2L)
                                .scheduleId(2L)
                                .drugName("Lisinopril")
                                .scheduledTime("16:00:00")
                                .timeWindowStart("15:30:00")
                                .timeWindowEnd("16:30:00")
                                .isOverdue(false)
                                .minutesUntilDue(30)
                                .requiresWitness(false)
                                .build()
                ))
                .build();
    }

    public static PendingMedicationResponse createEmptyPendingMedicationResponse() {
        return PendingMedicationResponse.builder()
                .residentId(1L)
                .pendingMedications(Collections.emptyList())
                .build();
    }

    // ==================== PHẦN 3: Entity Builders ====================

    public static MedicationOrder createMedicationOrderEntity(Long id, Resident resident, User prescriber,
                                                              String status, Boolean isControlled,
                                                              String drugName, String dosage,
                                                              String route, String frequency) {
        return MedicationOrder.builder()
                .id(id)
                .drugName(drugName != null ? drugName : "Medication " + id)
                .dosage(dosage != null ? dosage : "100 mg")
                .route(route != null ? route : "ORAL")
                .frequency(frequency != null ? frequency : "Once Daily")
                .status(status != null ? status : MedicationStatus.ACTIVE)
                .resident(resident)
                .prescribedBy(prescriber)
                .isControlledSubstance(isControlled != null ? isControlled : false)
                .isDeleted(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    public static MedicationOrder createDiscontinuedMedicationOrder(Long id, Resident resident, User prescriber) {
        return MedicationOrder.builder()
                .id(id)
                .drugName("Discontinued Drug")
                .dosage("50 mg")
                .route("ORAL")
                .frequency("Once Daily")
                .status(MedicationStatus.DISCONTINUED)
                .resident(resident)
                .prescribedBy(prescriber)
                .isControlledSubstance(false)
                .isDeleted(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    public static Resident createResident(Long id) {
        Facility facility = Facility.builder()
                .id(1L)
                .name("Test Facility")
                .build();

        Room room = Room.builder()
                .id(id)
                .roomNumber("20" + id)
                .facility(facility)
                .build();

        Bed bed = Bed.builder()
                .id(id)
                .bedNumber(String.valueOf((char) ('A' + id - 1)))
                .room(room)
                .status("OCCUPIED")
                .build();

        return Resident.builder()
                .id(id)
                .firstName("Resident" + id)
                .lastName("Test")
                .dateOfBirth(LocalDate.of(1940 + (int)(id % 30), 1, 1))
                .status(MedicationStatus.ACTIVE)
                .bed(bed)
                .isDeleted(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    public static Resident createResidentWithAllergies(Long id, List<String> allergies) {
        Resident resident = createResident(id);
        // Allergies would be stored in a separate table in real implementation
        return resident;
    }

    public static User createUser(Long id, String roleName) {
        Role role = Role.builder()
                .id(id)
                .roleName(roleName)
                .build();

        return User.builder()
                .id(id)
                .firstName("User" + id)
                .lastName("Test")
                .email("user" + id + "@test.com")
                .role(role)
                .status("ACTIVE")
                .licenseNumber("LIC" + id)
                .build();
    }

    public static MedicationOrder createMedicationOrder(Long id, Resident resident, User prescriber,
                                                        String status, Boolean isControlled) {
        return MedicationOrder.builder()
                .id(id)
                .drugName("Medication " + id)
                .dosage("100 mg")
                .route("ORAL")
                .frequency("Once Daily")
                .status(status != null ? status : MedicationStatus.ACTIVE)
                .resident(resident)
                .prescribedBy(prescriber)
                .isControlledSubstance(isControlled != null ? isControlled : false)
                .isDeleted(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    public static MedicationOrder createControlledSubstanceOrder(Long id, Resident resident, User prescriber) {
        return MedicationOrder.builder()
                .id(id)
                .drugName("Morphine")
                .dosage("5 mg")
                .route("ORAL")
                .frequency("Every 6 hours")
                .status(MedicationStatus.ACTIVE)
                .resident(resident)
                .prescribedBy(prescriber)
                .isControlledSubstance(true)
                .isDeleted(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    public static MedicationSchedule createMedicationSchedule(Long id, MedicationOrder order,
                                                              LocalTime time, Boolean isActive) {
        return MedicationSchedule.builder()
                .id(id)
                .order(order)
                .scheduledTime(time != null ? time : LocalTime.of(8, 0))
                .isActive(isActive != null ? isActive : true)
                .build();
    }

    public static MedicationLog createMedicationLog(Long id, MedicationOrder order, User admin,
                                                    String status, OffsetDateTime loggedAt) {
        return MedicationLog.builder()
                .id(id)
                .status(status != null ? status : MedicationStatus.ADMINISTERED)
                .order(order)
                .administeredBy(admin)
                .witnessedBy(null)
                .loggedAt(loggedAt != null ? loggedAt : OffsetDateTime.now())
                .isClinicallyJustified(true)
                .overrideReason(status != null && status.equals(MedicationStatus.ADMINISTERED) ? null : "Override reason")
                .build();
    }

    public static MedicationLog createMedicationLogWithWitness(Long id, MedicationOrder order, User admin,
                                                               User witness, String status, OffsetDateTime loggedAt) {
        return MedicationLog.builder()
                .id(id)
                .status(status != null ? status : MedicationStatus.ADMINISTERED)
                .order(order)
                .administeredBy(admin)
                .witnessedBy(witness)
                .loggedAt(loggedAt != null ? loggedAt : OffsetDateTime.now())
                .isClinicallyJustified(true)
                .overrideReason(status != null && status.equals(MedicationStatus.ADMINISTERED) ? null : "Override reason")
                .build();
    }

    // ==================== PHẦN 1: Response Builders ====================

    public static MarDashboardResponse createDashboardResponse() {
        return MarDashboardResponse.builder()
                .shift(MedicationStatus.DAY)
                .date(LocalDate.now().toString())
                .summary(MarDashboardResponse.DashboardSummary.builder()
                        .pending(10)
                        .completed(25)
                        .overdue(3)
                        .held(1)
                        .refused(1)
                        .notAvailable(1)
                        .build())
                .globalAllergyAlerts(createAllergyAlerts())
                .medPassList(createMedPassList())
                .shiftHandoffNotes("Test handoff notes")
                .build();
    }

    public static List<MarDashboardResponse.AllergyAlert> createAllergyAlerts() {
        return Arrays.asList(
                MarDashboardResponse.AllergyAlert.builder()
                        .residentId(1L)
                        .residentName("John Smith")
                        .allergy("Penicillin")
                        .unconfirmed(true)
                        .build()
        );
    }

    public static List<MarDashboardResponse.MedPassItem> createMedPassList() {
        return Arrays.asList(
                MarDashboardResponse.MedPassItem.builder()
                        .residentId(1L)
                        .residentName("John Smith")
                        .roomNumber("208")
                        .bedNumber("B")
                        .status(MedicationStatus.OVERDUE)
                        .nextMedication(MarDashboardResponse.NextMedication.builder()
                                .orderId(1L)
                                .drugName("Aspirin")
                                .scheduledTime("08:00:00")
                                .timeWindowStart("07:30:00")
                                .timeWindowEnd("08:30:00")
                                .build())
                        .hasUnconfirmedAllergy(false)
                        .build(),
                MarDashboardResponse.MedPassItem.builder()
                        .residentId(2L)
                        .residentName("Mary Brown")
                        .roomNumber("118")
                        .bedNumber("A")
                        .status(MedicationStatus.DUE_SOON)
                        .nextMedication(MarDashboardResponse.NextMedication.builder()
                                .orderId(2L)
                                .drugName("Insulin")
                                .scheduledTime("09:00:00")
                                .timeWindowStart("08:30:00")
                                .timeWindowEnd("09:30:00")
                                .build())
                        .hasUnconfirmedAllergy(false)
                        .build()
        );
    }

    public static MarResidentResponse createMarResidentResponse() {
        return MarResidentResponse.builder()
                .resident(MarResidentResponse.ResidentInfo.builder()
                        .id(1L)
                        .fullName("John Smith")
                        .roomNumber("208")
                        .bedNumber("B")
                        .build())
                .dateRange(MarResidentResponse.DateRange.builder()
                        .start(LocalDate.now().minusDays(7).toString())
                        .end(LocalDate.now().toString())
                        .build())
                .summaryStats(MarResidentResponse.SummaryStats.builder()
                        .totalScheduled(21)
                        .administered(18)
                        .held(1)
                        .refused(1)
                        .notAvailable(0)
                        .overrides(1)
                        .build())
                .medicationGrid(createMedicationGrid())
                .build();
    }

    public static List<MarResidentResponse.MedicationGrid> createMedicationGrid() {
        return Arrays.asList(
                MarResidentResponse.MedicationGrid.builder()
                        .orderId(1L)
                        .drugName("Aspirin")
                        .dosage("100 mg")
                        .route("ORAL")
                        .frequency("Every 8 hours")
                        .days(createDays())
                        .build()
        );
    }

    public static List<MarResidentResponse.DayDetail> createDays() {
        return Arrays.asList(
                MarResidentResponse.DayDetail.builder()
                        .date(LocalDate.now().toString())
                        .scheduleId(1L)
                        .scheduledTime("08:00:00")
                        .status(MedicationStatus.ADMINISTERED)
                        .administeredBy(MarResidentResponse.AdminBy.builder()
                                .id(7L)
                                .displayName("Nurse Jane Doe (RN)")
                                .build())
                        .witnessedBy(null)
                        .loggedAt(OffsetDateTime.now().toString())
                        .overrideReason(null)
                        .build(),
                MarResidentResponse.DayDetail.builder()
                        .date(LocalDate.now().toString())
                        .scheduleId(2L)
                        .scheduledTime("16:00:00")
                        .status(MedicationStatus.SCHEDULED)
                        .administeredBy(null)
                        .witnessedBy(null)
                        .loggedAt(null)
                        .overrideReason(null)
                        .build()
        );
    }

    public static MarScheduleShiftResponse createShiftScheduleResponse(String shift) {
        return MarScheduleShiftResponse.builder()
                .shift(shift)
                .date(LocalDate.now().toString())
                .schedule(createShiftScheduleItems())
                .build();
    }

    public static List<MarScheduleShiftResponse.ShiftScheduleItem> createShiftScheduleItems() {
        return Arrays.asList(
                MarScheduleShiftResponse.ShiftScheduleItem.builder()
                        .residentId(1L)
                        .residentName("John Smith")
                        .roomNumber("208")
                        .medications(createShiftMedications())
                        .build()
        );
    }

    public static List<MarScheduleShiftResponse.ShiftMedication> createShiftMedications() {
        return Arrays.asList(
                MarScheduleShiftResponse.ShiftMedication.builder()
                        .orderId(1L)
                        .scheduleId(1L)
                        .drugName("Aspirin")
                        .dosage("100 mg")
                        .scheduledTime("08:00:00")
                        .isActive(true)
                        .status(MedicationStatus.SCHEDULED)
                        .build()
        );
    }

    public static MarDashboardResponse createEmptyDashboardResponse() {
        return MarDashboardResponse.builder()
                .shift(MedicationStatus.DAY)
                .date(LocalDate.now().toString())
                .summary(MarDashboardResponse.DashboardSummary.builder()
                        .pending(0)
                        .completed(0)
                        .overdue(0)
                        .held(0)
                        .refused(0)
                        .notAvailable(0)
                        .build())
                .globalAllergyAlerts(Collections.emptyList())
                .medPassList(Collections.emptyList())
                .shiftHandoffNotes("")
                .build();
    }

    public static MarScheduleShiftResponse createEmptyShiftScheduleResponse(String shift) {
        return MarScheduleShiftResponse.builder()
                .shift(shift)
                .date(LocalDate.now().toString())
                .schedule(Collections.emptyList())
                .build();
    }

    // ==================== PHẦN 2: Request Builders ====================

    public static StartMedPassRequest createStartMedPassRequest(Long residentId) {
        return StartMedPassRequest.builder()
                .residentId(residentId)
                .build();
    }

    public static ScanBarcodeRequest createScanBarcodeRequest(String sessionId, Long residentId,
                                                              Long orderId, Long scheduleId,
                                                              String barcodeData, String scanMethod) {
        return ScanBarcodeRequest.builder()
                .sessionId(sessionId)
                .residentId(residentId)
                .orderId(orderId)
                .scheduleId(scheduleId)
                .barcodeData(barcodeData)
                .scanMethod(scanMethod)
                .build();
    }

    public static AdministerMedicationRequest createAdministerMedicationRequest(String sessionId,
                                                                                Long orderId,
                                                                                Long scheduleId,
                                                                                Long witnessedBy,
                                                                                String notes) {
        return AdministerMedicationRequest.builder()
                .sessionId(sessionId)
                .orderId(orderId)
                .scheduleId(scheduleId)
                .witnessedBy(witnessedBy)
                .notes(notes)
                .build();
    }

    public static OverrideVerificationRequest createOverrideVerificationRequest(String sessionId,
                                                                                Long orderId,
                                                                                Long scheduleId,
                                                                                String overrideReason,
                                                                                String otherReasonText,
                                                                                Boolean confirmClinicallyJustified,
                                                                                Long witnessedBy,
                                                                                String notes) {
        return OverrideVerificationRequest.builder()
                .sessionId(sessionId)
                .orderId(orderId)
                .scheduleId(scheduleId)
                .overrideReason(overrideReason)
                .otherReasonText(otherReasonText)
                .confirmClinicallyJustified(confirmClinicallyJustified)
                .witnessedBy(witnessedBy)
                .notes(notes)
                .build();
    }

    public static RefuseMedicationRequest createRefuseMedicationRequest(String sessionId,
                                                                        Long orderId,
                                                                        Long scheduleId,
                                                                        String overrideReason) {
        return RefuseMedicationRequest.builder()
                .sessionId(sessionId)
                .orderId(orderId)
                .scheduleId(scheduleId)
                .overrideReason(overrideReason)
                .build();
    }

    public static HoldMedicationRequest createHoldMedicationRequest(String sessionId,
                                                                    Long orderId,
                                                                    Long scheduleId,
                                                                    String overrideReason) {
        return HoldMedicationRequest.builder()
                .sessionId(sessionId)
                .orderId(orderId)
                .scheduleId(scheduleId)
                .overrideReason(overrideReason)
                .build();
    }

    // ==================== PHẦN 2: Response Builders ====================

    public static StartMedPassResponse createStartMedPassResponse() {
        return StartMedPassResponse.builder()
                .sessionId("MP-20260717-0001")
                .expiresAt(OffsetDateTime.now().plusMinutes(30).toString())
                .resident(StartMedPassResponse.ResidentInfo.builder()
                        .id(1L)
                        .fullName("John Smith")
                        .roomNumber("208")
                        .bedNumber("B")
                        .dateOfBirth("1945-03-12")
                        .allergies(Arrays.asList("Penicillin", "Sulfa"))
                        .allergyConfirmed(false)
                        .build())
                .pendingMedications(Arrays.asList(
                        StartMedPassResponse.PendingMedication.builder()
                                .orderId(1L)
                                .drugName("Aspirin")
                                .dosage("100 mg")
                                .route("ORAL")
                                .frequency("Every 8 hours")
                                .scheduledTime("08:00:00")
                                .isControlledSubstance(false)
                                .build()
                ))
                .build();
    }

    public static StartMedPassResponse createStartMedPassResponseWithControlledSubstance() {
        return StartMedPassResponse.builder()
                .sessionId("MP-20260717-0002")
                .expiresAt(OffsetDateTime.now().plusMinutes(30).toString())
                .resident(StartMedPassResponse.ResidentInfo.builder()
                        .id(2L)
                        .fullName("Mary Brown")
                        .roomNumber("118")
                        .bedNumber("A")
                        .dateOfBirth("1950-06-20")
                        .allergies(Collections.emptyList())
                        .allergyConfirmed(false)
                        .build())
                .pendingMedications(Arrays.asList(
                        StartMedPassResponse.PendingMedication.builder()
                                .orderId(2L)
                                .drugName("Morphine")
                                .dosage("5 mg")
                                .route("ORAL")
                                .frequency("Every 6 hours")
                                .scheduledTime("08:00:00")
                                .isControlledSubstance(true)
                                .build()
                ))
                .build();
    }

    public static StartMedPassResponse createStartMedPassResponseWithMultipleMedications() {
        return StartMedPassResponse.builder()
                .sessionId("MP-20260717-0003")
                .expiresAt(OffsetDateTime.now().plusMinutes(30).toString())
                .resident(StartMedPassResponse.ResidentInfo.builder()
                        .id(3L)
                        .fullName("Robert Johnson")
                        .roomNumber("305")
                        .bedNumber("C")
                        .dateOfBirth("1938-08-25")
                        .allergies(Arrays.asList("Sulfa"))
                        .allergyConfirmed(false)
                        .build())
                .pendingMedications(Arrays.asList(
                        StartMedPassResponse.PendingMedication.builder()
                                .orderId(3L)
                                .drugName("Lisinopril")
                                .dosage("10 mg")
                                .route("ORAL")
                                .frequency("Once Daily")
                                .scheduledTime("08:00:00")
                                .isControlledSubstance(false)
                                .build(),
                        StartMedPassResponse.PendingMedication.builder()
                                .orderId(4L)
                                .drugName("Metformin")
                                .dosage("500 mg")
                                .route("ORAL")
                                .frequency("Twice Daily")
                                .scheduledTime("08:00:00")
                                .isControlledSubstance(false)
                                .build()
                ))
                .build();
    }

    public static ScanBarcodeResponse createScanBarcodeResponse(boolean success) {
        if (success) {
            return ScanBarcodeResponse.builder()
                    .verificationStatus("MATCHED")
                    .fiveRights(createFiveRights(true))
                    .canAdminister(true)
                    .requiresOverride(false)
                    .requiresWitness(false)
                    .overrideReasons(Collections.emptyList())
                    .build();
        } else {
            return ScanBarcodeResponse.builder()
                    .verificationStatus("FAILED")
                    .fiveRights(createFiveRights(false))
                    .canAdminister(false)
                    .requiresOverride(true)
                    .requiresWitness(false)
                    .overrideReasons(Arrays.asList(
                            "BARCODE_UNREADABLE",
                            "EMERGENCY_ADMINISTRATION",
                            "TIME_WINDOW_EXCEPTION",
                            "PATIENT_UNAVAILABLE",
                            "UNCONFIRMED_ALLERGY",
                            "OTHER"
                    ))
                    .build();
        }
    }

    public static ScanBarcodeResponse.FiveRights createFiveRights(boolean allPassed) {
        return ScanBarcodeResponse.FiveRights.builder()
                .rightResident(ScanBarcodeResponse.RightDetail.builder()
                        .passed(true)
                        .detail("Matches resident")
                        .build())
                .rightMedication(ScanBarcodeResponse.RightDetail.builder()
                        .passed(allPassed)
                        .detail(allPassed ? "Matches order" : "Scanned: Amoxicillin, Expected: Aspirin")
                        .build())
                .rightDose(ScanBarcodeResponse.RightDetail.builder()
                        .passed(true)
                        .detail("100 mg matches")
                        .build())
                .rightRoute(ScanBarcodeResponse.RightDetail.builder()
                        .passed(true)
                        .detail("ORAL route confirmed")
                        .build())
                .rightTime(ScanBarcodeResponse.RightDetail.builder()
                        .passed(allPassed)
                        .detail(allPassed ? "Within ±30 min window" : "Outside ±30 min window (scheduled 08:00, scanned 08:45)")
                        .build())
                .build();
    }

    public static ScanBarcodeResponse createScanBarcodeResponseTimeWindowFailed() {
        return ScanBarcodeResponse.builder()
                .verificationStatus("FAILED")
                .fiveRights(ScanBarcodeResponse.FiveRights.builder()
                        .rightResident(ScanBarcodeResponse.RightDetail.builder()
                                .passed(true)
                                .detail("Matches resident")
                                .build())
                        .rightMedication(ScanBarcodeResponse.RightDetail.builder()
                                .passed(true)
                                .detail("Matches order")
                                .build())
                        .rightDose(ScanBarcodeResponse.RightDetail.builder()
                                .passed(true)
                                .detail("100 mg matches")
                                .build())
                        .rightRoute(ScanBarcodeResponse.RightDetail.builder()
                                .passed(true)
                                .detail("ORAL route confirmed")
                                .build())
                        .rightTime(ScanBarcodeResponse.RightDetail.builder()
                                .passed(false)
                                .detail("Outside ±30 min window (scheduled 08:00, scanned 08:45)")
                                .build())
                        .build())
                .canAdminister(false)
                .requiresOverride(true)
                .requiresWitness(false)
                .overrideReasons(Arrays.asList(
                        "TIME_WINDOW_EXCEPTION",
                        "OTHER"
                ))
                .build();
    }

    public static AdministerMedicationResponse createAdministerMedicationResponse() {
        return AdministerMedicationResponse.builder()
                .logId(123456L)
                .orderId(1L)
                .scheduleId(1L)
                .status(MedicationStatus.ADMINISTERED)
                .isClinicallyJustified(true)
                .administeredBy(AdministerMedicationResponse.AdminBy.builder()
                        .id(7L)
                        .displayName("Nurse Jane Doe (RN)")
                        .build())
                .witnessedBy(null)
                .loggedAt(OffsetDateTime.now().toString())
                .build();
    }

    public static AdministerMedicationResponse createAdministerMedicationResponseWithWitness() {
        return AdministerMedicationResponse.builder()
                .logId(123456L)
                .orderId(2L)
                .scheduleId(2L)
                .status(MedicationStatus.ADMINISTERED)
                .isClinicallyJustified(true)
                .administeredBy(AdministerMedicationResponse.AdminBy.builder()
                        .id(7L)
                        .displayName("Nurse Jane Doe (RN)")
                        .build())
                .witnessedBy(AdministerMedicationResponse.AdminBy.builder()
                        .id(8L)
                        .displayName("Nurse John Smith (LPN)")
                        .build())
                .loggedAt(OffsetDateTime.now().toString())
                .build();
    }

    public static OverrideVerificationResponse createOverrideVerificationResponse() {
        return OverrideVerificationResponse.builder()
                .logId(123457L)
                .orderId(1L)
                .scheduleId(1L)
                .status(MedicationStatus.ADMINISTERED)
                .isClinicallyJustified(true)
                .overrideReason("TIME_WINDOW_EXCEPTION")
                .administeredBy(OverrideVerificationResponse.AdminBy.builder()
                        .id(7L)
                        .displayName("Nurse Jane Doe (RN)")
                        .build())
                .witnessedBy(OverrideVerificationResponse.AdminBy.builder()
                        .id(8L)
                        .displayName("Nurse John Smith (LPN)")
                        .build())
                .loggedAt(OffsetDateTime.now().toString())
                .auditLogged(true)
                .build();
    }

    public static OverrideVerificationResponse createOverrideVerificationResponseOtherReason() {
        return OverrideVerificationResponse.builder()
                .logId(123457L)
                .orderId(1L)
                .scheduleId(1L)
                .status(MedicationStatus.ADMINISTERED)
                .isClinicallyJustified(true)
                .overrideReason("Patient was in critical condition")
                .administeredBy(OverrideVerificationResponse.AdminBy.builder()
                        .id(7L)
                        .displayName("Nurse Jane Doe (RN)")
                        .build())
                .witnessedBy(OverrideVerificationResponse.AdminBy.builder()
                        .id(8L)
                        .displayName("Nurse John Smith (LPN)")
                        .build())
                .loggedAt(OffsetDateTime.now().toString())
                .auditLogged(true)
                .build();
    }

    public static RefuseMedicationResponse createRefuseMedicationResponse() {
        return RefuseMedicationResponse.builder()
                .logId(123458L)
                .orderId(1L)
                .scheduleId(1L)
                .status(MedicationStatus.REFUSED)
                .overrideReason("Patient refused due to nausea")
                .administeredBy(RefuseMedicationResponse.AdminBy.builder()
                        .id(7L)
                        .displayName("Nurse Jane Doe (RN)")
                        .build())
                .loggedAt(OffsetDateTime.now().toString())
                .build();
    }

    public static RefuseMedicationResponse createRefuseMedicationResponseWithOtherReason() {
        return RefuseMedicationResponse.builder()
                .logId(123458L)
                .orderId(1L)
                .scheduleId(1L)
                .status(MedicationStatus.REFUSED)
                .overrideReason("Patient refused due to bad taste")
                .administeredBy(RefuseMedicationResponse.AdminBy.builder()
                        .id(7L)
                        .displayName("Nurse Jane Doe (RN)")
                        .build())
                .loggedAt(OffsetDateTime.now().toString())
                .build();
    }

    public static HoldMedicationResponse createHoldMedicationResponse() {
        return HoldMedicationResponse.builder()
                .logId(123459L)
                .orderId(1L)
                .scheduleId(1L)
                .status(MedicationStatus.HELD)
                .overrideReason("Patient was in therapy session")
                .administeredBy(HoldMedicationResponse.AdminBy.builder()
                        .id(7L)
                        .displayName("Nurse Jane Doe (RN)")
                        .build())
                .loggedAt(OffsetDateTime.now().toString())
                .build();
    }

    public static HoldMedicationResponse createHoldMedicationResponseWithOtherReason() {
        return HoldMedicationResponse.builder()
                .logId(123459L)
                .orderId(1L)
                .scheduleId(1L)
                .status(MedicationStatus.HELD)
                .overrideReason("Patient was sleeping")
                .administeredBy(HoldMedicationResponse.AdminBy.builder()
                        .id(7L)
                        .displayName("Nurse Jane Doe (RN)")
                        .build())
                .loggedAt(OffsetDateTime.now().toString())
                .build();
    }
}