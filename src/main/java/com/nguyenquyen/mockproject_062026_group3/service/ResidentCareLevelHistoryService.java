package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.common.AuditLoggingHelper;
import com.nguyenquyen.mockproject_062026_group3.dto.CareLevelActiveSummaryResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.CareLevelHistoryResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.CareLevelHistoryTransitionRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.CareLevelHistoryUpdateRequest;
import com.nguyenquyen.mockproject_062026_group3.entity.CareLevel;
import com.nguyenquyen.mockproject_062026_group3.entity.Resident;
import com.nguyenquyen.mockproject_062026_group3.entity.ResidentCareLevelHistory;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.repository.CareLevelRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.ResidentCareLevelHistoryRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.ResidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ResidentCareLevelHistoryService {

    @Autowired
    private ResidentCareLevelHistoryRepository historyRepository;

    @Autowired
    private ResidentRepository residentRepository;

    @Autowired
    private CareLevelRepository careLevelRepository;

    @Autowired
    private AuditLoggingHelper auditLoggingHelper;

    @Transactional(readOnly = true)
    public List<CareLevelHistoryResponse> getCareLevelHistory(Long residentId) {
        residentRepository.findById(residentId)
                .filter(r -> !r.getIsDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.RESIDENT_NOT_FOUND));

        List<ResidentCareLevelHistory> history = historyRepository.findByResidentIdOrderByStartDateDesc(residentId);
        return history.stream()
                .map(CareLevelHistoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> transitionCareLevel(Long residentId, CareLevelHistoryTransitionRequest request) {
        if (request.getCareLevelId() == null || request.getStartDate() == null) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }

        Resident resident = residentRepository.findById(residentId)
                .filter(r -> !r.getIsDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.RESIDENT_NOT_FOUND));

        CareLevel careLevel = careLevelRepository.findById(request.getCareLevelId())
                .orElseThrow(() -> new AppException(ErrorCode.CARE_LEVEL_NOT_FOUND));

        // Tìm record hiện tại (endDate is null) và đóng lại
        Optional<ResidentCareLevelHistory> currentOpt = historyRepository.findByResidentIdAndEndDateIsNull(residentId);

        Map<String, Object> result = new HashMap<>();
        CareLevelHistoryResponse closedDto = null;

        if (currentOpt.isPresent()) {
            ResidentCareLevelHistory current = currentOpt.get();
            // startDate mới phải sau startDate record hiện tại
            if (!request.getStartDate().isAfter(current.getStartDate())) {
                throw new AppException(ErrorCode.BUSINESS_EXCEPTION);
            }
            current.setEndDate(request.getStartDate());
            historyRepository.save(current);

            closedDto = CareLevelHistoryResponse.fromEntity(current);
        }

        ResidentCareLevelHistory newRecord = ResidentCareLevelHistory.builder()
                .resident(resident)
                .careLevel(careLevel)
                .startDate(request.getStartDate())
                .endDate(null)
                .build();

        newRecord = historyRepository.save(newRecord);

        result.put("newRecord", CareLevelHistoryResponse.fromEntity(newRecord));
        if (closedDto != null) {
            result.put("closedRecord", closedDto);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<CareLevelActiveSummaryResponse> getActiveSummary(Long facilityId) {
        if (facilityId == null) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }

        List<Object[]> rows = historyRepository.countActiveResidentsByCareLevelForFacility(facilityId);
        return rows.stream()
                .map(row -> CareLevelActiveSummaryResponse.builder()
                        .levelCode((String) row[0])
                        .activeResidentCount((Long) row[1])
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public CareLevelHistoryResponse updateCareLevelHistory(Long id, CareLevelHistoryUpdateRequest request) {
        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        }

        ResidentCareLevelHistory history = historyRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CARE_LEVEL_HISTORY_NOT_FOUND));

        // Chỉ cho sửa record không phải current (endDate != null)
        if (history.getEndDate() == null) {
            throw new AppException(ErrorCode.BUSINESS_EXCEPTION);
        }

        String oldData = "careLevelId=" + history.getCareLevel().getId() + ", startDate=" + history.getStartDate();

        if (request.getCareLevelId() != null) {
            CareLevel careLevel = careLevelRepository.findById(request.getCareLevelId())
                    .orElseThrow(() -> new AppException(ErrorCode.CARE_LEVEL_NOT_FOUND));
            history.setCareLevel(careLevel);
        }

        if (request.getStartDate() != null) {
            history.setStartDate(request.getStartDate());
        }

        history = historyRepository.save(history);

        auditLoggingHelper.logAudit("resident_care_level_history", id.toString(), "UPDATE",
                oldData, "Reason: " + request.getReason());

        return CareLevelHistoryResponse.fromEntity(history);
    }
}
