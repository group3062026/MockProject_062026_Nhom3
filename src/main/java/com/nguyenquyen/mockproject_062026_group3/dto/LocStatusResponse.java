package com.nguyenquyen.mockproject_062026_group3.dto;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocStatusResponse {

  private Boolean confirmed;

  private String suggestedTier;

  private String confirmedTier;

}