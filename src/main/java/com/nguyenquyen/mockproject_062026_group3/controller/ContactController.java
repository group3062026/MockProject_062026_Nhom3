package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.common.SecurityUtils;
import com.nguyenquyen.mockproject_062026_group3.dto.ContactCreateRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.ContactResidentResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.ContactResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.ContactUpdateRequest;
import com.nguyenquyen.mockproject_062026_group3.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/contacts")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @Autowired
    private SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getContacts(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "include_deleted", required = false, defaultValue = "false") Boolean includeDeleted,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "page_size", required = false, defaultValue = "10") int pageSize) {

        securityUtils.checkRoles("Admission_Staff", "Facility_Manager", "Accountant/Billing_Staff", "System_Administrator");

        Page<ContactResponse> contactPage = contactService.getContacts(search, includeDeleted, page, pageSize);

        Map<String, Object> data = new HashMap<>();
        data.put("contacts", contactPage.getContent());

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getContactById(@PathVariable("id") Long id) {
        securityUtils.checkRoles("Admission_Staff", "Facility_Manager", "System_Administrator");

        ContactResponse contact = contactService.getContactById(id);
        Map<String, Object> data = new HashMap<>();
        data.put("contact", contact);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createContact(
            @RequestBody ContactCreateRequest request) {
        securityUtils.checkRoles("Admission_Staff", "System_Administrator");

        ContactResponse contact = contactService.createContact(request);
        Map<String, Object> data = new HashMap<>();
        data.put("contact", contact);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(data));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateContact(
            @PathVariable("id") Long id,
            @RequestBody ContactUpdateRequest request) {
        securityUtils.checkRoles("Admission_Staff", "System_Administrator");

        ContactResponse contact = contactService.updateContact(id, request);
        Map<String, Object> data = new HashMap<>();
        data.put("contact", contact);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteContact(@PathVariable("id") Long id) {
        securityUtils.checkRoles("System_Administrator");

        ContactResponse contact = contactService.deleteContact(id);
        Map<String, Object> data = new HashMap<>();
        data.put("contact", contact);

        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/{id}/residents")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResidentsByContact(@PathVariable("id") Long id) {
        securityUtils.checkRoles("Admission_Staff", "Facility_Manager", "System_Administrator");

        List<ContactResidentResponse> residents = contactService.getResidentsByContact(id);
        Map<String, Object> data = new HashMap<>();
        data.put("residents", residents);

        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
