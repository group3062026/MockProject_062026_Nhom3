package com.mockproject.group3.dto.admin.room;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for room.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoomResponse {
    private Long id;
    private String roomNumber;
    private String roomType;
    private Long facilityId;
    private Boolean isDeleted;
}
