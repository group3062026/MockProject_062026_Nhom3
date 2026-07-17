package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.dto.admin.incident.CreateIncidentSeverityRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.incident.CreateSlaConfigRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.incident.IncidentSeverityResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.incident.SlaConfigResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.incident.UpdateIncidentSeverityRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.incident.UpdateSlaConfigRequest;

import java.util.List;

public interface AdminIncidentSeverityService {
    List<IncidentSeverityResponse> getIncidentSeverityLevels();
    IncidentSeverityResponse createSeverityLevel(CreateIncidentSeverityRequest request);
    IncidentSeverityResponse updateSeverityLevel(Long severityId, UpdateIncidentSeverityRequest request);
    
    List<SlaConfigResponse> getSLAConfig();
    SlaConfigResponse createSLA(CreateSlaConfigRequest request);
    SlaConfigResponse updateSLA(Long slaConfigId, UpdateSlaConfigRequest request);
}
