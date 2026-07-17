package com.nguyenquyen.mockproject_062026_group3.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    private List<T> items;          // Danh sách dữ liệu của trang hiện tại
    private int pageNo;             // Số trang hiện tại (bắt đầu từ 0)
    private int pageSize;           // Số lượng phần tử trên 1 trang
    private long totalElements;     // Tổng số record trong DB
    private int totalPages;         // Tổng số trang
    private boolean isLast;         // Cờ xác định trang cuối
}

