package com.nguyenquyen.mockproject_062026_group3.dto.admin.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for updating equipment (item_name/category/unit_value only).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEquipmentRequest {
    private String itemName;
    private Long categoryId;
    private BigDecimal unitValue;
}
