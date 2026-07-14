package com.mockproject.group3.service.impl;

import com.mockproject.group3.dto.contact.ContactResidentResponse;
import com.mockproject.group3.dto.contact.ContactResponse;
import com.mockproject.group3.dto.contact.CreateContactRequest;
import com.mockproject.group3.dto.contact.UpdateContactRequest;
import com.mockproject.group3.entity.Address;
import com.mockproject.group3.entity.Contact;
import com.mockproject.group3.entity.ResidentContact;
import com.mockproject.group3.exception.BusinessLogicException;
import com.mockproject.group3.exception.ResourceNotFoundException;
import com.mockproject.group3.repository.AddressRepository;
import com.mockproject.group3.repository.ContactRepository;
import com.mockproject.group3.repository.ResidentContactRepository;
import com.mockproject.group3.service.ContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;
    private final AddressRepository addressRepository;
    private final ResidentContactRepository residentContactRepository;

    @Override
    public Page<ContactResponse> getContacts(String search, Boolean includeDeleted, Pageable pageable) {
        Page<Contact> contacts;
        
        if (search != null && !search.isBlank()) {
            if (Boolean.TRUE.equals(includeDeleted)) {
                contacts = contactRepository.searchContactsIncludeDeleted(search, pageable);
            } else {
                contacts = contactRepository.searchContacts(search, pageable);
            }
        } else {
            if (Boolean.TRUE.equals(includeDeleted)) {
                contacts = contactRepository.findAll(pageable);
            } else {
                contacts = contactRepository.findByIsDeletedFalse(pageable);
            }
        }
        
        return contacts.map(this::mapToResponse);
    }

    @Override
    public ContactResponse getContactById(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
        return mapToResponse(contact);
    }

    @Override
    @Transactional
    public ContactResponse createContact(CreateContactRequest request) {
        Contact contact = Contact.builder()
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .phonePrimary(request.getPhonePrimary())
                .phoneSecondary(request.getPhoneSecondary())
                .email(request.getEmail())
                .build();

        if (request.getAddressId() != null) {
            Address address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
            contact.setAddress(address);
        }

        return mapToResponse(contactRepository.save(contact));
    }

    @Override
    @Transactional
    public ContactResponse updateContact(Long id, UpdateContactRequest request) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));

        if (request.getFirstName() != null) contact.setFirstName(request.getFirstName());
        if (request.getMiddleName() != null) contact.setMiddleName(request.getMiddleName());
        if (request.getLastName() != null) contact.setLastName(request.getLastName());
        if (request.getPhonePrimary() != null) contact.setPhonePrimary(request.getPhonePrimary());
        if (request.getPhoneSecondary() != null) contact.setPhoneSecondary(request.getPhoneSecondary());
        if (request.getEmail() != null) contact.setEmail(request.getEmail());

        if (request.getAddressId() != null) {
            Address address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
            contact.setAddress(address);
        }

        contact.setUpdatedAt(OffsetDateTime.now());

        return mapToResponse(contactRepository.save(contact));
    }

    @Override
    @Transactional
    public void deleteContact(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));

        boolean isGuarantorForActiveResident = residentContactRepository
                .existsByContactIdAndIsGuarantorTrueAndResidentStatusAndResidentIsDeletedFalse(id, "ACTIVE");

        if (isGuarantorForActiveResident) {
            throw new BusinessLogicException("Cannot delete contact because they are a guarantor for an active resident.");
        }

        contact.setIsDeleted(true);
        contact.setUpdatedAt(OffsetDateTime.now());
        contactRepository.save(contact);
        log.info("Soft deleted contact with id: {}", id);
    }

    @Override
    public List<ContactResidentResponse> getResidentsByContact(Long contactId) {
        if (!contactRepository.existsById(contactId)) {
            throw new ResourceNotFoundException("Contact not found");
        }

        List<ResidentContact> residentContacts = residentContactRepository.findByContactId(contactId);

        return residentContacts.stream().map(rc -> ContactResidentResponse.builder()
                .residentId(rc.getResident().getId())
                .firstName(rc.getResident().getFirstName())
                .lastName(rc.getResident().getLastName())
                .relationshipType(rc.getRelationshipType())
                .build()).collect(Collectors.toList());
    }

    private ContactResponse mapToResponse(Contact contact) {
        return ContactResponse.builder()
                .id(contact.getId())
                .firstName(contact.getFirstName())
                .middleName(contact.getMiddleName())
                .lastName(contact.getLastName())
                .phonePrimary(contact.getPhonePrimary())
                .phoneSecondary(contact.getPhoneSecondary())
                .email(contact.getEmail())
                .addressId(contact.getAddress() != null ? contact.getAddress().getId() : null)
                .isDeleted(contact.getIsDeleted())
                .createdAt(contact.getCreatedAt())
                .updatedAt(contact.getUpdatedAt())
                .build();
    }
}

