package com.mockproject.group3.service;

import com.mockproject.group3.dto.admin.staffing.StaffingConfigResponse;
import com.mockproject.group3.dto.admin.staffing.UpdateStaffingConfigRequest;

public interface AdminStaffingConfigService {
    StaffingConfigResponse getStaffingRatio();
    StaffingConfigResponse updateStaffingRatio(UpdateStaffingConfigRequest request);
}
