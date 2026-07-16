package com.nguyenquyen.mockproject_062026_group3.dto.response;



import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
//sc-034
@Data
@Builder
public class CarePlanReassessmentResponseDTO {

    private Long planId;
    private String currentStatus;
    private LocalDate reviewDueDate;
    private String reviewTrigger; // 90_DAY_CYCLE hoặc SIGNIFICANT_CHANGE

    // Danh sách mục tiêu cũ để y tá xem và sửa
    private List<GoalResponseDTO> goals;

    // Danh sách can thiệp cũ để y tá xem và sửa
    private List<InterventionResponseDTO> interventions;

    @Data
    @Builder
    public static class GoalResponseDTO {
        private Long id;
        private String description;
        private String status;
    }

    @Data
    @Builder
    public static class InterventionResponseDTO {
        private Long id;
        private String description;
        private String assignedRole;
        private String frequency;
    }
}
