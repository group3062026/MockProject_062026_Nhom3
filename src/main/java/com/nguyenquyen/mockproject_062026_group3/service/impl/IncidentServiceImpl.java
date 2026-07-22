package com.nguyenquyen.mockproject_062026_group3.service.impl;

import com.nguyenquyen.mockproject_062026_group3.common.SecurityUtils;
import com.nguyenquyen.mockproject_062026_group3.dto.request.CreateTimelineRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.UnlockIncidentRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.response.IncidentDetailDTO;
import com.nguyenquyen.mockproject_062026_group3.entity.*;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.repository.IncidentRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.IncidentTimelineRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.ResidentRepository;
import com.nguyenquyen.mockproject_062026_group3.service.IncidentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

// SC_043, SC_044 - M7-US-03
@Service
@RequiredArgsConstructor
@Slf4j
public class IncidentServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentTimelineRepository timelineRepository;
    private final ResidentRepository residentRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public IncidentDetailDTO getIncidentDetail(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INCIDENT_NOT_FOUND));

        List<IncidentTimeline> timelines = timelineRepository
                .findByIncidentIdOrderByCreatedAtAsc(id);

        return mapToDTO(incident, timelines);
    }

    @Override
    @Transactional
    public void unlockIncident(Long id, UnlockIncidentRequest request) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INCIDENT_NOT_FOUND));

        if (request.getReason() == null || request.getReason().isBlank()) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }

        // Unlock chart trên resident
        Resident resident = incident.getResident();
        resident.setIsChartLocked(false);
        residentRepository.save(resident);

        // Chuyển status sang RESOLVED
        incident.setStatus("RESOLVED");
        incidentRepository.save(incident);

        // Ghi timeline
        User currentUser = securityUtils.getCurrentUser();
        IncidentTimeline timeline = IncidentTimeline.builder()
                .incident(incident)
                .action("Chart unlocked and incident resolved")
                .reason(request.getReason())
                .actor(currentUser)
                .createdAt(OffsetDateTime.now())
                .build();
        timelineRepository.save(timeline);

        log.info("Incident {} resolved by user {}", id, currentUser.getId());
    }

    @Override
    @Transactional
    public IncidentDetailDTO.TimelineDTO addTimeline(Long id, CreateTimelineRequest request) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INCIDENT_NOT_FOUND));

        if (request.getAction() == null || request.getAction().isBlank()) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }

        User currentUser = securityUtils.getCurrentUser();
        IncidentTimeline timeline = IncidentTimeline.builder()
                .incident(incident)
                .action(request.getAction())
                .reason(request.getReason())
                .actor(currentUser)
                .createdAt(OffsetDateTime.now())
                .build();

        IncidentTimeline saved = timelineRepository.save(timeline);

        return IncidentDetailDTO.TimelineDTO.builder()
                .id(saved.getId())
                .action(saved.getAction())
                .reason(saved.getReason())
                .actor(IncidentDetailDTO.ActorDTO.builder()
                        .id(currentUser.getId())
                        .displayName(currentUser.getFirstName() + " " + currentUser.getLastName())
                        .build())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    // ── mapper ────────────────────────────────────────────────────────────────

    private IncidentDetailDTO mapToDTO(Incident incident, List<IncidentTimeline> timelines) {
        Resident resident = incident.getResident();
        IncidentSeverity severity = incident.getSeverity();
        User reporter = incident.getReportedBy();

        // Tính slaDeadlineHours còn lại
        long slaHoursRemaining = ChronoUnit.HOURS.between(
                OffsetDateTime.now(), incident.getSlaDeadline());

        // Map bed/room nếu có
        IncidentDetailDTO.BedDTO bedDTO = null;
        if (resident.getBed() != null) {
            Bed bed = resident.getBed();
            Room room = bed.getRoom();
            bedDTO = IncidentDetailDTO.BedDTO.builder()
                    .id(bed.getId())
                    .bedNumber(bed.getBedNumber())
                    .isLocked(resident.getIsChartLocked())
                    .room(IncidentDetailDTO.RoomDTO.builder()
                            .id(room.getId())
                            .roomNumber(room.getRoomNumber())
                            .roomType(room.getRoomType())
                            .build())
                    .build();
        }

        List<IncidentDetailDTO.TimelineDTO> timelineDTOs = timelines.stream()
                .map(t -> IncidentDetailDTO.TimelineDTO.builder()
                        .id(t.getId())
                        .action(t.getAction())
                        .reason(t.getReason())
                        .actor(t.getActor() != null
                                ? IncidentDetailDTO.ActorDTO.builder()
                                        .id(t.getActor().getId())
                                        .displayName(t.getActor().getFirstName() + " " + t.getActor().getLastName())
                                        .build()
                                : null)
                        .createdAt(t.getCreatedAt())
                        .build())
                .toList();

        return IncidentDetailDTO.builder()
                .id(incident.getId())
                .incidentType(incident.getIncidentType())
                .status(incident.getStatus())
                .description(incident.getDescription())
                .automaticLockChart(severity.getChartLockTrigger())
                .slaDeadlineHours(Math.max(slaHoursRemaining, 0))
                .resident(IncidentDetailDTO.ResidentDTO.builder()
                        .id(resident.getId())
                        .displayName(resident.getFirstName() + " " + resident.getLastName())
                        .gender(resident.getGender())
                        .bed(bedDTO)
                        .build())
                .severity(IncidentDetailDTO.SeverityDTO.builder()
                        .id(severity.getId())
                        .levelName(severity.getLevelName())
                        .slaConfigured(24)
                        .build())
                .reporter(IncidentDetailDTO.ReporterDTO.builder()
                        .id(reporter.getId())
                        .employeeCode(reporter.getEmployeeCode())
                        .displayName(reporter.getFirstName() + " " + reporter.getLastName())
                        .email(reporter.getEmail())
                        .phoneNumber(reporter.getPhoneNumber())
                        .status(reporter.getStatus())
                        .build())
                .timelines(timelineDTOs)
                .reportedAt(incident.getReportedAt())
                .build();
    }
}
