package com.mockproject.group3.service;

import com.mockproject.group3.dto.admin.facility.FacilityResponse;
import com.mockproject.group3.dto.admin.facility.UpdateFacilityRequest;

public interface AdminFacilityService {
    FacilityResponse getFacilityInfo();
    FacilityResponse updateFacilityInfo(UpdateFacilityRequest request);
}
