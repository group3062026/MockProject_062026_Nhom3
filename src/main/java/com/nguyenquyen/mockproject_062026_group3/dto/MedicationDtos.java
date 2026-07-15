package com.nguyenquyen.mockproject_062026_group3.dto;

import com.nguyenquyen.mockproject_062026_group3.entity.MedicationLog;
import com.nguyenquyen.mockproject_062026_group3.entity.MedicationOrder;
import com.nguyenquyen.mockproject_062026_group3.entity.MedicationSchedule;
import com.nguyenquyen.mockproject_062026_group3.entity.Resident;
import com.nguyenquyen.mockproject_062026_group3.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public class MedicationDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonSummary {
        private Long id;
        private String displayName;

        public static PersonSummary fromResident(Resident resident) {
            if (resident == null) {
                return null;
            }
            return PersonSummary.builder()
                    .id(resident.getId())
                    .displayName(displayName(resident.getFirstName(), resident.getMiddleName(), resident.getLastName()))
                    .build();
        }

        public static PersonSummary fromUser(User user) {
            if (user == null) {
                return null;
            }
            return PersonSummary.builder()
                    .id(user.getId())
                    .displayName(displayName(user.getFirstName(), user.getMiddleName(), user.getLastName()))
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicationOrderResponse {
        private Long id;
        private PersonSummary resident;
        private String drugName;
        private String dosage;
        private String route;
        private String frequency;
        private Boolean isControlledSubstance;
        private String status;
        private PersonSummary prescribedBy;
        private Boolean isDeleted;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        public static MedicationOrderResponse fromEntity(MedicationOrder order) {
            return MedicationOrderResponse.builder()
                    .id(order.getId())
                    .resident(PersonSummary.fromResident(order.getResident()))
                    .drugName(order.getDrugName())
                    .dosage(order.getDosage())
                    .route(order.getRoute())
                    .frequency(order.getFrequency())
                    .isControlledSubstance(order.getIsControlledSubstance())
                    .status(order.getStatus())
                    .prescribedBy(PersonSummary.fromUser(order.getPrescribedBy()))
                    .isDeleted(order.getIsDeleted())
                    .createdAt(order.getCreatedAt())
                    .updatedAt(order.getUpdatedAt())
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicationOrderCreateRequest {
        private Long residentId;
        private String drugName;
        private String dosage;
        private String route;
        private String frequency;
        private Boolean isControlledSubstance;
        private Long prescribedBy;
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicationOrderUpdateRequest {
        private String drugName;
        private String dosage;
        private String route;
        private String frequency;
        private Boolean isControlledSubstance;
        private String status;
        private LocalDate endDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicationScheduleResponse {
        private Long id;
        private Long orderId;
        private PersonSummary resident;
        private MedicationOrderMini medicationOrder;
        private LocalTime scheduledTime;
        private String status;
        private Boolean isActive;

        public static MedicationScheduleResponse fromEntity(MedicationSchedule schedule) {
            MedicationOrder order = schedule.getOrder();
            return MedicationScheduleResponse.builder()
                    .id(schedule.getId())
                    .orderId(order.getId())
                    .resident(PersonSummary.fromResident(order.getResident()))
                    .medicationOrder(MedicationOrderMini.fromEntity(order))
                    .scheduledTime(schedule.getScheduledTime())
                    .status(schedule.getIsActive() ? "SCHEDULED" : "INACTIVE")
                    .isActive(schedule.getIsActive())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicationOrderMini {
        private Long id;
        private String drugName;
        private String dosage;
        private String route;

        public static MedicationOrderMini fromEntity(MedicationOrder order) {
            return MedicationOrderMini.builder()
                    .id(order.getId())
                    .drugName(order.getDrugName())
                    .dosage(order.getDosage())
                    .route(order.getRoute())
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicationScheduleCreateRequest {
        private Long orderId;
        private LocalTime scheduledTime;
        private Boolean isActive;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicationScheduleUpdateRequest {
        private LocalTime scheduledTime;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodayMedicationTaskResponse {
        private Long scheduleId;
        private Long orderId;
        private Long residentId;
        private String residentName;
        private String drugName;
        private String dosage;
        private String route;
        private LocalTime scheduledTime;
        private Boolean isActive;
        private String status;

        public static TodayMedicationTaskResponse fromSchedule(MedicationSchedule schedule) {
            MedicationOrder order = schedule.getOrder();
            Resident resident = order.getResident();
            return TodayMedicationTaskResponse.builder()
                    .scheduleId(schedule.getId())
                    .orderId(order.getId())
                    .residentId(resident.getId())
                    .residentName(PersonSummary.fromResident(resident).getDisplayName())
                    .drugName(order.getDrugName())
                    .dosage(order.getDosage())
                    .route(order.getRoute())
                    .scheduledTime(schedule.getScheduledTime())
                    .isActive(schedule.getIsActive())
                    .status(schedule.getIsActive() ? "SCHEDULED" : "INACTIVE")
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicationLogResponse {
        private Long id;
        private Long orderId;
        private PersonSummary resident;
        private String drugName;
        private String status;
        private Boolean isClinicallyJustified;
        private String overrideReason;
        private PersonSummary administeredBy;
        private PersonSummary witnessedBy;
        private OffsetDateTime loggedAt;

        public static MedicationLogResponse fromEntity(MedicationLog log) {
            MedicationOrder order = log.getOrder();
            return MedicationLogResponse.builder()
                    .id(log.getId())
                    .orderId(order.getId())
                    .resident(PersonSummary.fromResident(order.getResident()))
                    .drugName(order.getDrugName())
                    .status(log.getStatus())
                    .isClinicallyJustified(log.getIsClinicallyJustified())
                    .overrideReason(log.getOverrideReason())
                    .administeredBy(PersonSummary.fromUser(log.getAdministeredBy()))
                    .witnessedBy(PersonSummary.fromUser(log.getWitnessedBy()))
                    .loggedAt(log.getLoggedAt())
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicationLogCreateRequest {
        private Long orderId;
        private String status;
        private Boolean isClinicallyJustified;
        private String overrideReason;
        private Long administeredBy;
        private Long witnessedBy;
        private OffsetDateTime loggedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicationExceptionLogRequest {
        private Long orderId;
        private String overrideReason;
        private Long administeredBy;
        private Long witnessedBy;
        private OffsetDateTime loggedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyMedicationRequest {
        private Long residentId;
        private String drugName;
        private LocalTime scheduledTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyMedicationResponse {
        private Boolean verified;
        private Long orderId;
        private Long residentId;
        private String drugName;
        private Boolean requiresOverride;
    }

    private static String displayName(String firstName, String middleName, String lastName) {
        StringBuilder builder = new StringBuilder();
        appendNamePart(builder, firstName);
        appendNamePart(builder, middleName);
        appendNamePart(builder, lastName);
        return builder.toString();
    }

    private static void appendNamePart(StringBuilder builder, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(" ");
        }
        builder.append(value.trim());
    }
}
