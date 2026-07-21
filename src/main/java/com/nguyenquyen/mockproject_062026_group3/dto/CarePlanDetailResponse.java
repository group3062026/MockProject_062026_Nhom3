package com.nguyenquyen.mockproject_062026_group3.dto;

import com.nguyenquyen.mockproject_062026_group3.entity.CareGoal;
import com.nguyenquyen.mockproject_062026_group3.entity.CareIntervention;
import com.nguyenquyen.mockproject_062026_group3.entity.CareTask;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CarePlanDetailResponse {

  private Long id;

  private String residentName;

  private String roomNumber;

  private String locTier;

  private String status;

  private Boolean significantChangeFlag;

  private List<CareGoal> goals;

  private List<CareIntervention> interventions;

  private List<CareTask> tasks;
  private CareCostResponse cost;

}