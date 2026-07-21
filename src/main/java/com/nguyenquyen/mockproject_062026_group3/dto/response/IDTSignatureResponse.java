package com.nguyenquyen.mockproject_062026_group3.dto.response;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
//sc-036
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IDTSignatureResponse {

    private List<SignatureItem> signatures;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SignatureItem {

        private Long id;

        private Long userId;

        private String userName;

        private String role;

        private String comments;

        private OffsetDateTime signedAt;
    }
}
