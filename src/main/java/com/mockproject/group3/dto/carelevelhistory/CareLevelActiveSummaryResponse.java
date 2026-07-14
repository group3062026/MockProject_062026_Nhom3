package com.mockproject.group3.dto.carelevelhistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CareLevelActiveSummaryResponse {
    private String levelCode;
    private Long activeResidentCount;
}

