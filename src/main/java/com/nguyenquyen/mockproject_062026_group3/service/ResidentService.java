package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.common.AuditLoggingHelper;
import com.nguyenquyen.mockproject_062026_group3.dto.*;
import com.nguyenquyen.mockproject_062026_group3.entity.*;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class ResidentService {

    @Autowired
    private ResidentRepository residentRepository;

    @Autowired
    private ResidentCareLevelHistoryRepository careLevelHistoryRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private BedRepository bedRepository;

    @Autowired
    private AuditLoggingHelper auditLoggingHelper;

    private String getResidentActiveCareLevelCode(Long residentId) {
        return careLevelHistoryRepository.findByResidentIdAndEndDateIsNull(residentId)
                .map(history -> history.getCareLevel().getLevelCode())
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Page<ResidentResponse> getResidents(String status, Long bedId, String search, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<Resident> residentPage = residentRepository.findResidentsFiltered(status, bedId, search, pageable);
        return residentPage.map(resident -> {
            String currentCareLevel = getResidentActiveCareLevelCode(resident.getId());
            return ResidentResponse.fromEntity(resident, currentCareLevel);
        });
    }

    @Transactional(readOnly = true)
    public ResidentResponse getResidentById(Long id) {
        Resident resident = residentRepository.findById(id)
                .filter(r -> !r.getIsDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.RESIDENT_NOT_FOUND));
        String currentCareLevel = getResidentActiveCareLevelCode(resident.getId());
        return ResidentResponse.fromEntity(resident, currentCareLevel);
    }

    @Transactional
    public ResidentResponse createResident(ResidentCreateRequest request) {
        if (request.getFirstName() == null || request.getLastName() == null || request.getDateOfBirth() == null) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }

        Address address = null;
        if (request.getAddressId() != null) {
            address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        }

        Resident resident = Resident.builder()
                .firstName(request.getFirstName())
                .middleName(request.getMiddleName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .maritalStatus(request.getMaritalStatus())
                .religionPreference(request.getReligionPreference())
                .status("PENDING")
                .isChartLocked(false)
                .address(address)
                .isDeleted(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        resident = residentRepository.save(resident);
        return ResidentResponse.fromEntity(resident, null);
    }

    @Transactional
    public ResidentResponse updateResident(Long id, ResidentUpdateRequest request, String userRole) {
        Resident resident = residentRepository.findById(id)
                .filter(r -> !r.getIsDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.RESIDENT_NOT_FOUND));

        // Constraint: If chart is locked and role is CNA, block modification
        if (resident.getIsChartLocked() != null && resident.getIsChartLocked() && "CNA".equals(userRole)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        if (request.getFirstName() != null) resident.setFirstName(request.getFirstName());
        if (request.getMiddleName() != null) resident.setMiddleName(request.getMiddleName());
        if (request.getLastName() != null) resident.setLastName(request.getLastName());
        if (request.getDateOfBirth() != null) resident.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) resident.setGender(request.getGender());
        if (request.getMaritalStatus() != null) resident.setMaritalStatus(request.getMaritalStatus());
        if (request.getReligionPreference() != null) resident.setReligionPreference(request.getReligionPreference());

        if (request.getAddressId() != null) {
            Address address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            resident.setAddress(address);
        }

        resident.setUpdatedAt(OffsetDateTime.now());
        resident = residentRepository.save(resident);
        
        String currentCareLevel = getResidentActiveCareLevelCode(resident.getId());
        return ResidentResponse.fromEntity(resident, currentCareLevel);
    }

    @Transactional
    public ResidentResponse updateResidentStatus(Long id, ResidentStatusUpdateRequest request) {
        if (request.getStatus() == null) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }

        Resident resident = residentRepository.findById(id)
                .filter(r -> !r.getIsDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.RESIDENT_NOT_FOUND));

        String newStatus = request.getStatus().toUpperCase();
        resident.setStatus(newStatus);

        // Side effect: DISCHARGED or DECEASED clears bed and marks it AVAILABLE
        if ("DISCHARGED".equals(newStatus) || "DECEASED".equals(newStatus)) {
            if (resident.getBed() != null) {
                Bed bed = resident.getBed();
                bed.setStatus("AVAILABLE");
                bedRepository.save(bed);
                resident.setBed(null);
            }
        }

        // Side effect: DECEASED locks chart
        if ("DECEASED".equals(newStatus)) {
            resident.setIsChartLocked(true);
        }

        resident.setUpdatedAt(OffsetDateTime.now());
        resident = residentRepository.save(resident);

        String currentCareLevel = getResidentActiveCareLevelCode(resident.getId());
        return ResidentResponse.fromEntity(resident, currentCareLevel);
    }

    @Transactional
    public ResidentResponse assignResidentBed(Long residentId, ResidentBedAssignRequest request) {
        if (request.getBedId() == null) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }

        Resident resident = residentRepository.findById(residentId)
                .filter(r -> !r.getIsDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.RESIDENT_NOT_FOUND));

        Bed bed = bedRepository.findById(request.getBedId())
                .orElseThrow(() -> new AppException(ErrorCode.BED_NOT_FOUND));

        // Check if already assigned
        if (resident.getBed() != null && resident.getBed().getId().equals(bed.getId())) {
            String currentCareLevel = getResidentActiveCareLevelCode(resident.getId());
            return ResidentResponse.fromEntity(resident, currentCareLevel);
        }

        // Check availability
        if (!"AVAILABLE".equals(bed.getStatus())) {
            throw new AppException(ErrorCode.BED_NOT_AVAILABLE);
        }

        // Release old bed
        if (resident.getBed() != null) {
            Bed oldBed = resident.getBed();
            oldBed.setStatus("AVAILABLE");
            bedRepository.save(oldBed);
        }

        // Assign new bed
        resident.setBed(bed);
        bed.setStatus("OCCUPIED");
        bedRepository.save(bed);

        resident.setUpdatedAt(OffsetDateTime.now());
        resident = residentRepository.save(resident);

        String currentCareLevel = getResidentActiveCareLevelCode(resident.getId());
        return ResidentResponse.fromEntity(resident, currentCareLevel);
    }

    @Transactional
    public ResidentResponse lockResidentChart(Long id, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }

        Resident resident = residentRepository.findById(id)
                .filter(r -> !r.getIsDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.RESIDENT_NOT_FOUND));

        resident.setIsChartLocked(true);
        resident.setUpdatedAt(OffsetDateTime.now());
        resident = residentRepository.save(resident);

        // Audit log reason
        auditLoggingHelper.logAudit("residents", resident.getId().toString(), "UPDATE", 
                "isChartLocked=false", "isChartLocked=true; Reason: " + reason);

        String currentCareLevel = getResidentActiveCareLevelCode(resident.getId());
        return ResidentResponse.fromEntity(resident, currentCareLevel);
    }

    @Transactional
    public ResidentResponse unlockResidentChart(Long id, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }

        Resident resident = residentRepository.findById(id)
                .filter(r -> !r.getIsDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.RESIDENT_NOT_FOUND));

        resident.setIsChartLocked(false);
        resident.setUpdatedAt(OffsetDateTime.now());
        resident = residentRepository.save(resident);

        // Audit log reason
        auditLoggingHelper.logAudit("residents", resident.getId().toString(), "UPDATE", 
                "isChartLocked=true", "isChartLocked=false; Reason: " + reason);

        String currentCareLevel = getResidentActiveCareLevelCode(resident.getId());
        return ResidentResponse.fromEntity(resident, currentCareLevel);
    }
}
