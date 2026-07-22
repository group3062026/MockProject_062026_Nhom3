package com.nguyenquyen.mockproject_062026_group3.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.OffsetDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResponse {


  private Long id;


  private String residentName;


  private String incidentType;


  private String severity;


  private String status;


  private OffsetDateTime reportedAt;


  private OffsetDateTime slaDeadline;


  private Boolean chartLocked;

}