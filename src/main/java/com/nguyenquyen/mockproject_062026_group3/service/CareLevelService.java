package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.dto.CareLevelResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.CareLevelUpdateRequest;
import com.nguyenquyen.mockproject_062026_group3.entity.CareLevel;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.repository.CareLevelRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.ResidentCareLevelHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CareLevelService {

    @Autowired
    private CareLevelRepository careLevelRepository;

    @Autowired
    private ResidentCareLevelHistoryRepository residentCareLevelHistoryRepository;

    @Transactional(readOnly = true)
    public List<CareLevelResponse> getCareLevels(Boolean includeDeleted) {
        List<CareLevel> careLevels;
        if (includeDeleted != null && includeDeleted) {
            careLevels = careLevelRepository.findAll();
        } else {
            careLevels = careLevelRepository.findAllByIsDeleted(false);
        }
        return careLevels.stream()
                .map(CareLevelResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CareLevelResponse getCareLevelById(Long id) {
        CareLevel careLevel = careLevelRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CARE_LEVEL_NOT_FOUND));
        return CareLevelResponse.fromEntity(careLevel);
    }

    @Transactional
    public CareLevelResponse updateCareLevelName(Long id, CareLevelUpdateRequest request) {
        CareLevel careLevel = careLevelRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CARE_LEVEL_NOT_FOUND));
        
        if (request.getLevelName() == null || request.getLevelName().trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }
        
        careLevel.setLevelName(request.getLevelName().trim());
        careLevel = careLevelRepository.save(careLevel);
        return CareLevelResponse.fromEntity(careLevel);
    }

    @Transactional
    public boolean deleteCareLevel(Long id) {
        CareLevel careLevel = careLevelRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CARE_LEVEL_NOT_FOUND));

        if (careLevel.getIsDeleted()) {
            return true; // Already deleted
        }

        // Check if blocked by active residents referencing this care level
        boolean hasActiveResident = residentCareLevelHistoryRepository
                .existsByCareLevelIdAndEndDateIsNullAndResidentStatus(id, "ACTIVE");

        if (hasActiveResident) {
            // Cannot delete because it is currently assigned to an active resident
            throw new AppException(ErrorCode.BUSINESS_EXCEPTION); 
        }

        careLevel.setIsDeleted(true);
        careLevelRepository.save(careLevel);
        return true;
    }
}
