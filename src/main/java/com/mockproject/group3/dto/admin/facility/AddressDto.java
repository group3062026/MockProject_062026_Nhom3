package com.mockproject.group3.dto.admin.facility;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for address information embedded in facility response.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressDto {
    private Long id;
    private String streetLine1;
    private String streetLine2;
    private String city;
    private String state;
    private String zipCode;
    private String addressType;
}
