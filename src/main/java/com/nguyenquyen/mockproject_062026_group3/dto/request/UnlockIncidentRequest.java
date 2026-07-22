package com.nguyenquyen.mockproject_062026_group3.dto.request;

import lombok.Data;

// SC_043 - DON unlock chart
@Data
public class UnlockIncidentRequest {
    private String reason;
    private String password;
}
