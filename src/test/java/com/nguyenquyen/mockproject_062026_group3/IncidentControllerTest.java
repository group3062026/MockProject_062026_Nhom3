package com.nguyenquyen.mockproject_062026_group3;


import com.fasterxml.jackson.databind.ObjectMapper;

import com.nguyenquyen.mockproject_062026_group3.controller.IncidentController;
import com.nguyenquyen.mockproject_062026_group3.dto.request.CreateIncidentRequest;
import com.nguyenquyen.mockproject_062026_group3.service.IncidentService;


import org.junit.jupiter.api.Test;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@WebMvcTest(IncidentController.class)
class IncidentControllerTest {


  @Autowired
  private MockMvc mockMvc;


  @Autowired
  private ObjectMapper objectMapper;



  @MockitoBean
  private IncidentService incidentService;



  /**
   * Test API:
   * GET /api/v1/incidents
   *
   * Role:
   * Nurse
   */
  @Test
  void getIncidentList_success()
      throws Exception {


    mockMvc.perform(

            get("/api/v1/incidents")

                .header(
                    "X-User-Role",
                    "NURSE"
                )

        )

        .andExpect(
            status().isOk()
        );

  }




  /**
   * Test API:
   * POST /api/v1/incidents
   *
   * Create new incident
   */
  @Test
  void createIncident_success()
      throws Exception {



    CreateIncidentRequest request =
        new CreateIncidentRequest();


    request.setResidentId(1L);

    request.setSeverityId(1L);

    request.setIncidentType(
        "FALL"
    );

    request.setDescription(
        "Resident fell in bathroom"
    );



    mockMvc.perform(

            post("/api/v1/incidents")

                .header(
                    "X-User-Role",
                    "NURSE"
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





  /**
   * Test API:
   * Unauthorized request
   */
  @Test
  void getIncident_withoutRole_fail()
      throws Exception {



    mockMvc.perform(

            get("/api/v1/incidents")

        )

        .andExpect(
            status().isUnauthorized()
        );


  }


}