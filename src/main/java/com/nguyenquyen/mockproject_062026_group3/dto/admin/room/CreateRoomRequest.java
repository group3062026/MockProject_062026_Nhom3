package com.nguyenquyen.mockproject_062026_group3.dto.admin.room;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new room.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateRoomRequest {

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    @NotBlank(message = "Room type is required")
    private String roomType;
}
