package com.nguyenquyen.mockproject_062026_group3.dto.request;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ESignApproveRequest {

  private String password;

  private Boolean accepted;

}