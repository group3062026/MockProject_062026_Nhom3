package com.nguyenquyen.mockproject_062026_group3.dto.admin.room;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating a room.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateRoomRequest {
    private String roomNumber;
    private String roomType;
}
