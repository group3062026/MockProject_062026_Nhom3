package com.nguyenquyen.mockproject_062026_group3.dto.request;


import lombok.Data;
import java.util.List;
//sc-034
@Data
public class ReassessmentSubmitRequestDTO {


    private String reassessmentReason;


    private List<GoalDTO> goals;


    private List<InterventionDTO> interventions;

    @Data
    public static class GoalDTO {

        private String description;
        private String status; // IN_PROGRESS
    }

    @Data
    public static class InterventionDTO {

        private String description;
        private String assignedRole; // Ví dụ: CNA, RN
        private String frequency;
    }
}
