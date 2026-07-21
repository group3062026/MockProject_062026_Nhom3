package com.nguyenquyen.mockproject_062026_group3.dto.request;


import lombok.Data;
//sc-033
@Data
public class RecordVitalsRequestDTO {
    private Long taskId;
    private Long residentId;
    private Integer bloodPressureSystolic;
    private Integer bloodPressureDiastolic;
    private Integer heartRateBpm;
    private Integer spo2Percentage;
    private Double temperatureFahrenheit;
    private String notes;
    private Integer respiratoryRate;
    private Integer painScale;
}
