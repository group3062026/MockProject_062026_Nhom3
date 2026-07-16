package com.mockproject.group3.service.impl;

import com.mockproject.group3.dto.admin.facility.AddressDto;
import com.mockproject.group3.dto.admin.facility.FacilityResponse;
import com.mockproject.group3.dto.admin.facility.UpdateFacilityRequest;
import com.mockproject.group3.entity.Address;
import com.mockproject.group3.entity.Facility;
import com.mockproject.group3.repository.AddressRepository;
import com.mockproject.group3.repository.FacilityRepository;
import com.mockproject.group3.service.AdminFacilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminFacilityServiceImpl implements AdminFacilityService {

    private final FacilityRepository facilityRepository;
    private final AddressRepository addressRepository;

    @Override
    @Transactional(readOnly = true)
    public FacilityResponse getFacilityInfo() {
        // Assuming MVP single facility or context-bound. Fetching ID 1 for now.
        Facility facility = facilityRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Facility not found"));
        return mapToFacilityResponse(facility);
    }

    @Override
    @Transactional
    public FacilityResponse updateFacilityInfo(UpdateFacilityRequest request) {
        Facility facility = facilityRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Facility not found"));

        facility.setFacilityCode(request.getFacilityCode());
        facility.setName(request.getName());
        facility.setLicenseNumber(request.getLicenseNumber());
        facility.setTargetState(request.getTargetState());
        facility.setPhoneNumber(request.getPhoneNumber());

        if (request.getAddress() != null) {
            Address address = facility.getAddress();
            if (address == null) {
                address = new Address();
            }
            address.setStreetLine1(request.getAddress().getStreetLine1());
            address.setStreetLine2(request.getAddress().getStreetLine2());
            address.setCity(request.getAddress().getCity());
            address.setState(request.getAddress().getState());
            address.setZipCode(request.getAddress().getZipCode());
            address.setAddressType(request.getAddress().getAddressType() != null ? request.getAddress().getAddressType() : "FACILITY");
            address = addressRepository.save(address);
            facility.setAddress(address);
        }

        facility = facilityRepository.save(facility);
        return mapToFacilityResponse(facility);
    }

    private FacilityResponse mapToFacilityResponse(Facility facility) {
        AddressDto addressDto = null;
        if (facility.getAddress() != null) {
            addressDto = AddressDto.builder()
                    .id(facility.getAddress().getId())
                    .streetLine1(facility.getAddress().getStreetLine1())
                    .streetLine2(facility.getAddress().getStreetLine2())
                    .city(facility.getAddress().getCity())
                    .state(facility.getAddress().getState())
                    .zipCode(facility.getAddress().getZipCode())
                    .addressType(facility.getAddress().getAddressType())
                    .build();
        }

        return FacilityResponse.builder()
                .id(facility.getId())
                .facilityCode(facility.getFacilityCode())
                .name(facility.getName())
                .licenseNumber(facility.getLicenseNumber())
                .targetState(facility.getTargetState())
                .phoneNumber(facility.getPhoneNumber())
                .address(addressDto)
                .build();
    }
}
