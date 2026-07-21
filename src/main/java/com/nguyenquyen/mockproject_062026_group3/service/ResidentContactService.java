package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.dto.ContactResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.ResidentContactCreateRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.ResidentContactResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.ResidentContactUpdateRequest;
import com.nguyenquyen.mockproject_062026_group3.entity.Contact;
import com.nguyenquyen.mockproject_062026_group3.entity.Resident;
import com.nguyenquyen.mockproject_062026_group3.entity.ResidentContact;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.repository.ContactRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.ResidentContactRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.ResidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ResidentContactService {

    @Autowired
    private ResidentContactRepository residentContactRepository;

    @Autowired
    private ResidentRepository residentRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Transactional(readOnly = true)
    public List<ResidentContactResponse> getResidentContacts(Long residentId) {
        residentRepository.findById(residentId)
                .filter(r -> !r.getIsDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.RESIDENT_NOT_FOUND));

        List<ResidentContact> links = residentContactRepository.findByResidentId(residentId);
        return links.stream()
                .map(ResidentContactResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ResidentContactResponse createResidentContact(Long residentId, ResidentContactCreateRequest request) {
        if (request.getContactId() == null || request.getRelationshipType() == null
                || request.getRelationshipType().trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }

        Resident resident = residentRepository.findById(residentId)
                .filter(r -> !r.getIsDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.RESIDENT_NOT_FOUND));

        Contact contact = contactRepository.findByIdAndIsDeletedFalse(request.getContactId())
                .orElseThrow(() -> new AppException(ErrorCode.CONTACT_NOT_FOUND));

        // Auto-unset isPrimary cũ nếu isPrimary=true
        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            Optional<ResidentContact> currentPrimary = residentContactRepository
                    .findByResidentIdAndIsPrimaryTrue(residentId);
            currentPrimary.ifPresent(rc -> {
                rc.setIsPrimary(false);
                residentContactRepository.save(rc);
            });
        }

        ResidentContact link = ResidentContact.builder()
                .resident(resident)
                .contact(contact)
                .relationshipType(request.getRelationshipType().trim())
                .isGuarantor(request.getIsGuarantor() != null ? request.getIsGuarantor() : false)
                .isEmergencyContact(request.getIsEmergencyContact() != null ? request.getIsEmergencyContact() : false)
                .isPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false)
                .financialResponsibilityPct(request.getFinancialResponsibilityPct())
                .createdAt(OffsetDateTime.now())
                .build();

        link = residentContactRepository.save(link);
        return ResidentContactResponse.fromEntity(link);
    }

    @Transactional
    public ResidentContactResponse updateResidentContact(Long residentId, Long id, ResidentContactUpdateRequest request) {
        residentRepository.findById(residentId)
                .filter(r -> !r.getIsDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.RESIDENT_NOT_FOUND));

        ResidentContact link = residentContactRepository.findByResidentIdAndId(residentId, id)
                .orElseThrow(() -> new AppException(ErrorCode.RESIDENT_CONTACT_NOT_FOUND));

        if (request.getRelationshipType() != null) {
            link.setRelationshipType(request.getRelationshipType().trim());
        }
        if (request.getIsGuarantor() != null) {
            link.setIsGuarantor(request.getIsGuarantor());
        }
        if (request.getIsEmergencyContact() != null) {
            link.setIsEmergencyContact(request.getIsEmergencyContact());
        }
        if (request.getFinancialResponsibilityPct() != null) {
            link.setFinancialResponsibilityPct(request.getFinancialResponsibilityPct());
        }

        // Re-validate single-primary: nếu set isPrimary=true, unset cũ
        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            Optional<ResidentContact> currentPrimary = residentContactRepository
                    .findByResidentIdAndIsPrimaryTrue(residentId);
            currentPrimary.ifPresent(rc -> {
                if (!rc.getId().equals(id)) {
                    rc.setIsPrimary(false);
                    residentContactRepository.save(rc);
                }
            });
            link.setIsPrimary(true);
        } else if (request.getIsPrimary() != null) {
            link.setIsPrimary(false);
        }

        link = residentContactRepository.save(link);
        return ResidentContactResponse.fromEntity(link);
    }

    @Transactional
    public boolean deleteResidentContact(Long residentId, Long id) {
        residentRepository.findById(residentId)
                .filter(r -> !r.getIsDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.RESIDENT_NOT_FOUND));

        ResidentContact link = residentContactRepository.findByResidentIdAndId(residentId, id)
                .orElseThrow(() -> new AppException(ErrorCode.RESIDENT_CONTACT_NOT_FOUND));

        residentContactRepository.delete(link);
        return true;
    }

    @Transactional(readOnly = true)
    public ContactResponse getGuarantorContact(Long residentId) {
        residentRepository.findById(residentId)
                .filter(r -> !r.getIsDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.RESIDENT_NOT_FOUND));

        ResidentContact link = residentContactRepository.findByResidentIdAndIsGuarantorTrue(residentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESIDENT_CONTACT_NOT_FOUND));

        return ContactResponse.fromEntity(link.getContact());
    }

    @Transactional(readOnly = true)
    public ContactResponse getPrimaryContact(Long residentId) {
        residentRepository.findById(residentId)
                .filter(r -> !r.getIsDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.RESIDENT_NOT_FOUND));

        ResidentContact link = residentContactRepository.findByResidentIdAndIsPrimaryTrue(residentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESIDENT_CONTACT_NOT_FOUND));

        return ContactResponse.fromEntity(link.getContact());
    }
}
