package com.nguyenquyen.mockproject_062026_group3.dto.request;




import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
//sc-032
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusUpdateRequestDTO {


    private String status; // Truyền vào các giá trị: COMPLETED, MISSED, REFUSED, PENDING

    private Boolean isAbnormalFlagged; // Cờ báo hiệu có bất thường (True/False)

    private String notes; // Ghi chú (Rất quan trọng nếu status là REFUSED để lưu lý do)
}
