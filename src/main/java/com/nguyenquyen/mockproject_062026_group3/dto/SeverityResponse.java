package com.nguyenquyen.mockproject_062026_group3.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeverityResponse {


  private Long id;


  private String levelName;


  private Boolean automaticLockChart;


  private Integer resolutionTime;

}