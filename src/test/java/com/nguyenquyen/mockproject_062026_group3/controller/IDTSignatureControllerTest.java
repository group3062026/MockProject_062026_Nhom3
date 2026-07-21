package com.nguyenquyen.mockproject_062026_group3.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nguyenquyen.mockproject_062026_group3.dto.request.IDTSignatureRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.response.IDTSignatureResponse;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.service.IDTSignatureService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = IDTSignatureController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        com.nguyenquyen.mockproject_062026_group3.config.SecurityConfig.class,
                        com.nguyenquyen.mockproject_062026_group3.security.JwtAuthenticationFilter.class,
                        com.nguyenquyen.mockproject_062026_group3.security.JwtAuthEntryPoint.class
                }
        )
)
@AutoConfigureMockMvc(addFilters = false)
class IDTSignatureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IDTSignatureService idtSignatureService;

    // ── getIDTSignatures ──────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "DON")
    void getIDTSignatures_returnsSuccess() throws Exception {
        IDTSignatureResponse response = IDTSignatureResponse.builder()
                .signatures(List.of(
                        IDTSignatureResponse.SignatureItem.builder()
                                .id(1L)
                                .userId(5L)
                                .userName("Alan Cho")
                                .role("PHYSICIAN")
                                .comments("Reviewed and acknowledged.")
                                .signedAt(OffsetDateTime.now())
                                .build(),
                        IDTSignatureResponse.SignatureItem.builder()
                                .id(2L)
                                .userId(6L)
                                .userName("Grace Liu")
                                .role("DIETITIAN")
                                .comments("Nutritional goals reviewed.")
                                .signedAt(OffsetDateTime.now())
                                .build()
                ))
                .build();

        when(idtSignatureService.getIDTSignatures(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/care-plans/1/idt-signatures")
                        .header("X-User-Role", "DON")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.signatures.length()").value(2))
                .andExpect(jsonPath("$.data.signatures[0].userName").value("Alan Cho"))
                .andExpect(jsonPath("$.data.signatures[0].role").value("PHYSICIAN"))
                .andExpect(jsonPath("$.data.signatures[1].userName").value("Grace Liu"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getIDTSignatures_asAdmin_returnsSuccess() throws Exception {
        IDTSignatureResponse response = IDTSignatureResponse.builder()
                .signatures(Collections.emptyList())
                .build();

        when(idtSignatureService.getIDTSignatures(2L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/care-plans/2/idt-signatures")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.signatures").isEmpty());
    }

    @Test
    @WithMockUser(roles = "DON")
    void getIDTSignatures_carePlanNotFound_returnsError() throws Exception {
        when(idtSignatureService.getIDTSignatures(9999L))
                .thenThrow(new AppException(ErrorCode.CARE_PLAN_NOT_FOUND));

        mockMvc.perform(get("/api/v1/care-plans/9999/idt-signatures")
                        .header("X-User-Role", "DON")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "DON")
    void getIDTSignatures_withoutRole_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/care-plans/1/idt-signatures")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ── submitIDTSignature ────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "PHYSICIAN")
    void submitIDTSignature_physician_returnsCreated() throws Exception {
        IDTSignatureRequest request = new IDTSignatureRequest();
        request.setComments("Reviewed and acknowledged.");

        IDTSignatureResponse.SignatureItem item = IDTSignatureResponse.SignatureItem.builder()
                .id(10L)
                .userId(5L)
                .userName("Alan Cho")
                .role("PHYSICIAN")
                .comments("Reviewed and acknowledged.")
                .signedAt(OffsetDateTime.now())
                .build();

        when(idtSignatureService.submitIDTSignature(eq(1L), any())).thenReturn(item);

        mockMvc.perform(post("/api/v1/care-plans/1/idt-signatures")
                        .header("X-User-Role", "PHYSICIAN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.userName").value("Alan Cho"))
                .andExpect(jsonPath("$.data.role").value("PHYSICIAN"))
                .andExpect(jsonPath("$.data.comments").value("Reviewed and acknowledged."));
    }

    @Test
    @WithMockUser(roles = "DIETITIAN")
    void submitIDTSignature_dietitian_returnsCreated() throws Exception {
        IDTSignatureRequest request = new IDTSignatureRequest();
        request.setComments("Nutritional goals reviewed.");

        IDTSignatureResponse.SignatureItem item = IDTSignatureResponse.SignatureItem.builder()
                .id(11L)
                .userId(6L)
                .userName("Grace Liu")
                .role("DIETITIAN")
                .comments("Nutritional goals reviewed.")
                .signedAt(OffsetDateTime.now())
                .build();

        when(idtSignatureService.submitIDTSignature(eq(1L), any())).thenReturn(item);

        mockMvc.perform(post("/api/v1/care-plans/1/idt-signatures")
                        .header("X-User-Role", "DIETITIAN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("DIETITIAN"));
    }

    @Test
    @WithMockUser(roles = "PHYSICIAN")
    void submitIDTSignature_nullComments_returnsCreated() throws Exception {
        IDTSignatureRequest request = new IDTSignatureRequest();
        request.setComments(null);

        IDTSignatureResponse.SignatureItem item = IDTSignatureResponse.SignatureItem.builder()
                .id(12L).userId(5L).userName("Alan Cho").role("PHYSICIAN")
                .comments(null).signedAt(OffsetDateTime.now())
                .build();

        when(idtSignatureService.submitIDTSignature(eq(1L), any())).thenReturn(item);

        mockMvc.perform(post("/api/v1/care-plans/1/idt-signatures")
                        .header("X-User-Role", "PHYSICIAN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "PHYSICIAN")
    void submitIDTSignature_duplicate_returnsConflict() throws Exception {
        IDTSignatureRequest request = new IDTSignatureRequest();
        request.setComments("Trying again");

        when(idtSignatureService.submitIDTSignature(eq(1L), any()))
                .thenThrow(new AppException(ErrorCode.RESOURCE_ALREADY_EXISTS));

        mockMvc.perform(post("/api/v1/care-plans/1/idt-signatures")
                        .header("X-User-Role", "PHYSICIAN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "PHYSICIAN")
    void submitIDTSignature_withoutRole_returnsUnauthorized() throws Exception {
        IDTSignatureRequest request = new IDTSignatureRequest();
        request.setComments("Test");

        mockMvc.perform(post("/api/v1/care-plans/1/idt-signatures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
