package com.nguyenquyen.mockproject_062026_group3.dto.request;

import lombok.Data;

import java.time.OffsetDateTime;


@Data
public class CreateIncidentRequest {


  private Long residentId;


  private String incidentType;


  private Long severityId;


  private OffsetDateTime incidentTime;


  private String description;


}