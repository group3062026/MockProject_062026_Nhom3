package com.mockproject.group3.dto.admin.inventory;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating an inventory category.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    private String categoryName;

    private String description;
}
