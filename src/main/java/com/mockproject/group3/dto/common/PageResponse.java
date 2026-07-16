package com.mockproject.group3.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Custom paginated response wrapper matching the API Document format.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {
    private List<T> data;
    private PageInfo page;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PageInfo {
        private int page;
        private int pageSize;
        private long totalItems;
        private int totalPages;
    }

    public static <T> PageResponse<T> of(org.springframework.data.domain.Page<T> springPage) {
        return PageResponse.<T>builder()
                .data(springPage.getContent())
                .page(PageInfo.builder()
                        .page(springPage.getNumber() + 1)
                        .pageSize(springPage.getSize())
                        .totalItems(springPage.getTotalElements())
                        .totalPages(springPage.getTotalPages())
                        .build())
                .build();
    }
}
