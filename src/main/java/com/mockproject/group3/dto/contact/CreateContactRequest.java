package com.mockproject.group3.dto.contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateContactRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    private String middleName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Primary phone is required")
    @Pattern(regexp = "^1-\\d{3}-\\d{3}-\\d{4}$", message = "Phone format must be 1-XXX-XXX-XXXX")
    private String phonePrimary;

    @Pattern(regexp = "^1-\\d{3}-\\d{3}-\\d{4}$", message = "Phone format must be 1-XXX-XXX-XXXX")
    private String phoneSecondary;

    @Email(message = "Invalid email format")
    private String email;

    private Long addressId;
}

