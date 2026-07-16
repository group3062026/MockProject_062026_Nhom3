package com.mockproject.group3.dto.admin.inventory;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for durable medical equipment.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EquipmentResponse {
    private Long id;
    private String itemName;
    private String assetTag;
    private String status;
    private Long facilityId;
    private Long categoryId;
    private BigDecimal unitValue;
    private Long assignedToResident;
    private Long assignedToUser;
}
