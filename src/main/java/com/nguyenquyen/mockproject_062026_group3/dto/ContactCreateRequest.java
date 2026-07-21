package com.nguyenquyen.mockproject_062026_group3.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactCreateRequest {
    private String firstName;
    private String middleName;
    private String lastName;
    private String phonePrimary;
    private String phoneSecondary;
    private String email;
    private Long addressId;
}
