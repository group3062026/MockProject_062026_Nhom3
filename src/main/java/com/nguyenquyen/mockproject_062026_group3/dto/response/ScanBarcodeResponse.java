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
public class ScanBarcodeResponse {
    private String verificationStatus; // MATCHED, FAILED
    private FiveRights fiveRights;
    private Boolean canAdminister;
    private Boolean requiresOverride;
    private Boolean requiresWitness;
    private List<String> overrideReasons;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FiveRights {
        private RightDetail rightResident;
        private RightDetail rightMedication;
        private RightDetail rightDose;
        private RightDetail rightRoute;
        private RightDetail rightTime;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RightDetail {
        private Boolean passed;
        private String detail;
    }
}