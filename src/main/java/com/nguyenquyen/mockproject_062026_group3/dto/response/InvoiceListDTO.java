package com.nguyenquyen.mockproject_062026_group3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO cho Invoice trong danh sách (list view)
 * sc-035
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceListDTO {
    
    private Long id;
    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private String status;  // DRAFT, SENT, PARTIALLY_PAID, PAID, OVERDUE, VOID
    private LocalDate dueDate;
    
    // Chỉ báo thời hạn
    private Boolean isOverdue;
    private Integer daysOverdue;  // Null nếu không quá hạn
}

