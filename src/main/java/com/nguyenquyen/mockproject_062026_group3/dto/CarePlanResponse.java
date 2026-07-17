package com.nguyenquyen.mockproject_062026_group3.dto.careplan;

import lombok.*;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CarePlanResponse {

    private Long id;

    private String residentName;

    private String locTier;

    private String status;

    private OffsetDateTime lastReview;

    private OffsetDateTime nextReview;

    private String assignedTo;
}