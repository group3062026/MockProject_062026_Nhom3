package com.nguyenquyen.mockproject_062026_group3.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComplianceChecklistResponse {

  private Boolean planStarted48Hours;

  private Boolean comprehensivePlan7Days;

  private Boolean mdsAssessmentLinked;

  private Boolean caTitle22Addressed;

  private Integer completedCount;

}