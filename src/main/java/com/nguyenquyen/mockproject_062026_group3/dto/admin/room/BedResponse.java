package com.nguyenquyen.mockproject_062026_group3.dto.admin.room;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for bed.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BedResponse {
    private Long id;
    private String bedNumber;
    private String status;
    private Long roomId;
}
