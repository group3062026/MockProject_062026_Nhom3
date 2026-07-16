package com.mockproject.group3.service.impl;

import com.mockproject.group3.dto.admin.staffing.StaffingConfigResponse;
import com.mockproject.group3.dto.admin.staffing.UpdateStaffingConfigRequest;
import com.mockproject.group3.entity.Facility;
import com.mockproject.group3.entity.StaffingConfig;
import com.mockproject.group3.repository.FacilityRepository;
import com.mockproject.group3.repository.StaffingConfigRepository;
import com.mockproject.group3.service.AdminStaffingConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminStaffingConfigServiceImpl implements AdminStaffingConfigService {

    private final StaffingConfigRepository staffingConfigRepository;
    private final FacilityRepository facilityRepository;

    private static final Long FACILITY_ID = 1L; // Assuming single facility context for MVP

    @Override
    @Transactional(readOnly = true)
    public StaffingConfigResponse getStaffingRatio() {
        StaffingConfig config = staffingConfigRepository.findByFacilityId(FACILITY_ID)
                .orElseThrow(() -> new RuntimeException("Staffing config not found for facility"));
        return mapToResponse(config);
    }

    @Override
    @Transactional
    public StaffingConfigResponse updateStaffingRatio(UpdateStaffingConfigRequest request) {
        StaffingConfig config = staffingConfigRepository.findByFacilityId(FACILITY_ID)
                .orElse(null);
        
        if (config == null) {
            Facility facility = facilityRepository.findById(FACILITY_ID)
                    .orElseThrow(() -> new RuntimeException("Facility not found"));
            config = StaffingConfig.builder()
                    .facility(facility)
                    .build();
        }

        config.setMinHrsPerResidentDay(request.getMinHrsPerResidentDay());
        config.setWarnBelowPercentage(request.getWarnBelowPercentage());

        config = staffingConfigRepository.save(config);
        return mapToResponse(config);
    }

    private StaffingConfigResponse mapToResponse(StaffingConfig config) {
        return StaffingConfigResponse.builder()
                .id(config.getId())
                .facilityId(config.getFacility() != null ? config.getFacility().getId() : null)
                .minHrsPerResidentDay(config.getMinHrsPerResidentDay())
                .warnBelowPercentage(config.getWarnBelowPercentage())
                .build();
    }
}
