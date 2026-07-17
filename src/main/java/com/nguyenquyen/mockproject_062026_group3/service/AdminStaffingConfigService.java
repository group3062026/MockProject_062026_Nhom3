package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.dto.admin.staffing.StaffingConfigResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.staffing.UpdateStaffingConfigRequest;

public interface AdminStaffingConfigService {
    StaffingConfigResponse getStaffingRatio();
    StaffingConfigResponse updateStaffingRatio(UpdateStaffingConfigRequest request);
}
