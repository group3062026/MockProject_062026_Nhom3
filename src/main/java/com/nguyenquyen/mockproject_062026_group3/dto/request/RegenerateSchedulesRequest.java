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
public class RegenerateSchedulesRequest {
    private List<String> newScheduledTimes; // ["08:00:00", "16:00:00", "00:00:00"]
}