package com.nguyenquyen.mockproject_062026_group3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
//sc-032
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareTaskResponseDTO {

        // 1. Dữ liệu tổng quan cho thanh Progress Bar
        private ShiftProgressDTO shiftProgress;

        // 2. Danh sách các bệnh nhân và các công việc tương ứng
        private List<ResidentTasksDTO> residents;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ShiftProgressDTO {
            private int completed;
            private int total;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ResidentTasksDTO {
            private Long residentId;
            private String name;
            private String planStatus;
            private List<TaskItemDTO> tasks;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class TaskItemDTO {
            private Long taskId;
            private String taskName; // Lấy từ field taskType của Entity
            private String dueTime;  // Trả về dạng chuỗi "14:00" cho Frontend dễ vẽ
            private String status;
            private Boolean hasAbnormalAlert;
        }

}
