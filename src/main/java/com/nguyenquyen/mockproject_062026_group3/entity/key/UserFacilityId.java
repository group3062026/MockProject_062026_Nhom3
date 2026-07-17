package com.nguyenquyen.mockproject_062026_group3.entity.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@Embeddable
public class UserFacilityId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "facility_id")
    private Long facilityId;
}

