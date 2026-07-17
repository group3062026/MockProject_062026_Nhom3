package com.nguyenquyen.mockproject_062026_group3.service.impl;

import com.nguyenquyen.mockproject_062026_group3.dto.admin.carelevel.CareLevelRateResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.carelevel.CareLevelResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.carelevel.CreateCareLevelRateRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.carelevel.UpdateCareLevelRateRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.carelevel.UpdateCareLevelRequest;
import com.nguyenquyen.mockproject_062026_group3.entity.CareLevel;
import com.nguyenquyen.mockproject_062026_group3.entity.CareLevelRate;
import com.nguyenquyen.mockproject_062026_group3.entity.Facility;
import com.nguyenquyen.mockproject_062026_group3.repository.CareLevelRateRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.CareLevelRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.FacilityRepository;
import com.nguyenquyen.mockproject_062026_group3.service.AdminCareLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCareLevelServiceImpl implements AdminCareLevelService {

    private final CareLevelRepository careLevelRepository;
    private final CareLevelRateRepository careLevelRateRepository;
    private final FacilityRepository facilityRepository;

    private static final Long FACILITY_ID = 1L;

    @Override
    @Transactional(readOnly = true)
    public List<CareLevelResponse> getCareLevel() {
        return careLevelRepository.findAllByIsDeletedFalse().stream()
                .map(cl -> CareLevelResponse.builder()
                        .id(cl.getId())
                        .levelCode(cl.getLevelCode())
                        .levelName(cl.getLevelName())
                        .isDeleted(cl.getIsDeleted())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CareLevelResponse updateCareLevelChange(Long careLevelId, UpdateCareLevelRequest request) {
        CareLevel cl = careLevelRepository.findById(careLevelId)
                .orElseThrow(() -> new RuntimeException("Care level not found"));
        
        if (request.getIsDeleted() != null) {
            cl.setIsDeleted(request.getIsDeleted());
            cl = careLevelRepository.save(cl);
        }
        
        return CareLevelResponse.builder()
                .id(cl.getId())
                .levelCode(cl.getLevelCode())
                .levelName(cl.getLevelName())
                .isDeleted(cl.getIsDeleted())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CareLevelRateResponse> getLOCRate(Long careLevelId) {
        List<CareLevelRate> rates = careLevelRateRepository.findByCareLevelId(careLevelId);
        return rates.stream().map(this::mapToRateResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CareLevelRateResponse createLOCRate(CreateCareLevelRateRequest request) {
        CareLevel cl = careLevelRepository.findById(request.getCareLevelId())
                .orElseThrow(() -> new RuntimeException("Care level not found"));
        
        Facility facility = facilityRepository.findById(FACILITY_ID)
                .orElseThrow(() -> new RuntimeException("Facility not found"));

        CareLevelRate rate = CareLevelRate.builder()
                .careLevel(cl)
                .facility(facility)
                .dailyRate(request.getDailyRate())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .build();
        
        rate = careLevelRateRepository.save(rate);
        return mapToRateResponse(rate);
    }

    @Override
    @Transactional
    public CareLevelRateResponse updateLOCRate(Long rateId, UpdateCareLevelRateRequest request) {
        CareLevelRate rate = careLevelRateRepository.findById(rateId)
                .orElseThrow(() -> new RuntimeException("Rate not found"));
        
        if (request.getCareLevelId() != null) {
            CareLevel cl = careLevelRepository.findById(request.getCareLevelId())
                    .orElseThrow(() -> new RuntimeException("Care level not found"));
            rate.setCareLevel(cl);
        }
        if (request.getDailyRate() != null) rate.setDailyRate(request.getDailyRate());
        if (request.getEffectiveFrom() != null) rate.setEffectiveFrom(request.getEffectiveFrom());
        if (request.getEffectiveTo() != null) rate.setEffectiveTo(request.getEffectiveTo());
        
        rate = careLevelRateRepository.save(rate);
        return mapToRateResponse(rate);
    }

    @Override
    @Transactional
    public void deleteLOCRate(Long rateId) {
        CareLevelRate rate = careLevelRateRepository.findById(rateId)
                .orElseThrow(() -> new RuntimeException("Rate not found"));
        // Check if currently effective (just simple check)
        LocalDate now = LocalDate.now();
        if (rate.getEffectiveFrom().isBefore(now) && (rate.getEffectiveTo() == null || rate.getEffectiveTo().isAfter(now))) {
            throw new RuntimeException("Cannot delete currently effective rate");
        }
        careLevelRateRepository.delete(rate);
    }

    @Override
    @Transactional
    public List<CareLevelRateResponse> seedSampleLOCRate() {
        // Mock seeding logic for API #25
        List<CareLevelRate> rates = new ArrayList<>();
        // In real app, we would clear and seed
        return rates.stream().map(this::mapToRateResponse).collect(Collectors.toList());
    }

    private CareLevelRateResponse mapToRateResponse(CareLevelRate rate) {
        return CareLevelRateResponse.builder()
                .id(rate.getId())
                .careLevelId(rate.getCareLevel() != null ? rate.getCareLevel().getId() : null)
                .facilityId(rate.getFacility() != null ? rate.getFacility().getId() : null)
                .dailyRate(rate.getDailyRate())
                .effectiveFrom(rate.getEffectiveFrom())
                .effectiveTo(rate.getEffectiveTo())
                .build();
    }
}
