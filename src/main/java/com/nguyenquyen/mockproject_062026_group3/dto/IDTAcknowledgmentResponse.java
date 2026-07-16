package com.nguyenquyen.mockproject_062026_group3.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IDTAcknowledgmentResponse {

  private List<Member> members;


  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Member {

    private String role;

    private String name;

    private String status;

  }

}