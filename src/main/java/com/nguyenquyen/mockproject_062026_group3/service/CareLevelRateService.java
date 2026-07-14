package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.dto.CareLevelRateCreateRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.CareLevelRateCreateResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.CareLevelRateResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.CareLevelRateUpdateRequest;
import com.nguyenquyen.mockproject_062026_group3.entity.CareLevel;
import com.nguyenquyen.mockproject_062026_group3.entity.CareLevelRate;
import com.nguyenquyen.mockproject_062026_group3.entity.Facility;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.repository.CareLevelRateRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.CareLevelRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.FacilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CareLevelRateService {

    @Autowired
    private CareLevelRateRepository careLevelRateRepository;

    @Autowired
    private CareLevelRepository careLevelRepository;

    @Autowired
    private FacilityRepository facilityRepository;

    @Transactional(readOnly = true)
    public List<CareLevelRateResponse> getCareLevelRates(Long facilityId, Long careLevelId, Boolean activeOnly) {
        boolean filterActive = activeOnly != null && activeOnly;
        List<CareLevelRate> rates = careLevelRateRepository.findRatesFiltered(
                facilityId, careLevelId, filterActive, LocalDate.now());
        return rates.stream()
                .map(CareLevelRateResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CareLevelRateCreateResponse createCareLevelRate(CareLevelRateCreateRequest request) {
        if (request.getCareLevelId() == null || request.getFacilityId() == null || 
            request.getDailyRate() == null || request.getEffectiveFrom() == null) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }

        if (request.getDailyRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }

        CareLevel careLevel = careLevelRepository.findById(request.getCareLevelId())
                .orElseThrow(() -> new AppException(ErrorCode.CARE_LEVEL_NOT_FOUND));

        Facility facility = facilityRepository.findById(request.getFacilityId())
                .orElseThrow(() -> new AppException(ErrorCode.FACILITY_NOT_FOUND));

        // Find currently active rate (effective_to is null) for the same care level and facility
        Optional<CareLevelRate> activeRateOpt = careLevelRateRepository
                .findByFacilityIdAndCareLevelIdAndEffectiveToIsNull(request.getFacilityId(), request.getCareLevelId());

        CareLevelRateCreateResponse.PreviousRateClosed previousClosedDto = null;

        if (activeRateOpt.isPresent()) {
            CareLevelRate activeRate = activeRateOpt.get();
            // Validate: new effectiveFrom must be after previous effectiveFrom
            if (!request.getEffectiveFrom().isAfter(activeRate.getEffectiveFrom())) {
                throw new AppException(ErrorCode.BUSINESS_EXCEPTION);
            }
            // Auto-close previous rate: set effectiveTo to day before new effectiveFrom
            LocalDate closedTo = request.getEffectiveFrom().minusDays(1);
            activeRate.setEffectiveTo(closedTo);
            careLevelRateRepository.save(activeRate);

            previousClosedDto = CareLevelRateCreateResponse.PreviousRateClosed.builder()
                    .id(activeRate.getId())
                    .effectiveTo(closedTo)
                    .build();
        }

        CareLevelRate newRate = CareLevelRate.builder()
                .careLevel(careLevel)
                .facility(facility)
                .dailyRate(request.getDailyRate())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(null)
                .build();

        newRate = careLevelRateRepository.save(newRate);

        return CareLevelRateCreateResponse.builder()
                .newRate(CareLevelRateResponse.fromEntity(newRate))
                .previousRateClosed(previousClosedDto)
                .build();
    }

    @Transactional(readOnly = true)
    public List<CareLevelRateResponse> getCurrentCareLevelRates(Long facilityId) {
        if (facilityId == null) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }
        
        // Ensure facility exists
        if (!facilityRepository.existsById(facilityId)) {
            throw new AppException(ErrorCode.FACILITY_NOT_FOUND);
        }

        List<CareLevelRate> rates = careLevelRateRepository.findByFacilityIdAndEffectiveToIsNull(facilityId);
        return rates.stream()
                .map(CareLevelRateResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CareLevelRateResponse updateCareLevelRate(Long id, CareLevelRateUpdateRequest request) {
        if (request.getDailyRate() == null || request.getDailyRate().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }

        CareLevelRate rate = careLevelRateRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // Constraint: Only allowed if effectiveFrom is in the future
        if (!rate.getEffectiveFrom().isAfter(LocalDate.now())) {
            throw new AppException(ErrorCode.BUSINESS_EXCEPTION);
        }

        rate.setDailyRate(request.getDailyRate());
        rate = careLevelRateRepository.save(rate);
        return CareLevelRateResponse.fromEntity(rate);
    }

    @Transactional
    public boolean deleteCareLevelRate(Long id) {
        CareLevelRate rate = careLevelRateRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // Constraint: Hard-delete only allowed if effectiveFrom is in the future
        if (!rate.getEffectiveFrom().isAfter(LocalDate.now())) {
            throw new AppException(ErrorCode.BUSINESS_EXCEPTION);
        }

        careLevelRateRepository.delete(rate);
        return true;
    }
}
