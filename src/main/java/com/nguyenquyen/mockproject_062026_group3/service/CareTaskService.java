package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.dto.request.TaskStatusUpdateRequestDTO;
import com.nguyenquyen.mockproject_062026_group3.dto.response.CareTaskResponseDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


public interface CareTaskService {
    CareTaskResponseDTO getCareTasks( LocalDate Date);
    void updateTaskStatus(Long taskId, TaskStatusUpdateRequestDTO request);
}
