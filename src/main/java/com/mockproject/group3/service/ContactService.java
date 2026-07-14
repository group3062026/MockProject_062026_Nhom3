package com.mockproject.group3.service;

import com.mockproject.group3.dto.contact.ContactResidentResponse;
import com.mockproject.group3.dto.contact.ContactResponse;
import com.mockproject.group3.dto.contact.CreateContactRequest;
import com.mockproject.group3.dto.contact.UpdateContactRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContactService {

    Page<ContactResponse> getContacts(String search, Boolean includeDeleted, Pageable pageable);

    ContactResponse getContactById(Long id);

    ContactResponse createContact(CreateContactRequest request);

    ContactResponse updateContact(Long id, UpdateContactRequest request);

    void deleteContact(Long id);

    List<ContactResidentResponse> getResidentsByContact(Long contactId);
}

