package com.nguyenquyen.mockproject_062026_group3.dto.response;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

// SC_043, SC_044 - M7-US-03
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IncidentDetailDTO {

    private Long id;
    private String incidentType;
    private String status;
    private String description;
    private boolean automaticLockChart;
    private long slaDeadlineHours;

    private ResidentDTO resident;
    private SeverityDTO severity;
    private ReporterDTO reporter;
    private List<TimelineDTO> timelines;

    private OffsetDateTime reportedAt;
    private OffsetDateTime updatedAt;

    @Data @Builder @AllArgsConstructor @NoArgsConstructor
    public static class ResidentDTO {
        private Long id;
        private String displayName;
        private String gender;
        private BedDTO bed;
    }

    @Data @Builder @AllArgsConstructor @NoArgsConstructor
    public static class BedDTO {
        private Long id;
        private String bedNumber;
        private RoomDTO room;
        private boolean isLocked;
    }

    @Data @Builder @AllArgsConstructor @NoArgsConstructor
    public static class RoomDTO {
        private Long id;
        private String roomNumber;
        private String roomType;
    }

    @Data @Builder @AllArgsConstructor @NoArgsConstructor
    public static class SeverityDTO {
        private Long id;
        private String levelName;
        private long slaConfigured;
    }

    @Data @Builder @AllArgsConstructor @NoArgsConstructor
    public static class ReporterDTO {
        private Long id;
        private String employeeCode;
        private String displayName;
        private String email;
        private String phoneNumber;
        private String status;
    }

    @Data @Builder @AllArgsConstructor @NoArgsConstructor
    public static class TimelineDTO {
        private Long id;
        private String action;
        private String reason;
        private ActorDTO actor;
        private OffsetDateTime createdAt;
    }

    @Data @Builder @AllArgsConstructor @NoArgsConstructor
    public static class ActorDTO {
        private Long id;
        private String displayName;
    }
}
