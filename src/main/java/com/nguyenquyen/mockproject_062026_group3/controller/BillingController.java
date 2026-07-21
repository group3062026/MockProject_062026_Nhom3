package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.common.RequireRole;
import com.nguyenquyen.mockproject_062026_group3.common.PageResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.response.*;
import com.nguyenquyen.mockproject_062026_group3.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * API để quản lý hóa đơn, thanh toán và chi phí chăm sóc (Cost/Billing Panel)
 * sc-035, M2-US-09
 */
@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
@Slf4j
public class BillingController {
    
    private final BillingService billingService;
    
    /**
     * Lấy dashboard billing cho một cư dân
     * Hiển thị: Tổng chi phí, tình trạng thanh toán, các hóa đơn gần đây
     * 
     * @param residentId ID của cư dân
     * @return Dashboard billing data
     */
    @GetMapping("/residents/{residentId}/dashboard")
    @RequireRole({"ADMIN", "MANAGER"})
    public ApiResponse<BillingDashboardDTO> getBillingDashboard(@PathVariable Long residentId) {
        log.info("Getting billing dashboard for resident: {}", residentId);
        BillingDashboardDTO dashboard = billingService.getBillingDashboard(residentId);
        return ApiResponse.success(dashboard);
    }
    
    /**
     * Lấy danh sách hóa đơn của một cư dân (có phân trang)
     * 
     * @param residentId ID của cư dân
     * @param page Trang (mặc định 0)
     * @param pageSize Kích thước trang (mặc định 10, max 50)
     * @param status Lọc theo trạng thái (DRAFT, SENT, PARTIALLY_PAID, PAID, OVERDUE, VOID)
     * @return Danh sách hóa đơn
     */
    @GetMapping("/residents/{residentId}/invoices")
    @RequireRole({"ADMIN", "MANAGER"})
    public ApiResponse<PageResponse<InvoiceListDTO>> listInvoices(
            @PathVariable Long residentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        
        log.info("Listing invoices for resident: {}, status: {}", residentId, status);
        
        if (pageSize > 50) pageSize = 50;
        if (page < 0) page = 0;
        
        Pageable pageable = PageRequest.of(page, pageSize);
        PageResponse<InvoiceListDTO> response = billingService.listInvoices(residentId, status, pageable);
        
        return ApiResponse.success(response);
    }
    
    /**
     * Lấy chi tiết một hóa đơn
     * Bao gồm: Line items, danh sách thanh toán, bảo hiểm
     * 
     * @param invoiceId ID của hóa đơn
     * @return Chi tiết hóa đơn
     */
    @GetMapping("/invoices/{invoiceId}")
    @RequireRole({"ADMIN", "MANAGER"})
    public ApiResponse<InvoiceDetailDTO> getInvoiceDetail(@PathVariable Long invoiceId) {
        log.info("Getting invoice detail for invoice: {}", invoiceId);
        InvoiceDetailDTO detail = billingService.getInvoiceDetail(invoiceId);
        return ApiResponse.success(detail);
    }
    
    /**
     * Lấy chi phí hàng tháng của một cư dân trong một khoảng thời gian
     * 
     * @param residentId ID của cư dân
     * @param fromDate Ngày bắt đầu (yyyy-MM-dd)
     * @param toDate Ngày kết thúc (yyyy-MM-dd)
     * @return Tóm tắt chi phí hàng tháng
     */
    @GetMapping("/residents/{residentId}/monthly-cost")
    @RequireRole({"ADMIN", "MANAGER"})
    public ApiResponse<MonthlyCostSummaryDTO> getMonthlyCostSummary(
            @PathVariable Long residentId,
            @RequestParam String fromDate,
            @RequestParam String toDate) {
        
        log.info("Getting monthly cost summary for resident: {} from {} to {}", 
            residentId, fromDate, toDate);
        
        MonthlyCostSummaryDTO summary = billingService.getMonthlyCostSummary(
            residentId, 
            LocalDate.parse(fromDate), 
            LocalDate.parse(toDate)
        );
        
        return ApiResponse.success(summary);
    }
    
    /**
     * Lấy danh sách thanh toán của một cư dân
     * 
     * @param residentId ID của cư dân
     * @param page Trang
     * @param pageSize Kích thước trang
     * @return Danh sách thanh toán
     */
    @GetMapping("/residents/{residentId}/payments")
    @RequireRole({"ADMIN", "MANAGER"})
    public ApiResponse<PageResponse<PaymentListDTO>> listPayments(
            @PathVariable Long residentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        log.info("Listing payments for resident: {}", residentId);
        
        if (pageSize > 50) pageSize = 50;
        if (page < 0) page = 0;
        
        Pageable pageable = PageRequest.of(page, pageSize);
        PageResponse<PaymentListDTO> response = billingService.listPayments(residentId, pageable);
        
        return ApiResponse.success(response);
    }
    
    /**
     * Lấy danh sách chính sách bảo hiểm của cư dân
     * 
     * @param residentId ID của cư dân
     * @return Danh sách chính sách bảo hiểm
     */
    @GetMapping("/residents/{residentId}/insurance-policies")
    @RequireRole({"ADMIN", "MANAGER"})
    public ApiResponse<List<InsurancePolicyDTO>> listInsurancePolicies(@PathVariable Long residentId) {
        log.info("Getting insurance policies for resident: {}", residentId);
        List<InsurancePolicyDTO> policies = billingService.listInsurancePolicies(residentId);
        return ApiResponse.success(policies);
    }
    
    /**
     * Tính toán chi phí dự kiến cho khoảng thời gian
     * Dựa trên: mức độ chăm sóc, bảo hiểm, thời gian nằm viện
     * 
     * @param residentId ID của cư dân
     * @param estimatedDays Số ngày dự kiến
     * @return Ước tính chi phí
     */
    @PostMapping("/residents/{residentId}/estimate-cost")
    @RequireRole({"ADMIN", "MANAGER"})
    public ApiResponse<CostEstimateDTO> estimateCost(
            @PathVariable Long residentId,
            @RequestParam int estimatedDays) {
        
        log.info("Estimating cost for resident: {} for {} days", residentId, estimatedDays);
        CostEstimateDTO estimate = billingService.estimateCost(residentId, estimatedDays);
        
        return ApiResponse.success(estimate);
    }
}


