package com.nguyenquyen.mockproject_062026_group3.dto.request;

import lombok.Data;

// SC_043 - Add timeline entry
@Data
public class CreateTimelineRequest {
    private String action;
    private String reason;
}
