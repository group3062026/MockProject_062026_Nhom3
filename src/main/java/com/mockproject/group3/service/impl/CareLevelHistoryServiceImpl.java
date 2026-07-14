package com.mockproject.group3.service.impl;

import com.mockproject.group3.dto.carelevelhistory.CareLevelActiveSummaryResponse;
import com.mockproject.group3.dto.carelevelhistory.CareLevelHistoryResponse;
import com.mockproject.group3.dto.carelevelhistory.TransitionCareLevelRequest;
import com.mockproject.group3.dto.carelevelhistory.UpdateCareLevelHistoryRequest;
import com.mockproject.group3.entity.CareLevel;
import com.mockproject.group3.entity.Resident;
import com.mockproject.group3.entity.ResidentCareLevelHistory;
import com.mockproject.group3.exception.BusinessLogicException;
import com.mockproject.group3.exception.ResourceNotFoundException;
import com.mockproject.group3.repository.CareLevelRepository;
import com.mockproject.group3.repository.ResidentCareLevelHistoryRepository;
import com.mockproject.group3.repository.ResidentRepository;
import com.mockproject.group3.service.CareLevelHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareLevelHistoryServiceImpl implements CareLevelHistoryService {

    private final ResidentCareLevelHistoryRepository historyRepository;
    private final ResidentRepository residentRepository;
    private final CareLevelRepository careLevelRepository;

    @Override
    public List<CareLevelHistoryResponse> getResidentCareLevelHistory(Long residentId) {
        if (!residentRepository.existsById(residentId)) {
            throw new ResourceNotFoundException("Resident not found with id: " + residentId);
        }

        List<ResidentCareLevelHistory> history = historyRepository.findByResidentIdOrderByStartDateDesc(residentId);
        
        return history.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CareLevelHistoryResponse transitionResidentCareLevel(Long residentId, TransitionCareLevelRequest request) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found"));

        CareLevel newCareLevel = careLevelRepository.findById(request.getCareLevelId())
                .orElseThrow(() -> new ResourceNotFoundException("Care level not found"));

        // Find current record
        historyRepository.findByResidentIdAndEndDateIsNull(residentId).ifPresent(currentRecord -> {
            if (!request.getStartDate().isAfter(currentRecord.getStartDate())) {
                throw new BusinessLogicException("New start date must be after the current record's start date");
            }
            if (currentRecord.getCareLevel().getId().equals(request.getCareLevelId())) {
                throw new BusinessLogicException("Resident is already at this care level");
            }
            // Close current record
            currentRecord.setEndDate(request.getStartDate());
            historyRepository.save(currentRecord);
        });

        // Create new record
        ResidentCareLevelHistory newRecord = ResidentCareLevelHistory.builder()
                .resident(resident)
                .careLevel(newCareLevel)
                .startDate(request.getStartDate())
                .build();

        return mapToResponse(historyRepository.save(newRecord));
    }

    @Override
    public List<CareLevelActiveSummaryResponse> getCareLevelActiveSummary(Long facilityId) {
        List<Object[]> results = historyRepository.countActiveResidentsByCareLevelForFacility(facilityId);
        
        return results.stream().map(row -> CareLevelActiveSummaryResponse.builder()
                .levelCode((String) row[0])
                .activeResidentCount((Long) row[1])
                .build()).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CareLevelHistoryResponse updateCareLevelHistory(Long id, UpdateCareLevelHistoryRequest request) {
        ResidentCareLevelHistory history = historyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("History record not found"));

        if (history.getEndDate() == null) {
            throw new BusinessLogicException("Cannot edit the current active record. Use transition instead.");
        }

        if (request.getCareLevelId() != null) {
            CareLevel newCareLevel = careLevelRepository.findById(request.getCareLevelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Care level not found"));
            history.setCareLevel(newCareLevel);
        }

        if (request.getStartDate() != null) {
            // Further date validation could be added here to prevent overlapping history
            history.setStartDate(request.getStartDate());
        }

        log.info("History record {} updated. Reason: {}", id, request.getReason());

        return mapToResponse(historyRepository.save(history));
    }

    private CareLevelHistoryResponse mapToResponse(ResidentCareLevelHistory history) {
        return CareLevelHistoryResponse.builder()
                .id(history.getId())
                .careLevelId(history.getCareLevel().getId())
                .levelCode(history.getCareLevel().getLevelCode())
                .startDate(history.getStartDate())
                .endDate(history.getEndDate())
                .build();
    }
}

