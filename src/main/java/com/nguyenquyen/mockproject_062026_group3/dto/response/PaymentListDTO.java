package com.nguyenquyen.mockproject_062026_group3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * sc-035
 * DTO cho Payment trong danh sách
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentListDTO {
    
    private Long id;
    private Long invoiceId;
    private String payerType;  // MEDICARE, MEDICAID, PRIVATE_INSURANCE, FAMILY
    private String paymentMethod;  // CREDIT_CARD, ACH, CHECK, CASH, INSURANCE_DIRECT
    private BigDecimal amount;
    private OffsetDateTime paidAt;
}

