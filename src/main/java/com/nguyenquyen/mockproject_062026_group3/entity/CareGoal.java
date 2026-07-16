package com.nguyenquyen.mockproject_062026_group3.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
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


  private String status;


  @ManyToOne
  @JoinColumn(name="care_plan_id")
  private CarePlan carePlan;

}