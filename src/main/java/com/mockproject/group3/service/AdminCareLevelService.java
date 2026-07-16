package com.mockproject.group3.service;

import com.mockproject.group3.dto.admin.carelevel.CareLevelRateResponse;
import com.mockproject.group3.dto.admin.carelevel.CareLevelResponse;
import com.mockproject.group3.dto.admin.carelevel.CreateCareLevelRateRequest;
import com.mockproject.group3.dto.admin.carelevel.UpdateCareLevelRateRequest;
import com.mockproject.group3.dto.admin.carelevel.UpdateCareLevelRequest;

import java.util.List;

public interface AdminCareLevelService {
    List<CareLevelResponse> getCareLevel();
    CareLevelResponse updateCareLevelChange(Long careLevelId, UpdateCareLevelRequest request);
    
    List<CareLevelRateResponse> getLOCRate(Long careLevelId);
    CareLevelRateResponse createLOCRate(CreateCareLevelRateRequest request);
    CareLevelRateResponse updateLOCRate(Long rateId, UpdateCareLevelRateRequest request);
    void deleteLOCRate(Long rateId);
    List<CareLevelRateResponse> seedSampleLOCRate();
}
