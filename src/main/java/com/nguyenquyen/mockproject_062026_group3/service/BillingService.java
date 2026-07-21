package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.common.PageResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.response.*;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface cho Billing Management (M2-US-09)
 * Quản lý hóa đơn, thanh toán, chi phí và bảo hiểm
 */
public interface BillingService {
    
    /**
     * Lấy dashboard billing cho một cư dân
     */
    BillingDashboardDTO getBillingDashboard(Long residentId);
    
    /**
     * Lấy danh sách hóa đơn của một cư dân
     */
    PageResponse<InvoiceListDTO> listInvoices(Long residentId, String status, Pageable pageable);
    
    /**
     * Lấy chi tiết một hóa đơn
     */
    InvoiceDetailDTO getInvoiceDetail(Long invoiceId);
    
    /**
     * Lấy tóm tắt chi phí hàng tháng
     */
    MonthlyCostSummaryDTO getMonthlyCostSummary(Long residentId, LocalDate fromDate, LocalDate toDate);
    
    /**
     * Lấy danh sách thanh toán
     */
    PageResponse<PaymentListDTO> listPayments(Long residentId, Pageable pageable);
    
    /**
     * Lấy danh sách chính sách bảo hiểm
     */
    List<InsurancePolicyDTO> listInsurancePolicies(Long residentId);
    
    /**
     * Ước tính chi phí
     */
    CostEstimateDTO estimateCost(Long residentId, int estimatedDays);
}

