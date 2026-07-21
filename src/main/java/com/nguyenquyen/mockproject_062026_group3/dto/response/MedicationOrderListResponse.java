package com.nguyenquyen.mockproject_062026_group3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MedicationOrderListResponse {
    private List<MedicationOrderResponse> medicationOrders;
    private PaginationMetadata metadata;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PaginationMetadata {
        private Integer currentPage;
        private Integer totalPage;
        private Integer currentLimit;
        private Boolean hasNext;
        private Boolean hasPrevious;
    }
}