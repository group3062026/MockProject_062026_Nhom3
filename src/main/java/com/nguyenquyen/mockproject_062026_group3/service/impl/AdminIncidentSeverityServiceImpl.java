package com.nguyenquyen.mockproject_062026_group3.service.impl;

import com.nguyenquyen.mockproject_062026_group3.dto.admin.incident.CreateIncidentSeverityRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.incident.CreateSlaConfigRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.incident.IncidentSeverityResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.incident.SlaConfigResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.incident.UpdateIncidentSeverityRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.incident.UpdateSlaConfigRequest;
import com.nguyenquyen.mockproject_062026_group3.entity.IncidentSeverity;
import com.nguyenquyen.mockproject_062026_group3.entity.SlaConfig;
import com.nguyenquyen.mockproject_062026_group3.repository.IncidentSeverityRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.SlaConfigRepository;
import com.nguyenquyen.mockproject_062026_group3.service.AdminIncidentSeverityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminIncidentSeverityServiceImpl implements AdminIncidentSeverityService {

    private final IncidentSeverityRepository incidentSeverityRepository;
    private final SlaConfigRepository slaConfigRepository;

    @Override
    @Transactional(readOnly = true)
    public List<IncidentSeverityResponse> getIncidentSeverityLevels() {
        return incidentSeverityRepository.findAll().stream()
                .map(this::mapToSeverityResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public IncidentSeverityResponse createSeverityLevel(CreateIncidentSeverityRequest request) {
        if (incidentSeverityRepository.findByLevelName(request.getLevelName()).isPresent()) {
            throw new RuntimeException("Level name already exists");
        }

        IncidentSeverity severity = IncidentSeverity.builder()
                .levelName(request.getLevelName())
                .chartLockTrigger(request.getChartLockTrigger() != null ? request.getChartLockTrigger() : false)
                .build();
        
        severity = incidentSeverityRepository.save(severity);
        return mapToSeverityResponse(severity);
    }

    @Override
    @Transactional
    public IncidentSeverityResponse updateSeverityLevel(Long severityId, UpdateIncidentSeverityRequest request) {
        IncidentSeverity severity = incidentSeverityRepository.findById(severityId)
                .orElseThrow(() -> new RuntimeException("Severity level not found"));

        if (request.getLevelName() != null && !request.getLevelName().equals(severity.getLevelName())) {
            if (incidentSeverityRepository.findByLevelName(request.getLevelName()).isPresent()) {
                throw new RuntimeException("Level name already exists");
            }
            severity.setLevelName(request.getLevelName());
        }

        if (request.getChartLockTrigger() != null) {
            severity.setChartLockTrigger(request.getChartLockTrigger());
        }

        severity = incidentSeverityRepository.save(severity);
        return mapToSeverityResponse(severity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlaConfigResponse> getSLAConfig() {
        return slaConfigRepository.findAll().stream()
                .map(this::mapToSlaResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SlaConfigResponse createSLA(CreateSlaConfigRequest request) {
        IncidentSeverity severity = incidentSeverityRepository.findById(request.getSeverityId())
                .orElseThrow(() -> new RuntimeException("Severity level not found"));

        SlaConfig sla = SlaConfig.builder()
                .severity(severity)
                .slaWindowHrs(request.getSlaWindowHrs())
                .build();
        
        sla = slaConfigRepository.save(sla);
        return mapToSlaResponse(sla);
    }

    @Override
    @Transactional
    public SlaConfigResponse updateSLA(Long slaConfigId, UpdateSlaConfigRequest request) {
        SlaConfig sla = slaConfigRepository.findById(slaConfigId)
                .orElseThrow(() -> new RuntimeException("SLA config not found"));

        if (request.getSeverityId() != null) {
            IncidentSeverity severity = incidentSeverityRepository.findById(request.getSeverityId())
                    .orElseThrow(() -> new RuntimeException("Severity level not found"));
            sla.setSeverity(severity);
        }

        if (request.getSlaWindowHrs() != null) {
            sla.setSlaWindowHrs(request.getSlaWindowHrs());
        }

        sla = slaConfigRepository.save(sla);
        return mapToSlaResponse(sla);
    }

    private IncidentSeverityResponse mapToSeverityResponse(IncidentSeverity severity) {
        return IncidentSeverityResponse.builder()
                .id(severity.getId())
                .levelName(severity.getLevelName())
                .chartLockTrigger(severity.getChartLockTrigger())
                .build();
    }

    private SlaConfigResponse mapToSlaResponse(SlaConfig sla) {
        return SlaConfigResponse.builder()
                .id(sla.getId())
                .severityId(sla.getSeverity() != null ? sla.getSeverity().getId() : null)
                .slaWindowHrs(sla.getSlaWindowHrs())
                .build();
    }
}
