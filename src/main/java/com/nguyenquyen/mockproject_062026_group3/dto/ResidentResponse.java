package com.nguyenquyen.mockproject_062026_group3.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nguyenquyen.mockproject_062026_group3.entity.Resident;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResidentResponse {
    private Long id;
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String maritalStatus;
    private String religionPreference;
    private String status;
    private Boolean isChartLocked;
    private Long addressId;
    private Long bedId;
    private String currentCareLevel; // Derived field
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static ResidentResponse fromEntity(Resident entity, String currentCareLevel) {
        if (entity == null) return null;
        return ResidentResponse.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .middleName(entity.getMiddleName())
                .lastName(entity.getLastName())
                .dateOfBirth(entity.getDateOfBirth())
                .gender(entity.getGender())
                .maritalStatus(entity.getMaritalStatus())
                .religionPreference(entity.getReligionPreference())
                .status(entity.getStatus())
                .isChartLocked(entity.getIsChartLocked())
                .addressId(entity.getAddress() != null ? entity.getAddress().getId() : null)
                .bedId(entity.getBed() != null ? entity.getBed().getId() : null)
                .currentCareLevel(currentCareLevel)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
