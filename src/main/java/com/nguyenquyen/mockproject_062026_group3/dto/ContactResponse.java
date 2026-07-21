package com.nguyenquyen.mockproject_062026_group3.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nguyenquyen.mockproject_062026_group3.entity.Contact;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContactResponse {
    private Long id;
    private String firstName;
    private String middleName;
    private String lastName;
    private String phonePrimary;
    private String phoneSecondary;
    private String email;
    private Long addressId;
    private Boolean isDeleted;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static ContactResponse fromEntity(Contact entity) {
        if (entity == null) return null;
        return ContactResponse.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .middleName(entity.getMiddleName())
                .lastName(entity.getLastName())
                .phonePrimary(entity.getPhonePrimary())
                .phoneSecondary(entity.getPhoneSecondary())
                .email(entity.getEmail())
                .addressId(entity.getAddress() != null ? entity.getAddress().getId() : null)
                .isDeleted(entity.getIsDeleted())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
