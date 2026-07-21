package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.dto.ContactCreateRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.ContactResidentResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.ContactResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.ContactUpdateRequest;
import com.nguyenquyen.mockproject_062026_group3.entity.Address;
import com.nguyenquyen.mockproject_062026_group3.entity.Contact;
import com.nguyenquyen.mockproject_062026_group3.entity.ResidentContact;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.repository.AddressRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.ContactRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.ResidentContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ResidentContactRepository residentContactRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Transactional(readOnly = true)
    public Page<ContactResponse> getContacts(String search, Boolean includeDeleted, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        boolean withDeleted = includeDeleted != null && includeDeleted;

        Page<Contact> contactPage;
        if (search != null && !search.trim().isEmpty()) {
            contactPage = withDeleted
                    ? contactRepository.searchContactsIncludeDeleted(search, pageable)
                    : contactRepository.searchContacts(search, pageable);
        } else {
            contactPage = withDeleted
                    ? contactRepository.findAll(pageable)
                    : contactRepository.findByIsDeletedFalse(pageable);
        }

        return contactPage.map(ContactResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ContactResponse getContactById(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONTACT_NOT_FOUND));
        return ContactResponse.fromEntity(contact);
    }

    @Transactional
    public ContactResponse createContact(ContactCreateRequest request) {
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()
                || request.getLastName() == null || request.getLastName().trim().isEmpty()
                || request.getPhonePrimary() == null || request.getPhonePrimary().trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }

        Address address = null;
        if (request.getAddressId() != null) {
            address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        }

        Contact contact = Contact.builder()
                .firstName(request.getFirstName().trim())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName().trim())
                .phonePrimary(request.getPhonePrimary().trim())
                .phoneSecondary(request.getPhoneSecondary())
                .email(request.getEmail())
                .address(address)
                .isDeleted(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        contact = contactRepository.save(contact);
        return ContactResponse.fromEntity(contact);
    }

    @Transactional
    public ContactResponse updateContact(Long id, ContactUpdateRequest request) {
        Contact contact = contactRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONTACT_NOT_FOUND));

        if (request.getFirstName() != null) contact.setFirstName(request.getFirstName().trim());
        if (request.getMiddleName() != null) contact.setMiddleName(request.getMiddleName());
        if (request.getLastName() != null) contact.setLastName(request.getLastName().trim());
        if (request.getPhonePrimary() != null) contact.setPhonePrimary(request.getPhonePrimary().trim());
        if (request.getPhoneSecondary() != null) contact.setPhoneSecondary(request.getPhoneSecondary());
        if (request.getEmail() != null) contact.setEmail(request.getEmail());

        if (request.getAddressId() != null) {
            Address address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            contact.setAddress(address);
        }

        contact.setUpdatedAt(OffsetDateTime.now());
        contact = contactRepository.save(contact);
        return ContactResponse.fromEntity(contact);
    }

    @Transactional
    public ContactResponse deleteContact(Long id) {
        Contact contact = contactRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONTACT_NOT_FOUND));

        // Block nếu contact là guarantor cho ACTIVE resident
        boolean isActiveGuarantor = residentContactRepository
                .existsByContactIdAndIsGuarantorTrueAndResidentStatusAndResidentIsDeletedFalse(id, "ACTIVE");

        if (isActiveGuarantor) {
            throw new AppException(ErrorCode.BUSINESS_EXCEPTION);
        }

        contact.setIsDeleted(true);
        contact.setUpdatedAt(OffsetDateTime.now());
        contact = contactRepository.save(contact);
        return ContactResponse.fromEntity(contact);
    }

    @Transactional(readOnly = true)
    public List<ContactResidentResponse> getResidentsByContact(Long contactId) {
        contactRepository.findById(contactId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTACT_NOT_FOUND));

        List<ResidentContact> links = residentContactRepository.findByContactId(contactId);
        return links.stream()
                .map(ContactResidentResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
