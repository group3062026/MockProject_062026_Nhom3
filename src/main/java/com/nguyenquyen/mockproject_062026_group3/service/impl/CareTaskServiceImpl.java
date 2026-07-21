package com.nguyenquyen.mockproject_062026_group3.service.impl;

import com.nguyenquyen.mockproject_062026_group3.dto.request.TaskStatusUpdateRequestDTO;
import com.nguyenquyen.mockproject_062026_group3.dto.response.CareTaskResponseDTO;
import com.nguyenquyen.mockproject_062026_group3.entity.CareTask;
import com.nguyenquyen.mockproject_062026_group3.entity.Resident;
import com.nguyenquyen.mockproject_062026_group3.entity.Shift;
import com.nguyenquyen.mockproject_062026_group3.entity.ShiftAssignment;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.repository.CareTaskRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.ShiftAssignmentRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.UserRepository;
import com.nguyenquyen.mockproject_062026_group3.service.CareTaskService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
//sc-032
    @Service
    @RequiredArgsConstructor
    @Slf4j
    @Transactional
    public class CareTaskServiceImpl implements CareTaskService {
        @Autowired
        private final CareTaskRepository careTaskRepository;
        @Autowired
        private final ShiftAssignmentRepository shiftAssignmentRepository;
        @Autowired
        private final UserRepository userRepository;
        @Override

        public CareTaskResponseDTO getCareTasks( LocalDate date) {

            // Lấy ID của điều dưỡng đang đăng nhập
            Long currentCnaId = getCurrentUserId();

            LocalDate queryDate = (date != null) ? date : LocalDate.now();

            // TÌM CA LÀM VIỆC CỦA HỘ LÝ
            ShiftAssignment assignment = shiftAssignmentRepository.findConfirmedShiftForUser(currentCnaId, queryDate)
                    .orElseThrow(() -> new AppException(ErrorCode.SHIFT_ASSIGNMENT_NOT_FOUND));

            //  LẤY GIỜ BẮT ĐẦU VÀ KẾT THÚC TỪ DATABASE
            Shift shift = assignment.getShift();
            LocalTime dbStartTime = shift.getStartTime(); // VD: 06:00
            LocalTime dbEndTime = shift.getEndTime();     // VD: 14:00

            OffsetDateTime startTime = OffsetDateTime.of(queryDate, dbStartTime, ZoneOffset.ofHours(7));
            OffsetDateTime endTime = OffsetDateTime.of(queryDate, dbEndTime, ZoneOffset.ofHours(7));

            // XỬ LÝ CA ĐÊM (NIGHT SHIFT):
            // Nếu ca đêm bắt đầu lúc 22:00 và kết thúc lúc 06:00 sáng hôm sau.
            // Giờ kết thúc sẽ NHỎ HƠN giờ bắt đầu, ta phải cộng thêm 1 ngày cho endTime.
            if (endTime.isBefore(startTime)) {
                endTime = endTime.plusDays(1);
            }


            List<CareTask> flatTasks = careTaskRepository.findTasksForShift(currentCnaId, startTime, endTime);

            // Tính toán Progress (Hoàn thành / Tổng số)
            int totalTasks = flatTasks.size();
            int completedTasks = (int) flatTasks.stream()
                    .filter(t -> "COMPLETED".equals(t.getStatus()))
                    .count();

            // Gom nhóm bằng Java Stream API (Khóa chính là Object Resident)
            Map<Resident, List<CareTask>> tasksByResident = flatTasks.stream()
                    .collect(Collectors.groupingBy(t -> t.getCareIntervention().getCarePlan().getResident()));

            // Map dữ liệu vào DTO để trả về cho Controller
            List<CareTaskResponseDTO.ResidentTasksDTO> residentDTOs = tasksByResident.entrySet().stream()
                    .map(entry -> {
                        Resident resident = entry.getKey();
                        List<CareTask> tasks = entry.getValue();

                        return CareTaskResponseDTO.ResidentTasksDTO.builder()
                                .residentId(resident.getId())
                                .name(resident.getLastName() + " " + resident.getFirstName())
                                .planStatus(resident.getStatus())
                                // Map từng CareTask Entity sang TaskItem DTO
                                .tasks(tasks.stream().map(this::mapToTaskItemDTO).collect(Collectors.toList()))
                                .build();
                    })
                    .collect(Collectors.toList());

            return CareTaskResponseDTO.builder()
                    .shiftProgress(CareTaskResponseDTO.ShiftProgressDTO.builder()
                            .completed(completedTasks)
                            .total(totalTasks)
                            .build())
                    .residents(residentDTOs)
                    .build();
        }

        @Override

        public void updateTaskStatus(Long taskId, TaskStatusUpdateRequestDTO request) {
            // Tìm Task, nếu không thấy thì ném lỗi
            CareTask task = careTaskRepository.findById(taskId)
                    .orElseThrow(() -> new AppException(ErrorCode.CARE_PLAN_NOT_FOUND));

            // Cập nhật trạng thái
            task.setStatus(request.getStatus());

            // Nếu đánh dấu hoàn thành, lưu lại thời gian hoàn thành chính xác
            if ("COMPLETED".equals(request.getStatus())) {
                task.setCompletedAt(OffsetDateTime.now());
            }

            // Gắn cờ cảnh báo
            if (Boolean.TRUE.equals(request.getIsAbnormalFlagged())) {
                task.setIsAbnormalFlagged(true);
                log.warn("CẢNH BÁO: Bệnh nhân có dấu hiệu bất thường ");
            }


        }


        // CÁC HÀM PHỤ TRỢ
        private Long getCurrentUserId() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
            String email = authentication.getName();
            return userRepository.findByEmailAndIsDeletedFalse(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND))
                    .getId();
        }


        private CareTaskResponseDTO.TaskItemDTO mapToTaskItemDTO(CareTask task) {
            return CareTaskResponseDTO.TaskItemDTO.builder()
                    .taskId(task.getId())
                    .taskName(task.getTaskType())
                    .dueTime(task.getScheduledTime().toLocalTime().toString())
                    .status(task.getStatus())
                    .hasAbnormalAlert(task.getIsAbnormalFlagged())
                    .build();
        }
    }

