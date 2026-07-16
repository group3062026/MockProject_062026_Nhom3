package com.nguyenquyen.mockproject_062026_group3.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PhiAccessLogRequest {
    private Long residentId;
    private String accessType; // VIEW, PRINT, EXPORT, DOWNLOAD
    private LocalDate startDate;
    private LocalDate endDate;
}