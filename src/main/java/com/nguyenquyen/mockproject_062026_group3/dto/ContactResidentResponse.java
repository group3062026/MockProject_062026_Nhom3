package com.nguyenquyen.mockproject_062026_group3.dto;

import com.nguyenquyen.mockproject_062026_group3.entity.ResidentContact;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContactResidentResponse {
    private Long residentId;
    private String firstName;
    private String lastName;
    private String relationshipType;

    public static ContactResidentResponse fromEntity(ResidentContact entity) {
        if (entity == null) return null;
        return ContactResidentResponse.builder()
                .residentId(entity.getResident().getId())
                .firstName(entity.getResident().getFirstName())
                .lastName(entity.getResident().getLastName())
                .relationshipType(entity.getRelationshipType())
                .build();
    }
}
