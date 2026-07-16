package com.mockproject.group3.dto.admin.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an inventory category.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCategoryRequest {
    private String categoryName;
    private String description;
}
