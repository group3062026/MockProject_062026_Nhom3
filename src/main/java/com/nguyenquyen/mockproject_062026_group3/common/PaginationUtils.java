package com.nguyenquyen.mockproject_062026_group3.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Utility class cho các hàm chung
 */
public class PaginationUtils {
    
    /**
     * Tạo PageRequest từ PaginationRequest
     */
    public static PageRequest createPageRequest(PaginationRequest paginationRequest) {
        paginationRequest.validate();
        
        Sort.Direction direction = paginationRequest.getSortDirection().equalsIgnoreCase("ASC") 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        
        return PageRequest.of(
            paginationRequest.getPage(),
            paginationRequest.getPageSize(),
            Sort.by(direction, paginationRequest.getSortBy())
        );
    }
    
    /**
     * Kiểm tra index có hợp lệ không
     */
    public static void validateIndex(int page, int pageSize) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (pageSize <= 0 || pageSize > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
    }
}

