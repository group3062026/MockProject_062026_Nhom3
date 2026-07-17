package com.nguyenquyen.mockproject_062026_group3.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetMedicationOrdersRequest {
    private Long residentId;
    private String status; // ACTIVE, DISCONTINUED, ON_HOLD, ALL
    private String search; // search by drug name
    private Integer page;
    private Integer limit;
}