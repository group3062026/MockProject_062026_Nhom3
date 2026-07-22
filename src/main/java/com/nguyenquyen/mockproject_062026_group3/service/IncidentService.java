package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.dto.request.CreateTimelineRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.UnlockIncidentRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.response.IncidentDetailDTO;

// SC_043, SC_044 - M7-US-03
public interface IncidentService {

    IncidentDetailDTO getIncidentDetail(Long id);

    void unlockIncident(Long id, UnlockIncidentRequest request);

    IncidentDetailDTO.TimelineDTO addTimeline(Long id, CreateTimelineRequest request);
}
