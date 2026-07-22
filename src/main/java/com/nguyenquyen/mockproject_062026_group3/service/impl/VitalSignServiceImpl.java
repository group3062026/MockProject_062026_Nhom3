package com.nguyenquyen.mockproject_062026_group3.service.impl;

import com.nguyenquyen.mockproject_062026_group3.common.SecurityUtils;
import com.nguyenquyen.mockproject_062026_group3.dto.request.RecordVitalsRequestDTO;
import com.nguyenquyen.mockproject_062026_group3.entity.CareTask;
import com.nguyenquyen.mockproject_062026_group3.entity.VitalSign;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.repository.CareTaskRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.ResidentRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.UserRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.VitalSignRepository;
import com.nguyenquyen.mockproject_062026_group3.service.VitalSignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
//sc-033
@Service
@RequiredArgsConstructor
@Slf4j
public class VitalSignServiceImpl implements VitalSignService {
    @Autowired
    private final VitalSignRepository vitalSignRepository;
    @Autowired
    private final CareTaskRepository careTaskRepository;
    @Autowired
    private final ResidentRepository residentRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final ModelMapper modelMapper;
    @Autowired
    private final SecurityUtils securityUtils;
    @Transactional
     @Override
     public void recordVitalsAndCompleteTask(RecordVitalsRequestDTO request) {

         Long currentUserId = securityUtils.getCurrentUser().getId();

        // CHECK FOR ABNORMAL THRESHOLDS
         boolean isAbnormal = checkAbnormalVitals(request);

        // Save the record to the VITAL_SIGNS table
         VitalSign vs = modelMapper.map(request, VitalSign.class);
         vs.setResident(residentRepository.getReferenceById(request.getResidentId())); // Tối ưu: Chỉ lấy ID thay vì query nguyên object
         vs.setRecordedBy(userRepository.getReferenceById(currentUserId));
         vitalSignRepository.save(vs);

        // TASK UPDATE
         CareTask task = careTaskRepository.findById(request.getTaskId())
                 .orElseThrow(() -> new AppException(ErrorCode.CARE_PLAN_NOT_FOUND));

         task.setStatus("COMPLETED");
         task.setCompletedAt(OffsetDateTime.now());

        // If there are any problems with the vital signs, turn on the red flag for Task
         if (isAbnormal) {
             task.setIsAbnormalFlagged(true);
             log.warn("CẢNH BÁO MỨC ĐỘ CAO: Bệnh nhân ID {} có sinh hiệu bất thường! Gửi thông báo cho RN...", request.getResidentId());

         }


    }


    //Logic for checking basic medical indicators
    private boolean checkAbnormalVitals(RecordVitalsRequestDTO dto) {
        boolean abnormal = false;

        //SpO2 below 92% is a red alert.
        if (dto.getSpo2Percentage() != null && dto.getSpo2Percentage() < 92) {
            abnormal = true;
        }
        // Blood pressure is too high or too low
        if (dto.getBloodPressureSystolic() != null &&
                (dto.getBloodPressureSystolic() > 160 || dto.getBloodPressureSystolic() < 90)) {
            abnormal = true;
        }
        //Heart rate
        if (dto.getHeartRateBpm() != null &&
                (dto.getHeartRateBpm() > 110 || dto.getHeartRateBpm() < 50)) {
            abnormal = true;
        }
        // Fever (Above 100.4 F)
        if (dto.getTemperatureFahrenheit() != null && dto.getTemperatureFahrenheit() > 100.4) {
            abnormal = true;
        }

        return abnormal;
    }
}
