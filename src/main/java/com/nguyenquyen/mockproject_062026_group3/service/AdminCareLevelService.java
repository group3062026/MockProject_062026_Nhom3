package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.dto.admin.carelevel.CareLevelRateResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.carelevel.CareLevelResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.carelevel.CreateCareLevelRateRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.carelevel.UpdateCareLevelRateRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.admin.carelevel.UpdateCareLevelRequest;

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
