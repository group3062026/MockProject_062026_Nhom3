package com.nguyenquyen.mockproject_062026_group3.dto;

import com.nguyenquyen.mockproject_062026_group3.entity.CareLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CareLevelResponse {
    private Long id;
    private String levelCode;
    private String levelName;
    private Boolean isDeleted;

    public static CareLevelResponse fromEntity(CareLevel entity) {
        if (entity == null) return null;
        return CareLevelResponse.builder()
                .id(entity.getId())
                .levelCode(entity.getLevelCode())
                .levelName(entity.getLevelName())
                .isDeleted(entity.getIsDeleted())
                .build();
    }
}
