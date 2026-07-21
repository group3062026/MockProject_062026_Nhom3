package com.nguyenquyen.mockproject_062026_group3.dto;

import com.nguyenquyen.mockproject_062026_group3.entity.ResidentCareLevelHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CareLevelHistoryResponse {
    private Long id;
    private Long careLevelId;
    private String levelCode;
    private LocalDate startDate;
    private LocalDate endDate;

    public static CareLevelHistoryResponse fromEntity(ResidentCareLevelHistory entity) {
        if (entity == null) return null;
        return CareLevelHistoryResponse.builder()
                .id(entity.getId())
                .careLevelId(entity.getCareLevel().getId())
                .levelCode(entity.getCareLevel().getLevelCode())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .build();
    }
}
