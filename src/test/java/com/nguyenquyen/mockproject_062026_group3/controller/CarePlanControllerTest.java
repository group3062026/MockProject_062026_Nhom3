package com.nguyenquyen.mockproject_062026_group3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nguyenquyen.mockproject_062026_group3.dto.request.ESignApproveRequest;
import com.nguyenquyen.mockproject_062026_group3.service.CarePlanService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CarePlanController.class)
class CarePlanControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  // Thay thế @MockBean bằng @MockitoBean
  @MockitoBean
  private CarePlanService carePlanService;

  @Test
  void approveSign_success() throws Exception {

    ESignApproveRequest request =
        new ESignApproveRequest(
            "123456",
            true
        );

    mockMvc.perform(
            post("/api/v1/care-plans/1/approve-sign")
                .header(
                    "X-User-Role",
                    "DON"
                )
                .contentType(
                    MediaType.APPLICATION_JSON
                )
                .content(
                    objectMapper.writeValueAsString(request)
                )
        )
        .andExpect(
            status().isOk()
        );
  }

  @Test
  void approveSign_withoutRole() throws Exception {

    ESignApproveRequest request =
        new ESignApproveRequest(
            "123456",
            true
        );

    mockMvc.perform(
            post("/api/v1/care-plans/1/approve-sign")
                .contentType(
                    MediaType.APPLICATION_JSON
                )
                .content(
                    objectMapper.writeValueAsString(request)
                )
        )
        .andExpect(
            status().isUnauthorized()
        );
  }

}