package com.nguyenquyen.mockproject_062026_group3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nguyenquyen.mockproject_062026_group3.common.SecurityUtils;
import com.nguyenquyen.mockproject_062026_group3.controller.CareLevelController;
import com.nguyenquyen.mockproject_062026_group3.controller.CareLevelRateController;
import com.nguyenquyen.mockproject_062026_group3.controller.ResidentController;
import com.nguyenquyen.mockproject_062026_group3.controller.ResidentSensitiveInfoController;
import com.nguyenquyen.mockproject_062026_group3.dto.*;
import com.nguyenquyen.mockproject_062026_group3.service.CareLevelRateService;
import com.nguyenquyen.mockproject_062026_group3.service.CareLevelService;
import com.nguyenquyen.mockproject_062026_group3.service.ResidentSensitiveInfoService;
import com.nguyenquyen.mockproject_062026_group3.service.ResidentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        CareLevelController.class,
        CareLevelRateController.class,
        ResidentController.class,
        ResidentSensitiveInfoController.class
})
public class MockProjectM1ApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CareLevelService careLevelService;

    @MockBean
    private CareLevelRateService careLevelRateService;

    @MockBean
    private ResidentService residentService;

    @MockBean
    private ResidentSensitiveInfoService sensitiveInfoService;

    @MockBean
    private SecurityUtils securityUtils;

    // --- CareLevelController API Tests ---

    @Test
    public void testGetCareLevelsApi() throws Exception {
        CareLevelResponse cl = CareLevelResponse.builder().id(1L).levelCode("MEMORY_CARE").levelName("Memory Care").isDeleted(false).build();

        when(careLevelService.getCareLevels(anyBoolean())).thenReturn(Collections.singletonList(cl));

        mockMvc.perform(get("/api/v1/care-levels")
                        .header("X-User-Role", "Nurse")
                        .param("include_deleted", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.careLevels[0].levelCode").value("MEMORY_CARE"));
    }

    @Test
    public void testUpdateCareLevelNameApi() throws Exception {
        CareLevelResponse cl = CareLevelResponse.builder().id(1L).levelCode("MEMORY_CARE").levelName("Memory Care Wing").isDeleted(false).build();

        when(careLevelService.updateCareLevelName(eq(1L), any(CareLevelUpdateRequest.class))).thenReturn(cl);

        CareLevelUpdateRequest updateRequest = CareLevelUpdateRequest.builder().levelName("Memory Care Wing").build();

        mockMvc.perform(patch("/api/v1/care-levels/1")
                        .header("X-User-Role", "DON")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.careLevel.levelName").value("Memory Care Wing"));
    }

    // --- CareLevelRateController API Tests ---

    @Test
    public void testCreateCareLevelRateApi() throws Exception {
        CareLevelRateResponse newRate = CareLevelRateResponse.builder().id(11L).careLevelId(1L).facilityId(5L).dailyRate(BigDecimal.valueOf(160.0)).build();

        CareLevelRateCreateResponse mockResponse = CareLevelRateCreateResponse.builder().newRate(newRate).build();
        when(careLevelRateService.createCareLevelRate(any(CareLevelRateCreateRequest.class))).thenReturn(mockResponse);

        CareLevelRateCreateRequest createRequest = CareLevelRateCreateRequest.builder()
                .careLevelId(1L)
                .facilityId(5L)
                .dailyRate(BigDecimal.valueOf(160.0))
                .effectiveFrom(LocalDate.of(2026, 7, 1))
                .build();

        mockMvc.perform(post("/api/v1/care-level-rates")
                        .header("X-User-Role", "System_Administrator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(200)) // ApiResponse success helper wraps with 200, but HTTP status is 201 Created
                .andExpect(jsonPath("$.data.newRate.dailyRate").value(160.0));
    }

    // --- ResidentController API Tests ---

    @Test
    public void testGetResidentsApi() throws Exception {
        ResidentResponse resident = ResidentResponse.builder().id(101L).firstName("John").lastName("Doe").currentCareLevel("MEMORY_CARE").build();

        when(residentService.getResidents(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(Collections.singletonList(resident)));

        mockMvc.perform(get("/api/v1/residents")
                        .header("X-User-Role", "Nurse")
                        .param("status", "ACTIVE")
                        .param("page", "1")
                        .param("page_size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.residents[0].firstName").value("John"))
                .andExpect(jsonPath("$.data.meta.total").value(1));
    }

    @Test
    public void testAssignResidentBedApi() throws Exception {
        ResidentResponse resident = ResidentResponse.builder().id(101L).firstName("John").lastName("Doe").bedId(402L).build();

        when(residentService.assignResidentBed(eq(101L), any(ResidentBedAssignRequest.class))).thenReturn(resident);

        ResidentBedAssignRequest assignRequest = ResidentBedAssignRequest.builder().bedId(402L).build();

        mockMvc.perform(patch("/api/v1/residents/101/assign-bed")
                        .header("X-User-Role", "Nurse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resident.bedId").value(402));
    }

    @Test
    public void testDeleteResidentApi_IsBlocked() throws Exception {
        mockMvc.perform(delete("/api/v1/residents/101"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.statusCode").value(405))
                .andExpect(jsonPath("$.message").value("Method Not Allowed"));
    }

    // --- ResidentSensitiveInfoController API Tests ---

    @Test
    public void testGetResidentSensitiveInfoApi_Masked() throws Exception {
        ResidentSensitiveInfoResponse response = ResidentSensitiveInfoResponse.builder()

                .ssnMasked("***-**-6789")
                .medicalRecordNumberMasked("MRN-****5")
                .bankAccount("XXXXXX1234")
                .primaryInsuranceId("INS-99231")
                .build();
        when(sensitiveInfoService.getSensitiveInfo(eq(101L), eq(false), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/residents/101/sensitive-info")
                        .header("X-User-Role", "DON")
                        .param("reveal", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ssnMasked").value("***-**-6789"))
                .andExpect(jsonPath("$.data.bankAccount").value("XXXXXX1234"));
    }

    @Test
    public void testGetResidentSensitiveInfoApi_Revealed() throws Exception {
        ResidentSensitiveInfoResponse response = ResidentSensitiveInfoResponse.builder()

                .ssn("123456789")
                .medicalRecordNumber("MRN12345")
                .bankAccount("1234567890")
                .primaryInsuranceId("INS-99231")
                .build();
        when(sensitiveInfoService.getSensitiveInfo(eq(101L), eq(true), eq("Audit check"))).thenReturn(response);

        mockMvc.perform(get("/api/v1/residents/101/sensitive-info")
                        .header("X-User-Role", "System_Administrator")
                        .header("X-Access-Reason", "Audit check")
                        .param("reveal", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ssn").value("123456789"))
                .andExpect(jsonPath("$.data.bankAccount").value("1234567890"));
    }
}
