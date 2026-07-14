package com.mockproject.group3.service;

import com.mockproject.group3.dto.carelevelhistory.CareLevelActiveSummaryResponse;
import com.mockproject.group3.dto.carelevelhistory.CareLevelHistoryResponse;
import com.mockproject.group3.dto.carelevelhistory.TransitionCareLevelRequest;
import com.mockproject.group3.dto.carelevelhistory.UpdateCareLevelHistoryRequest;

import java.util.List;

public interface CareLevelHistoryService {

    List<CareLevelHistoryResponse> getResidentCareLevelHistory(Long residentId);

    CareLevelHistoryResponse transitionResidentCareLevel(Long residentId, TransitionCareLevelRequest request);

    List<CareLevelActiveSummaryResponse> getCareLevelActiveSummary(Long facilityId);

    CareLevelHistoryResponse updateCareLevelHistory(Long id, UpdateCareLevelHistoryRequest request);
}

