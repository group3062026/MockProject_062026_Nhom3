package com.nguyenquyen.mockproject_062026_group3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO cho chi tiết một Invoice
 * sc-035
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDetailDTO {
    
    private Long id;
    private Long residentId;
    private String residentName;
    
    // Thời kỳ tính hóa đơn
    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;
    
    // Chi phí
    private BigDecimal totalAmount;
    private BigDecimal medicareCoveredAmount;
    private BigDecimal medicaidCoveredAmount;
    private BigDecimal privateInsuranceCoveredAmount;
    private BigDecimal patientResponsibilityAmount;
    
    // Trạng thái
    private String status;  // DRAFT, SENT, PARTIALLY_PAID, PAID, OVERDUE, VOID
    private LocalDate dueDate;
    
    // Chi tiết các line items
    private List<LineItemDTO> lineItems;
    
    // Danh sách thanh toán
    private List<PaymentDTO> payments;
    
    // Thông tin bảo hiểm
    private List<InsuranceCoverageDTO> insuranceCoverages;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LineItemDTO {
        private Long id;
        private String description;
        private String itemType;  // ROOM_BOARD, CARE_LEVEL, MEDICATION, THERAPY, OTHER
        private BigDecimal amount;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaymentDTO {
        private Long id;
        private String payerType;  // MEDICARE, MEDICAID, PRIVATE_INSURANCE, FAMILY
        private String paymentMethod;  // CREDIT_CARD, ACH, CHECK, CASH, INSURANCE_DIRECT
        private BigDecimal amount;
        private LocalDate paidDate;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class InsuranceCoverageDTO {
        private Long id;
        private String providerName;
        private String providerType;  // MEDICARE, MEDICAID, PRIVATE, OTHER
        private BigDecimal coveredAmount;
        private String policyNumber;
    }
}

