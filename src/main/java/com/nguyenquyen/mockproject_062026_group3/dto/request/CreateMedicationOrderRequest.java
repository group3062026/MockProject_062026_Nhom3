package com.nguyenquyen.mockproject_062026_group3.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateMedicationOrderRequest {
    private Long residentId;
    private String drugName;
    private String dosage;
    private String route;
    private String frequency;
    private Boolean isControlledSubstance;
    private Long prescribedBy;
    private String prescriberNotes;
    private List<String> scheduledTimes; // ["08:00:00", "16:00:00"]
}