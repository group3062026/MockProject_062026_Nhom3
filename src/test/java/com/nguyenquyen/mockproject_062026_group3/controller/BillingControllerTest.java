package com.nguyenquyen.mockproject_062026_group3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nguyenquyen.mockproject_062026_group3.common.PageResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.response.*;
import com.nguyenquyen.mockproject_062026_group3.service.BillingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = BillingController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        com.nguyenquyen.mockproject_062026_group3.config.SecurityConfig.class,
                        com.nguyenquyen.mockproject_062026_group3.security.JwtAuthenticationFilter.class,
                        com.nguyenquyen.mockproject_062026_group3.security.JwtAuthEntryPoint.class
                }
        )
)
@AutoConfigureMockMvc(addFilters = false)
class BillingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BillingService billingService;

    // ── getBillingDashboard ───────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getBillingDashboard_returnsSuccess() throws Exception {
        BillingDashboardDTO dto = BillingDashboardDTO.builder()
                .residentId(1L)
                .residentName("John Doe")
                .totalCost(new BigDecimal("4500.00"))
                .totalPaid(new BigDecimal("3000.00"))
                .totalOutstanding(new BigDecimal("1500.00"))
                .pendingInvoiceCount(2)
                .overdueInvoiceCount(1)
                .recentInvoices(Collections.emptyList())
                .build();

        when(billingService.getBillingDashboard(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/billing/residents/1/dashboard")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.residentId").value(1))
                .andExpect(jsonPath("$.data.residentName").value("John Doe"))
                .andExpect(jsonPath("$.data.pendingInvoiceCount").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getBillingDashboard_residentNotFound_returnsError() throws Exception {
        when(billingService.getBillingDashboard(9999L))
                .thenThrow(new jakarta.persistence.EntityNotFoundException("Không tìm thấy cư dân"));

        mockMvc.perform(get("/api/v1/billing/residents/9999/dashboard")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    // ── listInvoices ─────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void listInvoices_defaultPagination_returnsSuccess() throws Exception {
        InvoiceListDTO invoice = InvoiceListDTO.builder()
                .id(1L)
                .totalAmount(new BigDecimal("1500.00"))
                .status("SENT")
                .isOverdue(false)
                .build();

        PageResponse<InvoiceListDTO> page = PageResponse.<InvoiceListDTO>builder()
                .items(List.of(invoice))
                .pageNo(0)
                .pageSize(10)
                .totalElements(1L)
                .totalPages(1)
                .isLast(true)
                .build();

        when(billingService.listInvoices(eq(1L), isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/billing/residents/1/invoices")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value(1))
                .andExpect(jsonPath("$.data.items[0].status").value("SENT"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void listInvoices_filterByStatus_returnsFiltered() throws Exception {
        InvoiceListDTO overdue = InvoiceListDTO.builder()
                .id(2L)
                .totalAmount(new BigDecimal("2000.00"))
                .status("OVERDUE")
                .isOverdue(true)
                .daysOverdue(5)
                .build();

        PageResponse<InvoiceListDTO> page = PageResponse.<InvoiceListDTO>builder()
                .items(List.of(overdue))
                .pageNo(0).pageSize(10).totalElements(1L).totalPages(1).isLast(true)
                .build();

        when(billingService.listInvoices(eq(1L), eq("OVERDUE"), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/billing/residents/1/invoices")
                        .param("status", "OVERDUE")
                        .header("X-User-Role", "MANAGER")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].status").value("OVERDUE"))
                .andExpect(jsonPath("$.data.items[0].isOverdue").value(true));
    }

    // ── getInvoiceDetail ──────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getInvoiceDetail_returnsSuccess() throws Exception {
        InvoiceDetailDTO detail = InvoiceDetailDTO.builder()
                .id(1L)
                .residentId(1L)
                .residentName("John Doe")
                .totalAmount(new BigDecimal("1500.00"))
                .status("SENT")
                .lineItems(Collections.emptyList())
                .payments(Collections.emptyList())
                .build();

        when(billingService.getInvoiceDetail(1L)).thenReturn(detail);

        mockMvc.perform(get("/api/v1/billing/invoices/1")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.residentName").value("John Doe"))
                .andExpect(jsonPath("$.data.status").value("SENT"));
    }

    // ── getMonthlyCostSummary ─────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void getMonthlyCostSummary_returnsSuccess() throws Exception {
        MonthlyCostSummaryDTO summary = MonthlyCostSummaryDTO.builder()
                .residentId(1L)
                .periodStart(LocalDate.of(2026, 1, 1))
                .periodEnd(LocalDate.of(2026, 6, 30))
                .totalCost(BigDecimal.ZERO)
                .monthlySummaries(Collections.emptyList())
                .build();

        when(billingService.getMonthlyCostSummary(eq(1L), any(), any())).thenReturn(summary);

        mockMvc.perform(get("/api/v1/billing/residents/1/monthly-cost")
                        .param("fromDate", "2026-01-01")
                        .param("toDate", "2026-06-30")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.residentId").value(1));
    }

    // ── listPayments ──────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void listPayments_returnsSuccess() throws Exception {
        PaymentListDTO payment = PaymentListDTO.builder()
                .id(1L)
                .invoiceId(1L)
                .payerType("MEDICARE")
                .amount(new BigDecimal("500.00"))
                .build();

        PageResponse<PaymentListDTO> page = PageResponse.<PaymentListDTO>builder()
                .items(List.of(payment))
                .pageNo(0).pageSize(10).totalElements(1L).totalPages(1).isLast(true)
                .build();

        when(billingService.listPayments(eq(1L), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/billing/residents/1/payments")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].payerType").value("MEDICARE"));
    }

    // ── listInsurancePolicies ─────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void listInsurancePolicies_returnsSuccess() throws Exception {
        InsurancePolicyDTO policy = InsurancePolicyDTO.builder()
                .id(1L)
                .providerName("Medicare")
                .providerType("MEDICARE")
                .isPrimary(true)
                .build();

        when(billingService.listInsurancePolicies(1L)).thenReturn(List.of(policy));

        mockMvc.perform(get("/api/v1/billing/residents/1/insurance-policies")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].providerName").value("Medicare"))
                .andExpect(jsonPath("$.data[0].isPrimary").value(true));
    }

    // ── estimateCost ──────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void estimateCost_returnsSuccess() throws Exception {
        CostEstimateDTO estimate = CostEstimateDTO.builder()
                .residentId(1L)
                .residentName("John Doe")
                .estimatedDays(30)
                .dailyRate(new BigDecimal("150.00"))
                .estimatedTotalCost(new BigDecimal("4500.00"))
                .estimatedInsuranceCovered(new BigDecimal("3600.00"))
                .estimatedPatientResponsibility(new BigDecimal("900.00"))
                .build();

        when(billingService.estimateCost(1L, 30)).thenReturn(estimate);

        mockMvc.perform(post("/api/v1/billing/residents/1/estimate-cost")
                        .param("estimatedDays", "30")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estimatedDays").value(30))
                .andExpect(jsonPath("$.data.estimatedTotalCost").value(4500.00));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void estimateCost_90days_returnsSuccess() throws Exception {
        CostEstimateDTO estimate = CostEstimateDTO.builder()
                .residentId(1L)
                .estimatedDays(90)
                .estimatedTotalCost(new BigDecimal("13500.00"))
                .build();

        when(billingService.estimateCost(1L, 90)).thenReturn(estimate);

        mockMvc.perform(post("/api/v1/billing/residents/1/estimate-cost")
                        .param("estimatedDays", "90")
                        .header("X-User-Role", "MANAGER")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estimatedDays").value(90));
    }
}
