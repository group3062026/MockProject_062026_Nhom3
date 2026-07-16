package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.dto.request.RecordVitalsRequestDTO;

public interface VitalSignService {
    void recordVitalsAndCompleteTask(RecordVitalsRequestDTO request);
}
