package com.mockproject.group3.dto.contact;

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
}

