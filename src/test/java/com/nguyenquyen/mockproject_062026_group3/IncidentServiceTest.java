package com.nguyenquyen.mockproject_062026_group3;


import com.nguyenquyen.mockproject_062026_group3.dto.IncidentResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.request.CreateIncidentRequest;
import com.nguyenquyen.mockproject_062026_group3.entity.*;
import com.nguyenquyen.mockproject_062026_group3.repository.*;

import com.nguyenquyen.mockproject_062026_group3.service.IncidentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



class IncidentServiceTest {


  @InjectMocks
  private IncidentService incidentService;


  @Mock
  private IncidentRepository incidentRepository;


  @Mock
  private IncidentSeverityRepository severityRepository;


  @Mock
  private ResidentRepository residentRepository;


  @Mock
  private UserRepository userRepository;



  @BeforeEach
  void setup(){

    MockitoAnnotations.openMocks(this);

  }



  @Test
  void createIncident_success(){


    IncidentSeverity severity =
        IncidentSeverity.builder()
            .id(1L)
            .levelName("Major")
            .chartLockTrigger(true)
            .resolutionTime(24)
            .build();



    Resident resident =
        Resident.builder()
            .id(1L)
            .firstName("Robert")
            .lastName("Hayes")
            .isChartLocked(false)
            .build();



    User user =
        User.builder()
            .id(1L)
            .build();



    when(
        severityRepository.findById(1L)
    )
        .thenReturn(
            Optional.of(severity)
        );


    when(
        residentRepository.findById(1L)
    )
        .thenReturn(
            Optional.of(resident)
        );


    when(
        userRepository.findById(1L)
    )
        .thenReturn(
            Optional.of(user)
        );



    Incident incident =
        Incident.builder()
            .id(1L)
            .incidentType("FALL")
            .status("OPEN")
            .severity(severity)
            .resident(resident)
            .build();



    when(
        incidentRepository.save(any())
    )
        .thenReturn(incident);



    CreateIncidentRequest request =
        new CreateIncidentRequest();


    request.setResidentId(1L);
    request.setSeverityId(1L);
    request.setIncidentType("FALL");
    request.setDescription(
        "Resident fell"
    );



    IncidentResponse response =
        incidentService.createIncident(request);



    assertNotNull(response);

    assertEquals(
        "FALL",
        response.getIncidentType()
    );


    verify(
        residentRepository
    )
        .save(resident);


  }





  @Test
  void getIncidentList_success(){


    when(
        incidentRepository.findAll()
    )
        .thenReturn(
            java.util.List.of()
        );


    var result =
        incidentService.getIncidents();



    assertNotNull(result);

    assertEquals(
        0,
        result.size()
    );


  }


}