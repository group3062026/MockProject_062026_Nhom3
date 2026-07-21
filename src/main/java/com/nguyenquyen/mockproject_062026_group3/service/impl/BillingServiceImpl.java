package com.nguyenquyen.mockproject_062026_group3.service.impl;

import com.nguyenquyen.mockproject_062026_group3.common.PageResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.response.*;
import com.nguyenquyen.mockproject_062026_group3.entity.*;
import com.nguyenquyen.mockproject_062026_group3.repository.*;
import com.nguyenquyen.mockproject_062026_group3.service.BillingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ResidentRepository residentRepository;
    private final ResidentInsurancePolicyRepository insurancePolicyRepository;

    @Override
    @Transactional(readOnly = true)
    public BillingDashboardDTO getBillingDashboard(Long residentId) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy cư dân với ID: " + residentId));

        List<Invoice> invoices = invoiceRepository.findByResidentIdAndIsDeletedFalse(residentId);

        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalOutstanding = BigDecimal.ZERO;
        BigDecimal totalInsuranceCovered = BigDecimal.ZERO;
        int pendingCount = 0;
        int overdueCount = 0;

        for (Invoice inv : invoices) {
            totalCost = totalCost.add(inv.getTotalAmount());

            BigDecimal paidAmount = inv.getMedicareCoveredAmount()
                    .add(inv.getMedicaidCoveredAmount())
                    .add(inv.getPrivateInsuranceCoveredAmount())
                    .add(inv.getPatientResponsibilityAmount());

            BigDecimal outstanding = inv.getTotalAmount().subtract(paidAmount);
            if (outstanding.compareTo(BigDecimal.ZERO) > 0) {
                totalOutstanding = totalOutstanding.add(outstanding);
            }

            totalInsuranceCovered = totalInsuranceCovered.add(inv.getMedicareCoveredAmount())
                    .add(inv.getMedicaidCoveredAmount())
                    .add(inv.getPrivateInsuranceCoveredAmount());

            if ("SENT".equals(inv.getStatus()) || "PARTIALLY_PAID".equals(inv.getStatus())) {
                pendingCount++;
            }
            if ("OVERDUE".equals(inv.getStatus()) || (inv.getDueDate().isBefore(LocalDate.now()) && !"PAID".equals(inv.getStatus()))) {
                overdueCount++;
            }
        }

        // Create a list of recent invoices

        List<BillingDashboardDTO.RecentInvoiceDTO> recentInvoices = invoices.stream()
                .sorted((i1, i2) -> i2.getCreatedAt().compareTo(i1.getCreatedAt()))
                .limit(5)
                .map(inv -> {
                    // Recalculate the amount paid for THIS invoice ONLY
                    BigDecimal paidForThisInvoice = inv.getMedicareCoveredAmount()
                            .add(inv.getMedicaidCoveredAmount())
                            .add(inv.getPrivateInsuranceCoveredAmount())
                            .add(inv.getPatientResponsibilityAmount());

                    return BillingDashboardDTO.RecentInvoiceDTO.builder()
                            .invoiceId(inv.getId())
                            .billingPeriodStart(inv.getBillingPeriodStart())
                            .billingPeriodEnd(inv.getBillingPeriodEnd())
                            .totalAmount(inv.getTotalAmount())
                            .paidAmount(paidForThisInvoice) // Dùng đúng biến vừa tính
                            .status(inv.getStatus())
                            .dueDate(inv.getDueDate())
                            .createdAt(inv.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return BillingDashboardDTO.builder()
                .residentId(residentId)
                .residentName(resident.getFirstName() + " " + resident.getLastName())
                .totalCost(totalCost)
                .totalPaid(totalCost.subtract(totalOutstanding))
                .totalOutstanding(totalOutstanding)
                .totalInsuranceCovered(totalInsuranceCovered)
                .pendingInvoiceCount(pendingCount)
                .overdueInvoiceCount(overdueCount)
                .dailyRate(new BigDecimal("150.00"))
                .careLevelName("Assisted Living")
                .recentInvoices(recentInvoices)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InvoiceListDTO> listInvoices(Long residentId, String status, Pageable pageable) {
        Page<Invoice> invoicePage;
        if (status != null && !status.isEmpty()) {
            invoicePage = invoiceRepository.findByResidentIdAndStatusAndIsDeletedFalse(residentId, status, pageable);
        } else {
            invoicePage = invoiceRepository.findByResidentIdAndIsDeletedFalse(residentId, pageable);
        }

        List<InvoiceListDTO> dtoList = invoicePage.getContent().stream().map(inv -> {
            LocalDate today = LocalDate.now();
            boolean isOverdue = inv.getDueDate().isBefore(today) && !"PAID".equals(inv.getStatus());
            Integer daysOverdue = isOverdue ? (int) ChronoUnit.DAYS.between(inv.getDueDate(), today) : null;

            BigDecimal paidAmount = inv.getMedicareCoveredAmount()
                    .add(inv.getMedicaidCoveredAmount())
                    .add(inv.getPrivateInsuranceCoveredAmount())
                    .add(inv.getPatientResponsibilityAmount());

            return InvoiceListDTO.builder()
                    .id(inv.getId())
                    .billingPeriodStart(inv.getBillingPeriodStart())
                    .billingPeriodEnd(inv.getBillingPeriodEnd())
                    .totalAmount(inv.getTotalAmount())
                    .paidAmount(paidAmount)
                    .outstandingAmount(inv.getTotalAmount().subtract(paidAmount))
                    .status(isOverdue && !"OVERDUE".equals(inv.getStatus()) ? "OVERDUE" : inv.getStatus())
                    .dueDate(inv.getDueDate())
                    .isOverdue(isOverdue)
                    .daysOverdue(daysOverdue)
                    .build();
        }).collect(Collectors.toList());

        // Use the correct properties from your PageResponse
        return PageResponse.<InvoiceListDTO>builder()
                .pageNo(invoicePage.getNumber())
                .pageSize(invoicePage.getSize())
                .totalPages(invoicePage.getTotalPages())
                .totalElements(invoicePage.getTotalElements())
                .items(dtoList)
                .isLast(invoicePage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceDetailDTO getInvoiceDetail(Long invoiceId) {
        Invoice inv = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Hóa đơn với ID: " + invoiceId));

        Resident res = inv.getResident();

        // Detailed invoice map (Line Items)
        List<InvoiceDetailDTO.LineItemDTO> lineItems = inv.getLineItems().stream()
                .map(item -> InvoiceDetailDTO.LineItemDTO.builder()
                        .id(item.getId())
                        .description(item.getDescription())
                        .itemType(item.getItemType())
                        .amount(item.getAmount())
                        .build())
                .collect(Collectors.toList());

        // Map payment list
        List<InvoiceDetailDTO.PaymentDTO> payments = paymentRepository.findByInvoiceId(invoiceId).stream()
                .map(p -> InvoiceDetailDTO.PaymentDTO.builder()
                        .id(p.getId())
                        .payerType(p.getPayerType())
                        .paymentMethod(p.getPaymentMethod())
                        .amount(p.getAmount())
                        .paidDate(p.getPaidAt().toLocalDate())
                        .build())
                .collect(Collectors.toList());

        return InvoiceDetailDTO.builder()
                .id(inv.getId())
                .residentId(res.getId())
                .residentName(res.getFirstName() + " " + res.getLastName())
                .billingPeriodStart(inv.getBillingPeriodStart())
                .billingPeriodEnd(inv.getBillingPeriodEnd())
                .totalAmount(inv.getTotalAmount())
                .medicareCoveredAmount(inv.getMedicareCoveredAmount())
                .medicaidCoveredAmount(inv.getMedicaidCoveredAmount())
                .privateInsuranceCoveredAmount(inv.getPrivateInsuranceCoveredAmount())
                .patientResponsibilityAmount(inv.getPatientResponsibilityAmount())
                .status(inv.getStatus())
                .dueDate(inv.getDueDate())
                .lineItems(lineItems)
                .payments(payments)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyCostSummaryDTO getMonthlyCostSummary(Long residentId, LocalDate fromDate, LocalDate toDate) {
        // Temporarily returns an empty object; a query to group data by month can be implemented later.
        return MonthlyCostSummaryDTO.builder()
                .residentId(residentId)
                .periodStart(fromDate)
                .periodEnd(toDate)
                .totalCost(BigDecimal.ZERO)
                .monthlySummaries(new ArrayList<>())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentListDTO> listPayments(Long residentId, Pageable pageable) {
        Page<Payment> paymentPage = paymentRepository.findByInvoice_ResidentId(residentId, pageable);

        List<PaymentListDTO> dtoList = paymentPage.getContent().stream().map(p ->
                PaymentListDTO.builder()
                        .id(p.getId())
                        .invoiceId(p.getInvoice().getId())
                        .payerType(p.getPayerType())
                        .paymentMethod(p.getPaymentMethod())
                        .amount(p.getAmount())
                        .paidAt(p.getPaidAt())
                        .build()
        ).collect(Collectors.toList());


        return PageResponse.<PaymentListDTO>builder()
                .pageNo(paymentPage.getNumber())
                .pageSize(paymentPage.getSize())
                .totalPages(paymentPage.getTotalPages())
                .totalElements(paymentPage.getTotalElements())
                .items(dtoList)
                .isLast(paymentPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsurancePolicyDTO> listInsurancePolicies(Long residentId) {
        List<ResidentInsurancePolicy> policies = insurancePolicyRepository.findByResidentIdAndIsDeletedFalse(residentId);

        return policies.stream().map(pol -> InsurancePolicyDTO.builder()
                .id(pol.getId())
                .providerName(pol.getInsuranceProvider().getProviderName())
                .providerType(pol.getInsuranceProvider().getProviderType())
                .policyNumber("xxxx-xxxx")
                .groupNumber(pol.getGroupNumber())
                .effectiveFrom(pol.getEffectiveFrom())
                .effectiveTo(pol.getEffectiveTo())
                .isPrimary(pol.getIsPrimary())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CostEstimateDTO estimateCost(Long residentId, int estimatedDays) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy cư dân"));

        BigDecimal dailyRate = new BigDecimal("150.00");
        BigDecimal estimatedTotal = dailyRate.multiply(BigDecimal.valueOf(estimatedDays));

        BigDecimal insuranceCovered = estimatedTotal.multiply(new BigDecimal("0.8"));
        BigDecimal patientResponsibility = estimatedTotal.subtract(insuranceCovered);

        return CostEstimateDTO.builder()
                .residentId(residentId)
                .residentName(resident.getFirstName() + " " + resident.getLastName())
                .estimatedDays(estimatedDays)
                .dailyRate(dailyRate)
                .estimatedTotalCost(estimatedTotal)
                .estimatedInsuranceCovered(insuranceCovered)
                .estimatedPatientResponsibility(patientResponsibility)
                .careLevelName("Skilled Nursing")
                .facilityName("Main Facility")
                .notes("Ước tính này dựa trên mức độ chăm sóc hiện tại và giả định bảo hiểm chi trả 80%.")
                .build();
    }
}