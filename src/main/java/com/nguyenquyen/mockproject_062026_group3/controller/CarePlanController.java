package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.ApiResponse;
import com.nguyenquyen.mockproject_062026_group3.common.SecurityUtils;
import com.nguyenquyen.mockproject_062026_group3.dto.CarePlanDetailResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.ComplianceChecklistResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.IDTAcknowledgmentResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.careplan.CarePlanResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.request.ESignApproveRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.request.RejectCarePlanRequest;
import com.nguyenquyen.mockproject_062026_group3.entity.CarePlan;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.repository.CarePlanRepository;
import com.nguyenquyen.mockproject_062026_group3.service.CarePlanService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/care-plans")
public class CarePlanController {


  @Autowired
  private CarePlanService carePlanService;

  @Autowired
  private CarePlanRepository carePlanRepository;

  @Autowired
  private SecurityUtils securityUtils;


  @GetMapping
  public ResponseEntity<ApiResponse<Map<String, Object>>> getCarePlans(
      @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
      @RequestParam(value = "resident_name", required = false) String residentName,
      @RequestParam(value = "status", required = false) String status,
      @RequestParam(value = "review_status", required = false) String reviewStatus
  ) {

    securityUtils.checkRoles(
        "NURSE",
        "DON",
        "CNA"
    );

    List<CarePlanResponse> carePlans =
        carePlanService.getCarePlans(
            page,
            size,
            residentName,
            status,
            reviewStatus
        );

    Map<String, Object> data = new HashMap<>();

    data.put(
        "carePlans",
        carePlans
    );

    return ResponseEntity.ok(
        ApiResponse.success(data)
    );
  }
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<Map<String,Object>>> getCarePlanDetail(
      @PathVariable Long id
  ) {

    securityUtils.checkRoles(
        "NURSE",
        "DON",
        "CNA"
    );


    CarePlanDetailResponse carePlan =
        carePlanService.getCarePlanDetail(id);


    Map<String,Object> data = new HashMap<>();

    data.put(
        "carePlan",
        carePlan
    );


    return ResponseEntity.ok(
        ApiResponse.success(data)
    );
  }
  @GetMapping("/{id}/compliance-checklist")
  public ResponseEntity<ApiResponse<ComplianceChecklistResponse>>
  getComplianceChecklist(
      @PathVariable Long id
  ){

    return ResponseEntity.ok(
        ApiResponse.success(
            carePlanService.getComplianceChecklist(id)
        )
    );

  }
  @GetMapping("/{id}/idt-acknowledgment")
  public ResponseEntity<ApiResponse<IDTAcknowledgmentResponse>>
  getIDTAcknowledgment(
      @PathVariable Long id
  ){

    return ResponseEntity.ok(
        ApiResponse.success(
            carePlanService.getIDTAcknowledgment(id)
        )
    );

  }
  public ResponseEntity<ApiResponse<CarePlanDetailResponse>>
  approveCarePlan(
      @PathVariable Long id
  ){

    securityUtils.checkRoles(
        "DON"
    );


    return ResponseEntity.ok(
        ApiResponse.success(
            carePlanService.approveCarePlan(id)
        )
    );

  }
  @PostMapping("/{id}/reject")
  public ResponseEntity<ApiResponse<CarePlanDetailResponse>>
  rejectCarePlan(
      @PathVariable Long id,
      @RequestBody RejectCarePlanRequest request
  ){

    securityUtils.checkRoles(
        "DON"
    );


    return ResponseEntity.ok(
        ApiResponse.success(
            carePlanService.rejectCarePlan(
                id,
                request.getReason()
            )
        )
    );

  }
  @PostMapping("/{id}/approve-sign")
  public ResponseEntity<ApiResponse<CarePlanDetailResponse>> approveWithSign(
      @PathVariable Long id,
      @RequestBody ESignApproveRequest request
  ){

    securityUtils.checkRoles(
        "DON"
    );


    return ResponseEntity.ok(
        ApiResponse.success(
            carePlanService.approveWithSign(
                id,
                request
            )
        )
    );
  }
}