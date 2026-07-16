package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;

import com.nguyenquyen.mockproject_062026_group3.dto.request.TaskStatusUpdateRequestDTO;
import com.nguyenquyen.mockproject_062026_group3.dto.response.CareTaskResponseDTO;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.service.CareTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
//sc-032
@RestController
@RequestMapping("api/v1/care-task")
public class CareTaskController {
    @Autowired
    private CareTaskService careTaskService;
    @GetMapping("/tasks")
    public ApiResponse<CareTaskResponseDTO> getCareTask(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate localDate) {
    LocalDate date = (localDate!=null) ?  localDate : LocalDate.now();
    CareTaskResponseDTO careTaskResponseDTO = careTaskService.getCareTasks( date);
    ApiResponse<CareTaskResponseDTO> response =  ApiResponse.<CareTaskResponseDTO>builder().data(careTaskResponseDTO)
                                                            .code(ErrorCode.SUCCESS.getCode()).message(ErrorCode.SUCCESS.getMessage()).build();
    return response;

    }
    @PatchMapping("/statustasks/{taskId}")
    public ApiResponse<Void> getCareTask(@PathVariable Long taskId, @RequestBody TaskStatusUpdateRequestDTO taskStatusUpdateRequestDTO) {

        careTaskService.updateTaskStatus(taskId,taskStatusUpdateRequestDTO);
        ApiResponse<Void> response =  ApiResponse.<Void>builder().data(null)
                .code(ErrorCode.SUCCESS.getCode()).message(ErrorCode.SUCCESS.getMessage()).build();
        return response;

    }
}
