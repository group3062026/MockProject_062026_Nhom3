package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.common.SecurityUtils;
import com.nguyenquyen.mockproject_062026_group3.dto.ContactResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.ResidentContactCreateRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.ResidentContactResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.ResidentContactUpdateRequest;
import com.nguyenquyen.mockproject_062026_group3.service.ResidentContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/residents/{resident_id}/contacts")
public class ResidentContactController {

    @Autowired
    private ResidentContactService residentContactService;

    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResidentContacts(
            @PathVariable("resident_id") Long residentId) {
        securityUtils.checkRoles("Nurse", "CNA", "DON", "Admission_Staff", "Accountant/Billing_Staff", "System_Administrator");

        List<ResidentContactResponse> contacts = residentContactService.getResidentContacts(residentId);
        Map<String, Object> data = new HashMap<>();
        data.put("contacts", contacts);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createResidentContact(
            @PathVariable("resident_id") Long residentId,
            @RequestBody ResidentContactCreateRequest request) {
        securityUtils.checkRoles("Admission_Staff", "System_Administrator");

        ResidentContactResponse residentContact = residentContactService.createResidentContact(residentId, request);
        Map<String, Object> data = new HashMap<>();
        data.put("residentContact", residentContact);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(data));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateResidentContact(
            @PathVariable("resident_id") Long residentId,
            @PathVariable("id") Long id,
            @RequestBody ResidentContactUpdateRequest request) {
        securityUtils.checkRoles("Admission_Staff", "System_Administrator");

        ResidentContactResponse residentContact = residentContactService.updateResidentContact(residentId, id, request);
        Map<String, Object> data = new HashMap<>();
        data.put("residentContact", residentContact);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteResidentContact(
            @PathVariable("resident_id") Long residentId,
            @PathVariable("id") Long id) {
        securityUtils.checkRoles("Admission_Staff", "System_Administrator");

        boolean deleted = residentContactService.deleteResidentContact(residentId, id);
        Map<String, Object> data = new HashMap<>();
        data.put("deleted", deleted);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/guarantor")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResidentGuarantorContact(
            @PathVariable("resident_id") Long residentId) {
        securityUtils.checkRoles("Accountant/Billing_Staff", "Admission_Staff", "System_Administrator");

        ContactResponse contact = residentContactService.getGuarantorContact(residentId);
        Map<String, Object> data = new HashMap<>();
        data.put("contact", contact);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/primary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResidentPrimaryContact(
            @PathVariable("resident_id") Long residentId) {
        securityUtils.checkRoles("Admission_Staff", "DON", "System_Administrator");

        ContactResponse contact = residentContactService.getPrimaryContact(residentId);
        Map<String, Object> data = new HashMap<>();
        data.put("contact", contact);

        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
