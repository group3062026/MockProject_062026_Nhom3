package com.nguyenquyen.mockproject_062026_group3.controller;


import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;

import com.nguyenquyen.mockproject_062026_group3.dto.request.CreateIncidentRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.ExternalReportRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.ResolveIncidentRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.UnlockChartRequest;

import com.nguyenquyen.mockproject_062026_group3.service.IncidentService;

import com.nguyenquyen.mockproject_062026_group3.common.SecurityUtils;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class IncidentController {


  private final IncidentService incidentService;

  private final SecurityUtils securityUtils;



  // =====================================================
  // GET INCIDENT SEVERITY
  // Screen 37 - Report Incident
  // =====================================================

  @GetMapping("/incident-severities")
  public ApiResponse<?> getIncidentSeverities(){


    securityUtils.checkRoles(
        "NURSE",
        "CNA",
        "DON",
        "ADMIN"
    );


    return ApiResponse.success(
        incidentService.getSeverities()
    );

  }




  // =====================================================
  // CREATE INCIDENT
  // Screen 37 - Report New Incident
  // =====================================================

  @PostMapping("/incidents")
  public ApiResponse<?> createIncident(
      @RequestBody CreateIncidentRequest request
  ){


    securityUtils.checkRoles(
        "NURSE",
        "CNA",
        "DON"
    );


    return ApiResponse.success(
        incidentService.createIncident(request)
    );

  }





  // =====================================================
  // GET INCIDENT LIST
  // Screen 40 - Incident List
  // =====================================================

  @GetMapping("/incidents")
  public ApiResponse<?> getIncidents(){


    securityUtils.checkRoles(
        "NURSE",
        "CNA",
        "DON",
        "ADMIN"
    );


    return ApiResponse.success(
        incidentService.getIncidents()
    );

  }





  // =====================================================
  // GET INCIDENT DETAIL
  // Screen 39 - Incident Detail
  // =====================================================

  @GetMapping("/incidents/{id}")
  public ApiResponse<?> getDetail(
      @PathVariable Long id
  ){


    securityUtils.checkRoles(
        "NURSE",
        "CNA",
        "DON",
        "ADMIN"
    );


    return ApiResponse.success(
        incidentService.getDetail(id)
    );

  }






  // =====================================================
  // SUBMIT EXTERNAL REPORT
  // Screen 41
  // =====================================================

  @PatchMapping("/incidents/{id}/external-report")
  public ApiResponse<?> submitExternalReport(
      @PathVariable Long id,
      @RequestBody ExternalReportRequest request
  ){


    securityUtils.checkRoles(
        "DON"
    );


    incidentService.submitExternalReport(
        id,
        request
    );


    return ApiResponse.success(null);

  }






  // =====================================================
  // UNLOCK CHART
  // Screen 42
  // =====================================================

  @PatchMapping("/incidents/{id}/unlock-chart")
  public ApiResponse<?> unlockChart(
      @PathVariable Long id,
      @RequestBody UnlockChartRequest request
  ){


    securityUtils.checkRoles(
        "DON",
        "ADMIN"
    );


    incidentService.unlockChart(
        id,
        request
    );


    return ApiResponse.success(null);

  }






  // =====================================================
  // RESOLVE INCIDENT
  // =====================================================

  @PatchMapping("/incidents/{id}/resolve")
  public ApiResponse<?> resolveIncident(
      @PathVariable Long id,
      @RequestBody ResolveIncidentRequest request
  ){


    securityUtils.checkRoles(
        "DON"
    );


    incidentService.resolveIncident(
        id,
        request
    );


    return ApiResponse.success(null);

  }






  // =====================================================
  // DASHBOARD SUMMARY
  // =====================================================

  @GetMapping("/incidents/summary")
  public ApiResponse<?> getSummary(){


    securityUtils.checkRoles(
        "DON",
        "ADMIN"
    );


    return ApiResponse.success(
        incidentService.getSummary()
    );

  }


}