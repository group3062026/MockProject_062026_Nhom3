package com.nguyenquyen.mockproject_062026_group3.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="care_goals")
public class CareGoal {



  @Id
  @GeneratedValue
  private Long id;


  @Column(name="care_area_name")
  private String careAreaName;


  @Column(name="source_type")
  private String sourceType;


  @Column(name="goal_description")
  private String goalDescription;


  @Column(name="measure")
  private String measure;


  @Column(name="target_date")
  private LocalDate targetDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // IN_PROGRESS, ACHIEVED, NOT_MET


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "care_plan_id", nullable = false)
    private CarePlan carePlan;






}
