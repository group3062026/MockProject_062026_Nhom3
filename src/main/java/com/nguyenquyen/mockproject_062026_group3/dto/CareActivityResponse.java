package com.nguyenquyen.mockproject_062026_group3.dto;


import lombok.*;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CareActivityResponse {

  private String action;

  private String performedBy;

  private OffsetDateTime performedAt;

}