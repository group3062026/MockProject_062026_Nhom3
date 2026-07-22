package com.nguyenquyen.mockproject_062026_group3.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentSummaryResponse {


  private long total;


  private long open;


  private long underInvestigation;


  private long resolved;


  private long chartLocked;


}