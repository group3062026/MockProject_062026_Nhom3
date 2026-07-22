package com.nguyenquyen.mockproject_062026_group3.service.impl;

import com.nguyenquyen.mockproject_062026_group3.common.SecurityUtils;
import com.nguyenquyen.mockproject_062026_group3.dto.request.CreateTimelineRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.UnlockIncidentRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.response.IncidentDetailDTO;
import com.nguyenquyen.mockproject_062026_group3.entity.*;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.repository.IncidentRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.IncidentTimelineRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.ResidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceImplTest {

    @Mock IncidentRepository incidentRepository;
    @Mock IncidentTimelineRepository timelineRepository;
    @Mock ResidentRepository residentRepository;
    @Mock SecurityUtils securityUtils;

    @InjectMocks IncidentServiceImpl incidentService;

    // ── shared fixtures ───────────────────────────────────────────────────────

    private Role role;
    private User currentUser;
    private IncidentSeverity severity;
    private Resident resident;
    private Incident incident;

    @BeforeEach
    void setUp() {
        role = Role.builder().id(1L).roleName("DON").build();

        currentUser = User.builder()
                .id(5L).email("don@test.com")
                .firstName("John").lastName("Doe")
                .employeeCode("NHMS-0001")
                .role(role).status("ACTIVE")
                .build();

        severity = IncidentSeverity.builder()
                .id(1L).levelName("Low").chartLockTrigger(false)
                .build();

        Room room = Room.builder().id(2L).roomNumber("102").roomType("SEMI_PRIVATE").build();
        Bed bed = Bed.builder().id(34L).bedNumber("C").room(room).build();

        resident = Resident.builder()
                .id(12L).firstName("Stephen").lastName("Curry")
                .gender("Male").status("ACTIVE").isChartLocked(true)
                .bed(bed)
                .build();

        incident = Incident.builder()
                .id(1L)
                .incidentType("MEDICATION_ERROR")
                .status("UNDER_INVESTIGATION")
                .description("Medication was administered later than scheduled.")
                .slaDeadline(OffsetDateTime.now().plusHours(13))
                .resident(resident)
                .severity(severity)
                .reportedBy(currentUser)
                .reportedAt(OffsetDateTime.now())
                .build();
    }

    // =========================================================================
    // getIncidentDetail
    // =========================================================================

    @Nested
    @DisplayName("getIncidentDetail")
    class GetIncidentDetailTests {

        @Test
        @DisplayName("trả về đầy đủ thông tin incident kèm timelines")
        void returnsFullDetail() {
            IncidentTimeline timeline = IncidentTimeline.builder()
                    .id(1L).incident(incident)
                    .action("incident reported").reason(null)
                    .actor(currentUser).createdAt(OffsetDateTime.now())
                    .build();

            when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
            when(timelineRepository.findByIncidentIdOrderByCreatedAtAsc(1L))
                    .thenReturn(List.of(timeline));

            IncidentDetailDTO result = incidentService.getIncidentDetail(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getIncidentType()).isEqualTo("MEDICATION_ERROR");
            assertThat(result.getStatus()).isEqualTo("UNDER_INVESTIGATION");
            assertThat(result.getResident().getDisplayName()).isEqualTo("Stephen Curry");
            assertThat(result.getResident().getGender()).isEqualTo("Male");
            assertThat(result.getSeverity().getLevelName()).isEqualTo("Low");
            assertThat(result.getReporter().getDisplayName()).isEqualTo("John Doe");
            assertThat(result.getTimelines()).hasSize(1);
            assertThat(result.getTimelines().get(0).getAction()).isEqualTo("incident reported");
        }

        @Test
        @DisplayName("map đúng thông tin bed và room")
        void mapsBedAndRoomCorrectly() {
            when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
            when(timelineRepository.findByIncidentIdOrderByCreatedAtAsc(1L))
                    .thenReturn(Collections.emptyList());

            IncidentDetailDTO result = incidentService.getIncidentDetail(1L);

            assertThat(result.getResident().getBed()).isNotNull();
            assertThat(result.getResident().getBed().getBedNumber()).isEqualTo("C");
            assertThat(result.getResident().getBed().isLocked()).isTrue();
            assertThat(result.getResident().getBed().getRoom().getRoomNumber()).isEqualTo("102");
            assertThat(result.getResident().getBed().getRoom().getRoomType()).isEqualTo("SEMI_PRIVATE");
        }

        @Test
        @DisplayName("resident không có bed thì bedDTO là null")
        void residentWithoutBed_bedDtoIsNull() {
            resident.setBed(null);
            when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
            when(timelineRepository.findByIncidentIdOrderByCreatedAtAsc(1L))
                    .thenReturn(Collections.emptyList());

            IncidentDetailDTO result = incidentService.getIncidentDetail(1L);

            assertThat(result.getResident().getBed()).isNull();
        }

        @Test
        @DisplayName("timeline không có actor thì actor là null")
        void timelineWithoutActor_actorIsNull() {
            IncidentTimeline timeline = IncidentTimeline.builder()
                    .id(2L).incident(incident)
                    .action("system event").reason(null)
                    .actor(null).createdAt(OffsetDateTime.now())
                    .build();

            when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
            when(timelineRepository.findByIncidentIdOrderByCreatedAtAsc(1L))
                    .thenReturn(List.of(timeline));

            IncidentDetailDTO result = incidentService.getIncidentDetail(1L);

            assertThat(result.getTimelines().get(0).getActor()).isNull();
        }

        @Test
        @DisplayName("slaDeadlineHours không âm khi đã quá hạn")
        void slaDeadlineHours_notNegativeWhenExpired() {
            incident.setSlaDeadline(OffsetDateTime.now().minusHours(5));
            when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
            when(timelineRepository.findByIncidentIdOrderByCreatedAtAsc(1L))
                    .thenReturn(Collections.emptyList());

            IncidentDetailDTO result = incidentService.getIncidentDetail(1L);

            assertThat(result.getSlaDeadlineHours()).isEqualTo(0);
        }

        @Test
        @DisplayName("incident không tồn tại thì ném INCIDENT_NOT_FOUND")
        void incidentNotFound_throwsException() {
            when(incidentRepository.findById(9999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> incidentService.getIncidentDetail(9999L))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining(ErrorCode.INCIDENT_NOT_FOUND.getMessage());
        }
    }

    // =========================================================================
    // unlockIncident
    // =========================================================================

    @Nested
    @DisplayName("unlockIncident")
    class UnlockIncidentTests {

        @Test
        @DisplayName("unlock thành công: resident.isChartLocked = false, status = RESOLVED, timeline được lưu")
        void unlock_success() {
            when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
            when(securityUtils.getCurrentUser()).thenReturn(currentUser);
            when(residentRepository.save(any())).thenReturn(resident);
            when(incidentRepository.save(any())).thenReturn(incident);
            when(timelineRepository.save(any())).thenReturn(IncidentTimeline.builder().id(99L).build());

            UnlockIncidentRequest request = new UnlockIncidentRequest();
            request.setReason("Problem resolved.");
            request.setPassword("pass");

            incidentService.unlockIncident(1L, request);

            assertThat(resident.getIsChartLocked()).isFalse();
            assertThat(incident.getStatus()).isEqualTo("RESOLVED");
            verify(residentRepository).save(resident);
            verify(incidentRepository).save(incident);
            verify(timelineRepository).save(any(IncidentTimeline.class));
        }

        @Test
        @DisplayName("timeline được lưu với action và reason đúng")
        void unlock_savesTimelineWithCorrectData() {
            when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
            when(securityUtils.getCurrentUser()).thenReturn(currentUser);
            when(residentRepository.save(any())).thenReturn(resident);
            when(incidentRepository.save(any())).thenReturn(incident);
            when(timelineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UnlockIncidentRequest request = new UnlockIncidentRequest();
            request.setReason("Resolved after review.");
            request.setPassword("pass");

            incidentService.unlockIncident(1L, request);

            verify(timelineRepository).save(argThat(t ->
                    "Chart unlocked and incident resolved".equals(t.getAction())
                    && "Resolved after review.".equals(t.getReason())
                    && t.getActor().getId().equals(5L)
            ));
        }

        @Test
        @DisplayName("reason rỗng thì ném INVALID_PARAMETER")
        void unlock_emptyReason_throwsException() {
            when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

            UnlockIncidentRequest request = new UnlockIncidentRequest();
            request.setReason("");
            request.setPassword("pass");

            assertThatThrownBy(() -> incidentService.unlockIncident(1L, request))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining(ErrorCode.INVALID_PARAMETER.getMessage());

            verify(residentRepository, never()).save(any());
            verify(incidentRepository, never()).save(any());
        }

        @Test
        @DisplayName("reason null thì ném INVALID_PARAMETER")
        void unlock_nullReason_throwsException() {
            when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

            UnlockIncidentRequest request = new UnlockIncidentRequest();
            request.setReason(null);
            request.setPassword("pass");

            assertThatThrownBy(() -> incidentService.unlockIncident(1L, request))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining(ErrorCode.INVALID_PARAMETER.getMessage());
        }

        @Test
        @DisplayName("incident không tồn tại thì ném INCIDENT_NOT_FOUND")
        void unlock_incidentNotFound_throwsException() {
            when(incidentRepository.findById(9999L)).thenReturn(Optional.empty());

            UnlockIncidentRequest request = new UnlockIncidentRequest();
            request.setReason("Resolved");
            request.setPassword("pass");

            assertThatThrownBy(() -> incidentService.unlockIncident(9999L, request))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining(ErrorCode.INCIDENT_NOT_FOUND.getMessage());
        }
    }

    // =========================================================================
    // addTimeline
    // =========================================================================

    @Nested
    @DisplayName("addTimeline")
    class AddTimelineTests {

        @Test
        @DisplayName("thêm timeline thành công, trả về DTO đúng")
        void addTimeline_success() {
            when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
            when(securityUtils.getCurrentUser()).thenReturn(currentUser);

            IncidentTimeline saved = IncidentTimeline.builder()
                    .id(10L).incident(incident)
                    .action("Physician notified.")
                    .reason(null)
                    .actor(currentUser)
                    .createdAt(OffsetDateTime.now())
                    .build();
            when(timelineRepository.save(any())).thenReturn(saved);

            CreateTimelineRequest request = new CreateTimelineRequest();
            request.setAction("Physician notified.");
            request.setReason(null);

            IncidentDetailDTO.TimelineDTO result = incidentService.addTimeline(1L, request);

            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getAction()).isEqualTo("Physician notified.");
            assertThat(result.getReason()).isNull();
            assertThat(result.getActor().getId()).isEqualTo(5L);
            assertThat(result.getActor().getDisplayName()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("thêm timeline với reason, trả về reason đúng")
        void addTimeline_withReason_returnsReason() {
            when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
            when(securityUtils.getCurrentUser()).thenReturn(currentUser);

            IncidentTimeline saved = IncidentTimeline.builder()
                    .id(11L).incident(incident)
                    .action("Investigation initiated.")
                    .reason("DON review required")
                    .actor(currentUser)
                    .createdAt(OffsetDateTime.now())
                    .build();
            when(timelineRepository.save(any())).thenReturn(saved);

            CreateTimelineRequest request = new CreateTimelineRequest();
            request.setAction("Investigation initiated.");
            request.setReason("DON review required");

            IncidentDetailDTO.TimelineDTO result = incidentService.addTimeline(1L, request);

            assertThat(result.getReason()).isEqualTo("DON review required");
        }

        @Test
        @DisplayName("lưu timeline với actor là user đang đăng nhập")
        void addTimeline_savesWithCurrentUser() {
            when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
            when(securityUtils.getCurrentUser()).thenReturn(currentUser);
            when(timelineRepository.save(any())).thenAnswer(inv -> {
                IncidentTimeline t = inv.getArgument(0);
                t = IncidentTimeline.builder()
                        .id(12L).incident(t.getIncident())
                        .action(t.getAction()).reason(t.getReason())
                        .actor(t.getActor()).createdAt(t.getCreatedAt())
                        .build();
                return t;
            });

            CreateTimelineRequest request = new CreateTimelineRequest();
            request.setAction("Test action");

            incidentService.addTimeline(1L, request);

            verify(timelineRepository).save(argThat(t ->
                    t.getActor().getId().equals(5L)
                    && "Test action".equals(t.getAction())
            ));
        }

        @Test
        @DisplayName("action rỗng thì ném INVALID_PARAMETER")
        void addTimeline_emptyAction_throwsException() {
            when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

            CreateTimelineRequest request = new CreateTimelineRequest();
            request.setAction("");

            assertThatThrownBy(() -> incidentService.addTimeline(1L, request))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining(ErrorCode.INVALID_PARAMETER.getMessage());

            verify(timelineRepository, never()).save(any());
        }

        @Test
        @DisplayName("action null thì ném INVALID_PARAMETER")
        void addTimeline_nullAction_throwsException() {
            when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));

            CreateTimelineRequest request = new CreateTimelineRequest();
            request.setAction(null);

            assertThatThrownBy(() -> incidentService.addTimeline(1L, request))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining(ErrorCode.INVALID_PARAMETER.getMessage());
        }

        @Test
        @DisplayName("incident không tồn tại thì ném INCIDENT_NOT_FOUND")
        void addTimeline_incidentNotFound_throwsException() {
            when(incidentRepository.findById(9999L)).thenReturn(Optional.empty());

            CreateTimelineRequest request = new CreateTimelineRequest();
            request.setAction("Test");

            assertThatThrownBy(() -> incidentService.addTimeline(9999L, request))
                    .isInstanceOf(AppException.class)
                    .hasMessageContaining(ErrorCode.INCIDENT_NOT_FOUND.getMessage());
        }
    }
}
