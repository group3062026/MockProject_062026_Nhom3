package com.nguyenquyen.mockproject_062026_group3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO cho tóm tắt chi phí hàng tháng
 * sc-035
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyCostSummaryDTO {
    
    private Long residentId;
    private String residentName;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    
    private BigDecimal totalRoomBoardCost;
    private BigDecimal totalCareLevelCost;
    private BigDecimal totalMedicationCost;
    private BigDecimal totalTherapyCost;
    private BigDecimal totalOtherCost;
    
    private BigDecimal totalCost;
    private BigDecimal insuranceCovered;
    private BigDecimal patientResponsibility;
    
    // Chi tiết theo tháng
    private List<MonthlySummaryDTO> monthlySummaries;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MonthlySummaryDTO {
        private String month;  // YYYY-MM
        private BigDecimal cost;
        private BigDecimal paid;
        private BigDecimal outstanding;
    }
}

