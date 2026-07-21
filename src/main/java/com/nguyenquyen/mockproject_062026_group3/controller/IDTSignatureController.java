package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.common.SecurityUtils;
import com.nguyenquyen.mockproject_062026_group3.dto.request.IDTSignatureRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.response.IDTSignatureResponse;
import com.nguyenquyen.mockproject_062026_group3.service.impl.IDTSignatureServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
//sc-036
@RestController
@RequestMapping("/api/v1/care-plans/{carePlanId}/idt-signatures")
public class IDTSignatureController {

    @Autowired
    private IDTSignatureServiceImpl idtSignatureService;

    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<IDTSignatureResponse>> getIDTSignatures(
            @PathVariable Long carePlanId
    ) {
        securityUtils.checkRoles("DON", "ADMIN");

        return ResponseEntity.ok(
                ApiResponse.success(idtSignatureService.getIDTSignatures(carePlanId))
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<IDTSignatureResponse.SignatureItem>> submitIDTSignature(
            @PathVariable Long carePlanId,
            @RequestBody IDTSignatureRequest request
    ) {
        securityUtils.checkRoles("PHYSICIAN", "DIETITIAN");

        return ResponseEntity.status(201).body(
                ApiResponse.created(idtSignatureService.submitIDTSignature(carePlanId, request))
        );
    }
}
