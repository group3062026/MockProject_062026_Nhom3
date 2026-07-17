package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.dto.admin.facility.FacilityResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.facility.UpdateFacilityRequest;

public interface AdminFacilityService {
    FacilityResponse getFacilityInfo();
    FacilityResponse updateFacilityInfo(UpdateFacilityRequest request);
}
