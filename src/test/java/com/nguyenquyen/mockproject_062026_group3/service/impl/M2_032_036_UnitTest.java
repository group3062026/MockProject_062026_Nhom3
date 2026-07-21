package com.nguyenquyen.mockproject_062026_group3.service.impl;

import com.nguyenquyen.mockproject_062026_group3.common.SecurityUtils;
import com.nguyenquyen.mockproject_062026_group3.dto.request.IDTSignatureRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.RecordVitalsRequestDTO;
import com.nguyenquyen.mockproject_062026_group3.dto.request.TaskStatusUpdateRequestDTO;
import com.nguyenquyen.mockproject_062026_group3.dto.response.CareTaskResponseDTO;
import com.nguyenquyen.mockproject_062026_group3.dto.response.IDTSignatureResponse;
import com.nguyenquyen.mockproject_062026_group3.entity.*;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class M2_032_036_UnitTest {

    // ─── Shared mocks ────────────────────────────────────────────────────────

    @Mock SecurityUtils securityUtils;
    @Mock UserRepository userRepository;
    @Mock CarePlanRepository carePlanRepository;
    @Mock CareTaskRepository careTaskRepository;
    @Mock ShiftAssignmentRepository shiftAssignmentRepository;
    @Mock VitalSignRepository vitalSignRepository;
    @Mock ResidentRepository residentRepository;
    @Mock IDTSignatureRepository idtSignatureRepository;
    @Mock ModelMapper modelMapper;

    // ─── Services under test ─────────────────────────────────────────────────

    @InjectMocks CareTaskServiceImpl careTaskService;
    @InjectMocks VitalSignServiceImpl vitalSignService;
    @InjectMocks IDTSignatureServiceImpl idtSignatureService;

    // ─── Common fixtures ─────────────────────────────────────────────────────

    private User currentUser;
    private Role role;

    @BeforeEach
    void setUp() {
        role = Role.builder().id(1L).roleName("CNA").build();
        currentUser = User.builder()
                .id(13L)
                .email("cna@test.com")
                .firstName("John")
                .lastName("Doe")
                .role(role)
                .status("ACTIVE")
                .build();

        // Set up Spring SecurityContext
        var auth = new UsernamePasswordAuthenticationToken(
                new org.springframework.security.core.userdetails.User(
                        currentUser.getEmail(),
                        "password",
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_CNA"))
                ),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CNA"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // =========================================================================
    // CareTaskServiceImpl
    // =========================================================================

    @Nested
    @DisplayName("CareTaskServiceImpl")
    class CareTaskServiceTests {

        @Test
        @DisplayName("getCareTasks - trả về danh sách task theo ca làm việc của user hiện tại")
        void getCareTasks_returnsGroupedTasks() {
            when(securityUtils.getCurrentUser()).thenReturn(currentUser);

            Shift shift = Shift.builder()
                    .startTime(LocalTime.of(6, 0))
                    .endTime(LocalTime.of(14, 0))
                    .build();
            ShiftAssignment assignment = ShiftAssignment.builder().shift(shift).build();
            when(shiftAssignmentRepository.findConfirmedShiftForUser(13L, LocalDate.now()))
                    .thenReturn(Optional.of(assignment));

            Resident resident = Resident.builder().id(1L).firstName("Alice").lastName("Smith").status("ACTIVE").build();
            CarePlan carePlan = CarePlan.builder().resident(resident).build();
            CareIntervention intervention = CareIntervention.builder().carePlan(carePlan).build();
            CareTask task = CareTask.builder()
                    .id(1L)
                    .taskType("BATHING")
                    .status("PENDING")
                    .isAbnormalFlagged(false)
                    .scheduledTime(OffsetDateTime.now())
                    .careIntervention(intervention)
                    .build();

            when(careTaskRepository.findTasksForShift(eq(13L), any(), any()))
                    .thenReturn(List.of(task));

            CareTaskResponseDTO result = careTaskService.getCareTasks(null);

            assertThat(result).isNotNull();
            assertThat(result.getResidents()).hasSize(1);
            assertThat(result.getResidents().get(0).getResidentId()).isEqualTo(1L);
            assertThat(result.getShiftProgress().getTotal()).isEqualTo(1);
            assertThat(result.getShiftProgress().getCompleted()).isEqualTo(0);
        }

        @Test
        @DisplayName("getCareTasks - không tìm thấy ca làm việc thì ném SHIFT_ASSIGNMENT_NOT_FOUND")
        void getCareTasks_noShift_throwsException() {
            when(securityUtils.getCurrentUser()).thenReturn(currentUser);
            when(shiftAssignmentRepository.findConfirmedShiftForUser(13L, LocalDate.now()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> careTaskService.getCareTasks(null))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining(ErrorCode.SHIFT_ASSIGNMENT_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("getCareTasks - tính đúng số task COMPLETED trong shiftProgress")
        void getCareTasks_countsCompletedCorrectly() {
            when(securityUtils.getCurrentUser()).thenReturn(currentUser);

            Shift shift = Shift.builder()
                    .startTime(LocalTime.of(6, 0))
                    .endTime(LocalTime.of(14, 0))
                    .build();
            when(shiftAssignmentRepository.findConfirmedShiftForUser(13L, LocalDate.now()))
                    .thenReturn(Optional.of(ShiftAssignment.builder().shift(shift).build()));

            Resident resident = Resident.builder().id(2L).firstName("Bob").lastName("Lee").status("ACTIVE").build();
            CarePlan carePlan = CarePlan.builder().resident(resident).build();
            CareIntervention intervention = CareIntervention.builder().carePlan(carePlan).build();

            CareTask done = CareTask.builder().id(1L).taskType("MEDICATION").status("COMPLETED")
                    .isAbnormalFlagged(false).scheduledTime(OffsetDateTime.now()).careIntervention(intervention).build();
            CareTask pending = CareTask.builder().id(2L).taskType("BATHING").status("PENDING")
                    .isAbnormalFlagged(false).scheduledTime(OffsetDateTime.now()).careIntervention(intervention).build();

            when(careTaskRepository.findTasksForShift(eq(13L), any(), any()))
                    .thenReturn(List.of(done, pending));

            CareTaskResponseDTO result = careTaskService.getCareTasks(null);

            assertThat(result.getShiftProgress().getCompleted()).isEqualTo(1);
            assertThat(result.getShiftProgress().getTotal()).isEqualTo(2);
        }

        @Test
        @DisplayName("updateTaskStatus - cập nhật COMPLETED và set completedAt")
        void updateTaskStatus_completed_setsCompletedAt() {
            CareTask task = CareTask.builder().id(1L).status("PENDING").isAbnormalFlagged(false).build();
            when(careTaskRepository.findById(1L)).thenReturn(Optional.of(task));

            TaskStatusUpdateRequestDTO req = new TaskStatusUpdateRequestDTO("COMPLETED", false, null);
            careTaskService.updateTaskStatus(1L, req);

            assertThat(task.getStatus()).isEqualTo("COMPLETED");
            assertThat(task.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("updateTaskStatus - cập nhật MISSED không set completedAt")
        void updateTaskStatus_missed_noCompletedAt() {
            CareTask task = CareTask.builder().id(2L).status("PENDING").isAbnormalFlagged(false).build();
            when(careTaskRepository.findById(2L)).thenReturn(Optional.of(task));

            TaskStatusUpdateRequestDTO req = new TaskStatusUpdateRequestDTO("MISSED", false, "Resident unavailable");
            careTaskService.updateTaskStatus(2L, req);

            assertThat(task.getStatus()).isEqualTo("MISSED");
            assertThat(task.getCompletedAt()).isNull();
        }

        @Test
        @DisplayName("updateTaskStatus - bật isAbnormalFlagged khi truyền true")
        void updateTaskStatus_setsAbnormalFlag() {
            CareTask task = CareTask.builder().id(3L).status("PENDING").isAbnormalFlagged(false).build();
            when(careTaskRepository.findById(3L)).thenReturn(Optional.of(task));

            TaskStatusUpdateRequestDTO req = new TaskStatusUpdateRequestDTO("COMPLETED", true, "Abnormal reading");
            careTaskService.updateTaskStatus(3L, req);

            assertThat(task.getIsAbnormalFlagged()).isTrue();
        }

        @Test
        @DisplayName("updateTaskStatus - task không tồn tại thì ném exception")
        void updateTaskStatus_taskNotFound_throwsException() {
            when(careTaskRepository.findById(999L)).thenReturn(Optional.empty());

            TaskStatusUpdateRequestDTO req = new TaskStatusUpdateRequestDTO("COMPLETED", false, null);

            assertThatThrownBy(() -> careTaskService.updateTaskStatus(999L, req))
                    .isInstanceOf(AppException.class);
        }
    }

    // =========================================================================
    // VitalSignServiceImpl
    // =========================================================================

    @Nested
    @DisplayName("VitalSignServiceImpl")
    class VitalSignServiceTests {

        @Test
        @DisplayName("recordVitalsAndCompleteTask - lưu vital sign và mark task COMPLETED")
        void recordVitals_savesAndCompletesTask() {
            when(securityUtils.getCurrentUser()).thenReturn(currentUser);

            RecordVitalsRequestDTO req = new RecordVitalsRequestDTO();
            req.setTaskId(1L);
            req.setResidentId(1L);
            req.setBloodPressureSystolic(120);
            req.setBloodPressureDiastolic(80);
            req.setHeartRateBpm(72);
            req.setSpo2Percentage(98);
            req.setTemperatureFahrenheit(98.6);
            req.setRespiratoryRate(16);
            req.setPainScale(0);

            VitalSign vs = new VitalSign();
            when(modelMapper.map(req, VitalSign.class)).thenReturn(vs);
            when(residentRepository.getReferenceById(1L))
                    .thenReturn(Resident.builder().id(1L).build());
            when(userRepository.getReferenceById(13L)).thenReturn(currentUser);

            CareTask task = CareTask.builder().id(1L).status("PENDING").isAbnormalFlagged(false).build();
            when(careTaskRepository.findById(1L)).thenReturn(Optional.of(task));

            vitalSignService.recordVitalsAndCompleteTask(req);

            verify(vitalSignRepository).save(vs);
            assertThat(task.getStatus()).isEqualTo("COMPLETED");
            assertThat(task.getCompletedAt()).isNotNull();
            assertThat(task.getIsAbnormalFlagged()).isFalse();
        }

        @Test
        @DisplayName("recordVitalsAndCompleteTask - SpO2 < 92 thì bật cờ bất thường")
        void recordVitals_lowSpo2_setsAbnormalFlag() {
            when(securityUtils.getCurrentUser()).thenReturn(currentUser);

            RecordVitalsRequestDTO req = new RecordVitalsRequestDTO();
            req.setTaskId(2L);
            req.setResidentId(1L);
            req.setSpo2Percentage(88);
            req.setBloodPressureSystolic(120);
            req.setBloodPressureDiastolic(80);
            req.setHeartRateBpm(72);
            req.setTemperatureFahrenheit(98.6);
            req.setRespiratoryRate(16);
            req.setPainScale(0);

            VitalSign vs = new VitalSign();
            when(modelMapper.map(req, VitalSign.class)).thenReturn(vs);
            when(residentRepository.getReferenceById(1L)).thenReturn(Resident.builder().id(1L).build());
            when(userRepository.getReferenceById(13L)).thenReturn(currentUser);

            CareTask task = CareTask.builder().id(2L).status("PENDING").isAbnormalFlagged(false).build();
            when(careTaskRepository.findById(2L)).thenReturn(Optional.of(task));

            vitalSignService.recordVitalsAndCompleteTask(req);

            assertThat(task.getIsAbnormalFlagged()).isTrue();
        }

        @Test
        @DisplayName("recordVitalsAndCompleteTask - huyết áp tâm thu > 160 thì bật cờ bất thường")
        void recordVitals_highBP_setsAbnormalFlag() {
            when(securityUtils.getCurrentUser()).thenReturn(currentUser);

            RecordVitalsRequestDTO req = new RecordVitalsRequestDTO();
            req.setTaskId(3L);
            req.setResidentId(1L);
            req.setBloodPressureSystolic(180);
            req.setBloodPressureDiastolic(100);
            req.setHeartRateBpm(80);
            req.setSpo2Percentage(97);
            req.setTemperatureFahrenheit(98.6);
            req.setRespiratoryRate(16);
            req.setPainScale(0);

            VitalSign vs = new VitalSign();
            when(modelMapper.map(req, VitalSign.class)).thenReturn(vs);
            when(residentRepository.getReferenceById(1L)).thenReturn(Resident.builder().id(1L).build());
            when(userRepository.getReferenceById(13L)).thenReturn(currentUser);

            CareTask task = CareTask.builder().id(3L).status("PENDING").isAbnormalFlagged(false).build();
            when(careTaskRepository.findById(3L)).thenReturn(Optional.of(task));

            vitalSignService.recordVitalsAndCompleteTask(req);

            assertThat(task.getIsAbnormalFlagged()).isTrue();
        }

        @Test
        @DisplayName("recordVitalsAndCompleteTask - sốt > 100.4F thì bật cờ bất thường")
        void recordVitals_fever_setsAbnormalFlag() {
            when(securityUtils.getCurrentUser()).thenReturn(currentUser);

            RecordVitalsRequestDTO req = new RecordVitalsRequestDTO();
            req.setTaskId(4L);
            req.setResidentId(1L);
            req.setBloodPressureSystolic(120);
            req.setBloodPressureDiastolic(80);
            req.setHeartRateBpm(72);
            req.setSpo2Percentage(97);
            req.setTemperatureFahrenheit(101.5);
            req.setRespiratoryRate(16);
            req.setPainScale(0);

            VitalSign vs = new VitalSign();
            when(modelMapper.map(req, VitalSign.class)).thenReturn(vs);
            when(residentRepository.getReferenceById(1L)).thenReturn(Resident.builder().id(1L).build());
            when(userRepository.getReferenceById(13L)).thenReturn(currentUser);

            CareTask task = CareTask.builder().id(4L).status("PENDING").isAbnormalFlagged(false).build();
            when(careTaskRepository.findById(4L)).thenReturn(Optional.of(task));

            vitalSignService.recordVitalsAndCompleteTask(req);

            assertThat(task.getIsAbnormalFlagged()).isTrue();
        }

        @Test
        @DisplayName("recordVitalsAndCompleteTask - task không tồn tại thì ném exception")
        void recordVitals_taskNotFound_throwsException() {
            when(securityUtils.getCurrentUser()).thenReturn(currentUser);

            RecordVitalsRequestDTO req = new RecordVitalsRequestDTO();
            req.setTaskId(999L);
            req.setResidentId(1L);
            req.setBloodPressureSystolic(120);
            req.setBloodPressureDiastolic(80);
            req.setHeartRateBpm(72);
            req.setSpo2Percentage(97);
            req.setTemperatureFahrenheit(98.6);
            req.setRespiratoryRate(16);
            req.setPainScale(0);

            VitalSign vs = new VitalSign();
            when(modelMapper.map(req, VitalSign.class)).thenReturn(vs);
            when(residentRepository.getReferenceById(1L)).thenReturn(Resident.builder().id(1L).build());
            when(userRepository.getReferenceById(13L)).thenReturn(currentUser);
            when(careTaskRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> vitalSignService.recordVitalsAndCompleteTask(req))
                    .isInstanceOf(AppException.class);
        }

        @Test
        @DisplayName("recordVitalsAndCompleteTask - lấy đúng user đang đăng nhập (không hardcode)")
        void recordVitals_usesCurrentUserNotHardcoded() {
            User anotherUser = User.builder().id(99L).email("nurse@test.com")
                    .firstName("Jane").lastName("Smith").role(role).status("ACTIVE").build();
            when(securityUtils.getCurrentUser()).thenReturn(anotherUser);

            RecordVitalsRequestDTO req = new RecordVitalsRequestDTO();
            req.setTaskId(5L);
            req.setResidentId(1L);
            req.setBloodPressureSystolic(120);
            req.setBloodPressureDiastolic(80);
            req.setHeartRateBpm(72);
            req.setSpo2Percentage(97);
            req.setTemperatureFahrenheit(98.6);
            req.setRespiratoryRate(16);
            req.setPainScale(0);

            VitalSign vs = new VitalSign();
            when(modelMapper.map(req, VitalSign.class)).thenReturn(vs);
            when(residentRepository.getReferenceById(1L)).thenReturn(Resident.builder().id(1L).build());
            when(userRepository.getReferenceById(99L)).thenReturn(anotherUser);

            CareTask task = CareTask.builder().id(5L).status("PENDING").isAbnormalFlagged(false).build();
            when(careTaskRepository.findById(5L)).thenReturn(Optional.of(task));

            vitalSignService.recordVitalsAndCompleteTask(req);

            // Xác nhận getReferenceById được gọi với ID 99 (user đăng nhập), không phải 13
            verify(userRepository).getReferenceById(99L);
            verify(userRepository, never()).getReferenceById(13L);
        }
    }

    // =========================================================================
    // IDTSignatureServiceImpl
    // =========================================================================

    @Nested
    @DisplayName("IDTSignatureServiceImpl")
    class IDTSignatureServiceTests {

        private CarePlan carePlan;
        private Role physicianRole;
        private User physician;

        @BeforeEach
        void setUpIDT() {
            physicianRole = Role.builder().id(2L).roleName("PHYSICIAN").build();
            physician = User.builder()
                    .id(5L).email("dr@test.com")
                    .firstName("Alan").lastName("Cho")
                    .role(physicianRole).status("ACTIVE")
                    .build();
            carePlan = CarePlan.builder().id(1L).status("PENDING_REVIEW").build();
        }

        @Test
        @DisplayName("getIDTSignatures - trả về danh sách signatures của care plan")
        void getIDTSignatures_returnsList() {
            when(carePlanRepository.findById(1L)).thenReturn(Optional.of(carePlan));

            IDTSignature sig = IDTSignature.builder()
                    .id(1L).carePlan(carePlan).user(physician)
                    .comments("Acknowledged").signedAt(OffsetDateTime.now())
                    .build();
            when(idtSignatureRepository.findByCarePlanId(1L)).thenReturn(List.of(sig));

            IDTSignatureResponse result = idtSignatureService.getIDTSignatures(1L);

            assertThat(result.getSignatures()).hasSize(1);
            assertThat(result.getSignatures().get(0).getUserId()).isEqualTo(5L);
            assertThat(result.getSignatures().get(0).getUserName()).isEqualTo("Alan Cho");
            assertThat(result.getSignatures().get(0).getRole()).isEqualTo("PHYSICIAN");
        }

        @Test
        @DisplayName("getIDTSignatures - care plan không tồn tại thì ném CARE_PLAN_NOT_FOUND")
        void getIDTSignatures_carePlanNotFound_throwsException() {
            when(carePlanRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> idtSignatureService.getIDTSignatures(999L))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining(ErrorCode.CARE_PLAN_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("getIDTSignatures - care plan chưa có ai ký thì trả về list rỗng")
        void getIDTSignatures_emptyList() {
            when(carePlanRepository.findById(1L)).thenReturn(Optional.of(carePlan));
            when(idtSignatureRepository.findByCarePlanId(1L)).thenReturn(Collections.emptyList());

            IDTSignatureResponse result = idtSignatureService.getIDTSignatures(1L);

            assertThat(result.getSignatures()).isEmpty();
        }

        @Test
        @DisplayName("submitIDTSignature - lấy user từ SecurityContext, không dùng userId từ request")
        void submitIDTSignature_usesCurrentUser() {
            when(securityUtils.getCurrentUser()).thenReturn(physician);
            when(carePlanRepository.findById(1L)).thenReturn(Optional.of(carePlan));
            when(idtSignatureRepository.existsByCarePlanIdAndUserId(1L, 5L)).thenReturn(false);

            IDTSignature saved = IDTSignature.builder()
                    .id(10L).carePlan(carePlan).user(physician)
                    .comments("Reviewed").signedAt(OffsetDateTime.now())
                    .build();
            when(idtSignatureRepository.save(any())).thenReturn(saved);

            IDTSignatureRequest req = new IDTSignatureRequest();
            req.setComments("Reviewed");

            IDTSignatureResponse.SignatureItem result = idtSignatureService.submitIDTSignature(1L, req);

            assertThat(result.getUserId()).isEqualTo(5L);
            assertThat(result.getUserName()).isEqualTo("Alan Cho");
            assertThat(result.getComments()).isEqualTo("Reviewed");
            // Xác nhận KHÔNG gọi userRepository.findByIdAndIsDeletedFalse (không dùng userId từ request)
            verify(userRepository, never()).findByIdAndIsDeletedFalse(any());
        }

        @Test
        @DisplayName("submitIDTSignature - ký không có comments (null) vẫn thành công")
        void submitIDTSignature_nullComments_succeeds() {
            when(securityUtils.getCurrentUser()).thenReturn(physician);
            when(carePlanRepository.findById(1L)).thenReturn(Optional.of(carePlan));
            when(idtSignatureRepository.existsByCarePlanIdAndUserId(1L, 5L)).thenReturn(false);

            IDTSignature saved = IDTSignature.builder()
                    .id(11L).carePlan(carePlan).user(physician)
                    .comments(null).signedAt(OffsetDateTime.now())
                    .build();
            when(idtSignatureRepository.save(any())).thenReturn(saved);

            IDTSignatureRequest req = new IDTSignatureRequest();
            req.setComments(null);

            IDTSignatureResponse.SignatureItem result = idtSignatureService.submitIDTSignature(1L, req);

            assertThat(result.getComments()).isNull();
        }

        @Test
        @DisplayName("submitIDTSignature - user đã ký rồi thì ném RESOURCE_ALREADY_EXISTS")
        void submitIDTSignature_duplicate_throwsException() {
            when(securityUtils.getCurrentUser()).thenReturn(physician);
            when(carePlanRepository.findById(1L)).thenReturn(Optional.of(carePlan));
            when(idtSignatureRepository.existsByCarePlanIdAndUserId(1L, 5L)).thenReturn(true);

            IDTSignatureRequest req = new IDTSignatureRequest();
            req.setComments("Trying again");

            assertThatThrownBy(() -> idtSignatureService.submitIDTSignature(1L, req))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining(ErrorCode.RESOURCE_ALREADY_EXISTS.getMessage());

            verify(idtSignatureRepository, never()).save(any());
        }

        @Test
        @DisplayName("submitIDTSignature - care plan không tồn tại thì ném CARE_PLAN_NOT_FOUND")
        void submitIDTSignature_carePlanNotFound_throwsException() {
            when(carePlanRepository.findById(999L)).thenReturn(Optional.empty());

            IDTSignatureRequest req = new IDTSignatureRequest();
            req.setComments("Test");

            assertThatThrownBy(() -> idtSignatureService.submitIDTSignature(999L, req))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining(ErrorCode.CARE_PLAN_NOT_FOUND.getMessage());
        }
    }
}
