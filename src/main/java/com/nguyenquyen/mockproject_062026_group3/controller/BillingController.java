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

 * API for managing invoices, payments, and care costs (Cost/Billing Panel)

 * sc-035, M2-US-09

 */
@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
@Slf4j
public class BillingController {
    
    private final BillingService billingService;

    /**

     * Get dashboard billing for a resident

     * Display: Total cost, payment status, recent bills

     *
     * @param residentId Resident ID

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

     * Get a list of bills for a resident (with pagination)

     *
     * @param residentId Resident ID

     * @param page Page (default 0)

     * @param pageSize Page size (default 10, max 50)

     * @param status Filter by status (DRAFT, SENT, PARTIALLY_PAID, PAID, OVERDUE, VOID)

     * @return Bill list

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

     * Get details of an invoice

     * Includes: Line items, payment list, insurance

     *
     * @param invoiceId Invoice ID

     * @return Invoice details

     */
    @GetMapping("/invoices/{invoiceId}")
    @RequireRole({"ADMIN", "MANAGER"})
    public ApiResponse<InvoiceDetailDTO> getInvoiceDetail(@PathVariable Long invoiceId) {
        log.info("Getting invoice detail for invoice: {}", invoiceId);
        InvoiceDetailDTO detail = billingService.getInvoiceDetail(invoiceId);
        return ApiResponse.success(detail);
    }

    /**

     * Get a resident's monthly expenses for a period of time

     *
     * @param residentId Resident ID

     * @param fromDate Start Date (yyyy-MM-dd)

     * @param toDate End Date (yyyy-MM-dd)

     * @return Monthly Expense Summary

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

     * Get a resident's payment list

     *
     * @param residentId Resident ID

     * @param page Page

     * @param pageSize Page size

     * @return Payment list

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

     * Get a list of residents' insurance policies

     *
     * @param residentId Resident ID

     * @return List of insurance policies

     */
    @GetMapping("/residents/{residentId}/insurance-policies")
    @RequireRole({"ADMIN", "MANAGER"})
    public ApiResponse<List<InsurancePolicyDTO>> listInsurancePolicies(@PathVariable Long residentId) {
        log.info("Getting insurance policies for resident: {}", residentId);
        List<InsurancePolicyDTO> policies = billingService.listInsurancePolicies(residentId);
        return ApiResponse.success(policies);
    }

    /**

     * Calculate estimated cost for the period

     * Based on: level of care, insurance, length of hospital stay

     *
     * @param residentId Resident ID

     * @param estimatedDays Estimated number of days

     * @return Estimated cost

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


