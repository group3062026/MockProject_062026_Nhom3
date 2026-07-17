package com.nguyenquyen.mockproject_062026_group3.dto.admin.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserFacilityDto {
    private Long facilityId;
    private Boolean isPrimary;
}
