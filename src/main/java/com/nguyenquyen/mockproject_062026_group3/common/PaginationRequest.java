package com.nguyenquyen.mockproject_062026_group3.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class để xử lý request phân trang từ client
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaginationRequest {
    @Builder.Default
    private int page = 0;                    // Trang hiện tại (bắt đầu từ 0)
    
    @Builder.Default
    private int pageSize = 10;               // Số lượng record trên 1 trang
    
    @Builder.Default
    private String sortBy = "id";            // Trường để sort
    
    @Builder.Default
    private String sortDirection = "DESC";   // Hướng sort (ASC/DESC)
    
    /**
     * Validate pagination request
     */
    public void validate() {
        if (this.page < 0) {
            this.page = 0;
        }
        if (this.pageSize <= 0 || this.pageSize > 100) {
            this.pageSize = 10;
        }
        if (this.sortDirection == null || (!this.sortDirection.equals("ASC") && !this.sortDirection.equals("DESC"))) {
            this.sortDirection = "DESC";
        }
    }
}

