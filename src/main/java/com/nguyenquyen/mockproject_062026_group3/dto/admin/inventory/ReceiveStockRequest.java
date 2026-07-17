package com.nguyenquyen.mockproject_062026_group3.dto.admin.inventory;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for receiving stock (Stock Receiving).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReceiveStockRequest {

    @NotNull(message = "Quantity is required")
    private Integer quantity;

    @NotNull(message = "Received by (User ID) is required")
    private Long receivedBy;

    private String note;
}
