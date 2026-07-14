package com.mockproject.group3.controller;

import com.mockproject.group3.common.ApiResponse;
import com.mockproject.group3.common.AppConstants;
import com.mockproject.group3.dto.contact.ContactResidentResponse;
import com.mockproject.group3.dto.contact.ContactResponse;
import com.mockproject.group3.dto.contact.CreateContactRequest;
import com.mockproject.group3.dto.contact.UpdateContactRequest;
import com.mockproject.group3.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller handling management of contacts.
 */
@RestController
@RequestMapping(AppConstants.API_PREFIX + "/contacts")
@RequiredArgsConstructor
@Tag(name = "Contacts", description = "Management of contacts (family, emergency, etc.)")
public class ContactController {

    private final ContactService contactService;

    @Operation(summary = "Get list of contacts", description = "Supports searching by name/phone/email and filtering soft-deleted records")
    @PreAuthorize("hasAnyRole('ADMISSION_STAFF', 'FACILITY_MANAGER', 'ACCOUNTANT', 'SYSTEM_ADMINISTRATOR')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ContactResponse>>> getContacts(
            @RequestParam(required = false) String search,
            @RequestParam(name = "include_deleted", required = false, defaultValue = "false") Boolean includeDeleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "page_size", defaultValue = "10") int pageSize) {
        
        Pageable pageable = PageRequest.of(page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(contactService.getContacts(search, includeDeleted, pageable)));
    }

    @Operation(summary = "Get contact by ID")
    @PreAuthorize("hasAnyRole('ADMISSION_STAFF', 'FACILITY_MANAGER', 'SYSTEM_ADMINISTRATOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContactResponse>> getContactById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(contactService.getContactById(id)));
    }

    @Operation(summary = "Create a new contact", description = "Creates a standalone contact (not yet linked to any resident)")
    @PreAuthorize("hasAnyRole('ADMISSION_STAFF', 'SYSTEM_ADMINISTRATOR')")
    @PostMapping
    public ResponseEntity<ApiResponse<ContactResponse>> createContact(@Valid @RequestBody CreateContactRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(contactService.createContact(request)));
    }

    @Operation(summary = "Update contact details")
    @PreAuthorize("hasAnyRole('ADMISSION_STAFF', 'SYSTEM_ADMINISTRATOR')")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ContactResponse>> updateContact(
            @PathVariable Long id,
            @Valid @RequestBody UpdateContactRequest request) {
        return ResponseEntity.ok(ApiResponse.success(contactService.updateContact(id, request)));
    }

    @Operation(summary = "Delete contact", description = "Soft deletes a contact; blocked if contact is guarantor for an ACTIVE resident")
    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContact(@PathVariable Long id) {
        contactService.deleteContact(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .statusCode(200)
                .message("Success")
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get residents by contact", description = "Returns residents linked to this contact")
    @PreAuthorize("hasAnyRole('ADMISSION_STAFF', 'FACILITY_MANAGER', 'SYSTEM_ADMINISTRATOR')")
    @GetMapping("/{id}/residents")
    public ResponseEntity<ApiResponse<List<ContactResidentResponse>>> getResidentsByContact(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(contactService.getResidentsByContact(id)));
    }
}

