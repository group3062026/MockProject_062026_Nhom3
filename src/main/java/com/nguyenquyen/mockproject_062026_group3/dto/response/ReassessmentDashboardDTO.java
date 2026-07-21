package com.nguyenquyen.mockproject_062026_group3.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;
//sc-034
@Data
@Builder
public class ReassessmentDashboardDTO {
    // Phần Banner cảnh báo màu vàng ở trên cùng
    private int totalRequiresReassessment;
    private int totalOverdueEscalated;
    private String escalationMessage;

    // Danh sách các dòng bệnh nhân trong bảng
    private List<ReassessmentItemDTO> items;

    @Data
    @Builder
    public static class ReassessmentItemDTO {
        private Long carePlanId;
        private Long residentId;
        private String residentDisplayName; // Ví dụ: "Robert Hayes · 204B"
        private String trigger;             // "90-day cycle" hoặc "Significant Change (SCS)"
        private LocalDate dueDate;
        private Integer overdueDays;        // Quá hạn mấy ngày
        private boolean isEscalated;        // Quá hạn > 3 ngày (ân hạn) thì là true
        private String status;              // "Review Due", "Needs Update", "Active"
        private String action;              // "Start" hoặc "View"
    }
}