package com.mockproject.group3.service;

import com.mockproject.group3.dto.admin.incident.CreateIncidentSeverityRequest;
import com.mockproject.group3.dto.admin.incident.CreateSlaConfigRequest;
import com.mockproject.group3.dto.admin.incident.IncidentSeverityResponse;
import com.mockproject.group3.dto.admin.incident.SlaConfigResponse;
import com.mockproject.group3.dto.admin.incident.UpdateIncidentSeverityRequest;
import com.mockproject.group3.dto.admin.incident.UpdateSlaConfigRequest;

import java.util.List;

public interface AdminIncidentSeverityService {
    List<IncidentSeverityResponse> getIncidentSeverityLevels();
    IncidentSeverityResponse createSeverityLevel(CreateIncidentSeverityRequest request);
    IncidentSeverityResponse updateSeverityLevel(Long severityId, UpdateIncidentSeverityRequest request);
    
    List<SlaConfigResponse> getSLAConfig();
    SlaConfigResponse createSLA(CreateSlaConfigRequest request);
    SlaConfigResponse updateSLA(Long slaConfigId, UpdateSlaConfigRequest request);
}
