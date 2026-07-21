package com.nguyenquyen.mockproject_062026_group3;


import com.fasterxml.jackson.databind.ObjectMapper;

import com.nguyenquyen.mockproject_062026_group3.controller.CarePlanController;

import com.nguyenquyen.mockproject_062026_group3.dto.CarePlanDetailResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.ComplianceChecklistResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.IDTAcknowledgmentResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.careplan.CarePlanResponse;

import com.nguyenquyen.mockproject_062026_group3.dto.request.ESignApproveRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.RejectCarePlanRequest;

import com.nguyenquyen.mockproject_062026_group3.repository.CarePlanRepository;
import com.nguyenquyen.mockproject_062026_group3.service.CarePlanService;


import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;


import java.util.Collections;
import java.util.List;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@WebMvcTest(CarePlanController.class)
class CarePlanControllerTest {



  @Autowired
  private MockMvc mockMvc;



  @Autowired
  private ObjectMapper objectMapper;



  @MockitoBean
  private CarePlanService carePlanService;



  @MockitoBean
  private CarePlanRepository carePlanRepository;





  // ==========================
  // GET ALL
  // ==========================


  @Test
  void getCarePlans_success() throws Exception {


    when(
        carePlanService.getCarePlans(
            0,
            10,
            null,
            null,
            null
        )
    )
        .thenReturn(Collections.emptyList());



    mockMvc.perform(
            get("/api/v1/care-plans")
                .header(
                    "X-User-Role",
                    "NURSE"
                )
        )
        .andExpect(
            status().isOk()
        );


    verify(
        carePlanService
    )
        .getCarePlans(
            0,
            10,
            null,
            null,
            null
        );

  }




  @Test
  void getCarePlans_withoutRole_return401() throws Exception {


    mockMvc.perform(
            get("/api/v1/care-plans")
        )
        .andExpect(
            status().isUnauthorized()
        );


    verifyNoInteractions(
        carePlanService
    );

  }





  @Test
  void getCarePlans_forbiddenRole_return403() throws Exception {


    mockMvc.perform(
            get("/api/v1/care-plans")
                .header(
                    "X-User-Role",
                    "ADMIN"
                )
        )
        .andExpect(
            status().isForbidden()
        );


  }






  // ==========================
  // DETAIL
  // ==========================


  @Test
  void getCarePlanDetail_success() throws Exception {


    when(
        carePlanService.getCarePlanDetail(1L)
    )
        .thenReturn(
            new CarePlanDetailResponse()
        );



    mockMvc.perform(
            get("/api/v1/care-plans/1")
                .header(
                    "X-User-Role",
                    "CNA"
                )
        )
        .andExpect(
            status().isOk()
        );


    verify(
        carePlanService
    )
        .getCarePlanDetail(1L);

  }





  @Test
  void getCarePlanDetail_invalidId() throws Exception {


    mockMvc.perform(
            get("/api/v1/care-plans/abc")
                .header(
                    "X-User-Role",
                    "NURSE"
                )
        )
        .andExpect(
            status().isBadRequest()
        );


  }







  // ==========================
  // COMPLIANCE
  // ==========================


  @Test
  void complianceChecklist_success() throws Exception {


    when(
        carePlanService.getComplianceChecklist(1L)
    )
        .thenReturn(
            new ComplianceChecklistResponse()
        );



    mockMvc.perform(
            get("/api/v1/care-plans/1/compliance-checklist")
        )
        .andExpect(
            status().isOk()
        );



    verify(
        carePlanService
    )
        .getComplianceChecklist(1L);


  }







  // ==========================
  // IDT
  // ==========================


  @Test
  void idtAcknowledgment_success() throws Exception {



    when(
        carePlanService.getIDTAcknowledgment(1L)
    )
        .thenReturn(
            new IDTAcknowledgmentResponse()
        );



    mockMvc.perform(
            get("/api/v1/care-plans/1/idt-acknowledgment")
        )
        .andExpect(
            status().isOk()
        );


    verify(
        carePlanService
    )
        .getIDTAcknowledgment(1L);


  }








  // ==========================
  // APPROVE
  // ==========================


  @Test
  void approve_success() throws Exception {


    when(
        carePlanService.approveCarePlan(1L)
    )
        .thenReturn(
            new CarePlanDetailResponse()
        );



    mockMvc.perform(
            post("/api/v1/care-plans/1/approve")
                .header(
                    "X-User-Role",
                    "DON"
                )
        )
        .andExpect(
            status().isOk()
        );


    verify(
        carePlanService
    )
        .approveCarePlan(1L);


  }








  // ==========================
  // REJECT
  // ==========================


  @Test
  void reject_success() throws Exception {



    RejectCarePlanRequest request =
        new RejectCarePlanRequest();

    request.setReason(
        "Wrong information"
    );



    when(
        carePlanService.rejectCarePlan(
            eq(1L),
            eq("Wrong information")
        )
    )
        .thenReturn(
            new CarePlanDetailResponse()
        );




    mockMvc.perform(
            post("/api/v1/care-plans/1/reject")
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



    verify(
        carePlanService
    )
        .rejectCarePlan(
            1L,
            "Wrong information"
        );


  }







  @Test
  void reject_withoutRole_return401() throws Exception {


    RejectCarePlanRequest request =
        new RejectCarePlanRequest();

    request.setReason(
        "test"
    );



    mockMvc.perform(
            post("/api/v1/care-plans/1/reject")
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








  // ==========================
  // APPROVE SIGN
  // ==========================



  @Test
  void approveSign_success() throws Exception {



    ESignApproveRequest request =
        new ESignApproveRequest(
            "123456",
            true
        );



    when(
        carePlanService.approveWithSign(
            eq(1L),
            any(ESignApproveRequest.class)
        )
    )
        .thenReturn(
            new CarePlanDetailResponse()
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



    verify(
        carePlanService
    )
        .approveWithSign(
            eq(1L),
            any()
        );


  }






  @Test
  void approveSign_withoutRole_return401() throws Exception {



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