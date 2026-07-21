package com.nguyenquyen.mockproject_062026_group3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * sc-035
 * DTO cho Billing Dashboard
 * Hiển thị tóm tắt tình trạng tài chính của một cư dân
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillingDashboardDTO {
    
    private Long residentId;
    private String residentName;
    
    // Thông tin chi phí
    private BigDecimal totalCost;              // Tổng chi phí (năng lực chi trả)
    private BigDecimal totalPaid;              // Tổng đã thanh toán
    private BigDecimal totalOutstanding;       // Tổng còn nợ
    private BigDecimal totalInsuranceCovered;  // Bảo hiểm đã thanh toán
    
    // Thông tin hóa đơn gần nhất
    private Integer pendingInvoiceCount;       // Số lượng hóa đơn chờ thanh toán
    private Integer overdueInvoiceCount;       // Số lượng hóa đơn quá hạn
    
    // Chi phí hàng ngày
    private BigDecimal dailyRate;              // Tỷ giá hàng ngày
    private String careLevelName;              // Mức độ chăm sóc hiện tại
    
    // Danh sách hóa đơn gần đây (top 5)
    private List<RecentInvoiceDTO> recentInvoices;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecentInvoiceDTO {
        private Long invoiceId;
        private LocalDate billingPeriodStart;
        private LocalDate billingPeriodEnd;
        private BigDecimal totalAmount;
        private BigDecimal paidAmount;
        private String status;  // DRAFT, SENT, PARTIALLY_PAID, PAID, OVERDUE, VOID
        private LocalDate dueDate;
        private OffsetDateTime createdAt;
    }
}

