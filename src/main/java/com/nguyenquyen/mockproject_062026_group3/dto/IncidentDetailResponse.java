package com.nguyenquyen.mockproject_062026_group3.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.OffsetDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDetailResponse {


  private Long id;


  private ResidentInfo resident;


  private String incidentType;


  private String status;


  private String description;


  private String severity;


  private OffsetDateTime slaDeadline;


  private OffsetDateTime reportedAt;


  private Boolean chartLocked;


  private List<TimelineResponse> timelines;



}