package com.nguyenquyen.mockproject_062026_group3.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CareLevelHistoryTransitionRequest {
    private Long careLevelId;
    private LocalDate startDate;
}
