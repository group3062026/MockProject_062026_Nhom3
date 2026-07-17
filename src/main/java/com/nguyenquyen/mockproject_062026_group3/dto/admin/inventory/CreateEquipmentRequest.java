package com.nguyenquyen.mockproject_062026_group3.dto.admin.inventory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating equipment.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateEquipmentRequest {

    @NotBlank(message = "Item name is required")
    private String itemName;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Asset tag is required")
    private String assetTag;

    @NotNull(message = "Facility ID is required")
    private Long facilityId;

    private BigDecimal unitValue;
}
