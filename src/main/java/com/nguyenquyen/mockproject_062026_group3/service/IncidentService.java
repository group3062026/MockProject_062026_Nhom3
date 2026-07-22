package com.nguyenquyen.mockproject_062026_group3.service;


import com.nguyenquyen.mockproject_062026_group3.dto.IncidentDetailResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.IncidentResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.IncidentSummaryResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.SeverityResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.request.CreateIncidentRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.ExternalReportRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.ResolveIncidentRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.UnlockChartRequest;

import com.nguyenquyen.mockproject_062026_group3.entity.Incident;
import com.nguyenquyen.mockproject_062026_group3.entity.IncidentSeverity;
import com.nguyenquyen.mockproject_062026_group3.entity.Resident;
import com.nguyenquyen.mockproject_062026_group3.entity.User;

import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;

import com.nguyenquyen.mockproject_062026_group3.repository.IncidentRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.IncidentSeverityRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.ResidentRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class IncidentService {


  private final IncidentRepository incidentRepository;

  private final IncidentSeverityRepository severityRepository;

  private final ResidentRepository residentRepository;

  private final UserRepository userRepository;



  // =====================================================
  // CREATE INCIDENT - M7 US01
  // =====================================================

  public IncidentResponse createIncident(
      CreateIncidentRequest request
  ){

    IncidentSeverity severity =
        severityRepository.findById(
                request.getSeverityId()
            )
            .orElseThrow(
                () -> new AppException(
                    ErrorCode.INCIDENT_SEVERITY_NOT_FOUND
                )
            );


    Resident resident =
        residentRepository.findById(
                request.getResidentId()
            )
            .orElseThrow(
                () -> new AppException(
                    ErrorCode.RESIDENT_NOT_FOUND
                )
            );


    User reporter =
        userRepository.findById(1L)
            .orElseThrow(
                () -> new AppException(
                    ErrorCode.USER_NOT_FOUND
                )
            );



    // Auto lock chart
    if(Boolean.TRUE.equals(
        severity.getChartLockTrigger()
    )){

      resident.setIsChartLocked(true);

      resident.setUpdatedAt(
          OffsetDateTime.now()
      );

      residentRepository.save(resident);
    }



    Incident incident =
        Incident.builder()

            .incidentType(
                request.getIncidentType()
            )

            .description(
                request.getDescription()
            )

            .resident(
                resident
            )

            .severity(
                severity
            )

            .reportedBy(
                reporter
            )

            .status(
                "OPEN"
            )

            .slaDeadline(
                OffsetDateTime.now()
                    .plusHours(
                        severity.getResolutionTime()
                    )
            )

            .reportedAt(
                OffsetDateTime.now()
            )

            .build();



    Incident saved =
        incidentRepository.save(incident);



    return mapToResponse(saved);

  }




  // =====================================================
  // GET INCIDENT LIST - M7 US04
  // =====================================================

  public List<IncidentResponse> getIncidents(){


    return incidentRepository.findAll()
        .stream()

        .map(this::mapToResponse)

        .toList();

  }




  // =====================================================
  // GET INCIDENT DETAIL - M7 US03
  // =====================================================

  public IncidentDetailResponse getDetail(
      Long id
  ){

    Incident incident =
        incidentRepository.findById(id)

            .orElseThrow(
                () -> new AppException(
                    ErrorCode.INCIDENT_NOT_FOUND
                )
            );



    return IncidentDetailResponse.builder()

        .id(
            incident.getId()
        )

        .incidentType(
            incident.getIncidentType()
        )

        .status(
            incident.getStatus()
        )

        .description(
            incident.getDescription()
        )

        .severity(
            incident.getSeverity()
                .getLevelName()
        )

        .slaDeadline(
            incident.getSlaDeadline()
        )

        .reportedAt(
            incident.getReportedAt()
        )

        .chartLocked(
            incident.getResident()
                .getIsChartLocked()
        )

        .build();

  }




  // =====================================================
  // GET SEVERITY LIST - M7 US01
  // =====================================================

  public List<SeverityResponse> getSeverities(){


    return severityRepository.findAll()

        .stream()

        .map(item ->

            SeverityResponse.builder()

                .id(
                    item.getId()
                )

                .levelName(
                    item.getLevelName()
                )

                .automaticLockChart(
                    item.getChartLockTrigger()
                )

                .resolutionTime(
                    item.getResolutionTime()
                )

                .build()

        )

        .toList();

  }




  // =====================================================
  // SUBMIT EXTERNAL REPORT - M7 US06
  // =====================================================

  public void submitExternalReport(
      Long id,
      ExternalReportRequest request
  ){

    Incident incident =
        incidentRepository.findById(id)

            .orElseThrow(
                () -> new AppException(
                    ErrorCode.INCIDENT_NOT_FOUND
                )
            );


    incident.setStatus(
        "SUBMITTED"
    );


    incidentRepository.save(incident);

  }





  // =====================================================
  // UNLOCK CHART - M7 US07
  // =====================================================

  public void unlockChart(
      Long id,
      UnlockChartRequest request
  ){

    Incident incident =
        incidentRepository.findById(id)

            .orElseThrow(
                () -> new AppException(
                    ErrorCode.INCIDENT_NOT_FOUND
                )
            );



    if(request.getReason() == null ||
        request.getReason().isEmpty()){

      throw new AppException(
          ErrorCode.INCIDENT_UNLOCK_REASON_REQUIRED
      );

    }



    Resident resident =
        incident.getResident();


    resident.setIsChartLocked(false);


    residentRepository.save(resident);


  }




  // =====================================================
  // RESOLVE INCIDENT
  // =====================================================

  public void resolveIncident(
      Long id,
      ResolveIncidentRequest request
  ){

    Incident incident =
        incidentRepository.findById(id)

            .orElseThrow(
                () -> new AppException(
                    ErrorCode.INCIDENT_NOT_FOUND
                )
            );



    if(
        "RESOLVED".equals(
            incident.getStatus()
        )
    ){

      throw new AppException(
          ErrorCode.INCIDENT_ALREADY_RESOLVED
      );

    }



    incident.setStatus(
        "RESOLVED"
    );


    incident.setResolutionNote(
        request.getResolutionNote()
    );


    incident.setResolvedAt(
        OffsetDateTime.now()
    );


    incidentRepository.save(incident);

  }




  // =====================================================
  // DASHBOARD SUMMARY
  // =====================================================

  public IncidentSummaryResponse getSummary(){


    List<Incident> list =
        incidentRepository.findAll();



    return IncidentSummaryResponse.builder()

        .total(
            list.size()
        )

        .open(
            list.stream()
                .filter(
                    x -> "OPEN"
                        .equals(x.getStatus())
                )
                .count()
        )

        .resolved(
            list.stream()
                .filter(
                    x -> "RESOLVED"
                        .equals(x.getStatus())
                )
                .count()
        )

        .chartLocked(
            list.stream()
                .filter(
                    x -> Boolean.TRUE.equals(
                        x.getResident()
                            .getIsChartLocked()
                    )
                )
                .count()
        )

        .build();

  }




  // =====================================================
  // MAPPING
  // =====================================================

  private IncidentResponse mapToResponse(
      Incident incident
  ){

    return IncidentResponse.builder()

        .id(
            incident.getId()
        )

        .residentName(
            incident.getResident()
                .getDisplayName()
        )

        .incidentType(
            incident.getIncidentType()
        )

        .severity(
            incident.getSeverity()
                .getLevelName()
        )

        .status(
            incident.getStatus()
        )

        .chartLocked(
            incident.getResident()
                .getIsChartLocked()
        )

        .reportedAt(
            incident.getReportedAt()
        )

        .slaDeadline(
            incident.getSlaDeadline()
        )

        .build();

  }

}