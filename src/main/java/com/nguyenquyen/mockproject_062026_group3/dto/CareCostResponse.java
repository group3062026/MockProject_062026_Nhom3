package com.nguyenquyen.mockproject_062026_group3.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CareCostResponse {

  private BigDecimal locRate;

  private BigDecimal roomRate;

  private BigDecimal dailyCost;

  private BigDecimal monthlyCost;

}