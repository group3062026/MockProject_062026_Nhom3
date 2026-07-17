package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.dto.CareActivityResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.CareCostResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.CarePlanDetailResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.ComplianceChecklistResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.IDTAcknowledgmentResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.careplan.CarePlanResponse;
import com.nguyenquyen.mockproject_062026_group3.dto.request.ESignApproveRequest;
import com.nguyenquyen.mockproject_062026_group3.entity.AuditLog;
import com.nguyenquyen.mockproject_062026_group3.entity.CareGoal;
import com.nguyenquyen.mockproject_062026_group3.entity.CareIntervention;
import com.nguyenquyen.mockproject_062026_group3.entity.CarePlan;
import com.nguyenquyen.mockproject_062026_group3.entity.CareTask;
import com.nguyenquyen.mockproject_062026_group3.entity.Resident;
import com.nguyenquyen.mockproject_062026_group3.entity.ResidentCareLevelHistory;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.repository.AuditLogRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.CareGoalRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.CareInterventionRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.CareLevelRateRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.CarePlanRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.CareTaskRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.ResidentCareLevelHistoryRepository;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CarePlanService {


  @Autowired
  private CarePlanRepository carePlanRepository;

  @Autowired
  private CareGoalRepository careGoalRepository;

  @Autowired
  private AuditLogRepository auditLogRepository;

  @Autowired
  private CareInterventionRepository careInterventionRepository;

  @Autowired
  private CareTaskRepository careTaskRepository;

  @Autowired
  private ResidentCareLevelHistoryRepository residentCareLevelHistoryRepository;

  @Autowired
  private CareLevelRateRepository careLevelRateRepository;


  public List<CarePlanResponse> getCarePlans(
      Integer page,
      Integer size,
      String residentName,
      String status,
      String reviewStatus
  ) {

    List<CarePlan> carePlans = carePlanRepository.findAll();

    return carePlans.stream()
        .map(this::convertToResponse)
        .toList();
  }


  private CarePlanResponse convertToResponse(
      CarePlan carePlan
  ) {

    return CarePlanResponse.builder()
        .id(carePlan.getId())

        .residentName(
            carePlan.getResident() != null
                ?
                carePlan.getResident().getFirstName()
                    + " "
                    + carePlan.getResident().getLastName()
                :
                    null
        )

        .status(
            carePlan.getStatus()
        )

        .build();
  }



  public CarePlanDetailResponse getCarePlanDetail(Long id) {


    CarePlan carePlan =
        carePlanRepository.findById(id)
            .orElseThrow(() ->
                new AppException(ErrorCode.CARE_PLAN_NOT_FOUND)
            );


    Resident resident = carePlan.getResident();


    List<CareGoal> goals =
        careGoalRepository.findByCarePlanId(id);


    List<CareIntervention> interventions =
        careInterventionRepository.findByCarePlanId(id);


    List<CareTask> tasks =
        careTaskRepository.findByCareInterventionCarePlanId(id);



    String locTier = null;

    BigDecimal locRate = null;

    BigDecimal dailyCost = null;

    BigDecimal monthlyCost = null;



    ResidentCareLevelHistory history =
        residentCareLevelHistoryRepository
            .findFirstByResidentIdAndEndDateIsNullOrderByStartDateDesc(
                resident.getId()
            )
            .orElse(null);



    if (history != null) {


      locTier =
          history.getCareLevel().getLevelName();



      locRate =
          careLevelRateRepository
              .findFirstByCareLevelIdOrderByEffectiveFromDesc(
                  history.getCareLevel().getId()
              )
              .map(rate -> rate.getDailyRate())
              .orElse(null);



      if (locRate != null) {

        dailyCost = locRate;

        monthlyCost =
            locRate.multiply(
                BigDecimal.valueOf(30)
            );
      }
    }



    return CarePlanDetailResponse.builder()

        .id(carePlan.getId())


        .residentName(
            resident.getFirstName()
                + " "
                + resident.getLastName()
        )


        .locTier(locTier)


        .status(
            carePlan.getStatus()
        )


        .significantChangeFlag(
            carePlan.getSignificantChangeFlag()
        )


        .goals(goals)


        .interventions(interventions)


        .tasks(tasks)


        .cost(
            CareCostResponse.builder()
                .locRate(locRate)
                .dailyCost(dailyCost)
                .monthlyCost(monthlyCost)
                .build()
        )


        .build();
  }

  public List<CareActivityResponse> getCareActivities(Long id) {


    return auditLogRepository
        .findByEntityIdOrderByCreatedAtDesc(id)
        .stream()
        .map(log ->
            CareActivityResponse.builder()

                .action(log.getAction())

                .performedAt(log.getPerformedAt())

                .build()
        )
        .toList();

  }
  public ComplianceChecklistResponse getComplianceChecklist(Long id) {


    return ComplianceChecklistResponse.builder()

        .planStarted48Hours(true)

        .comprehensivePlan7Days(true)

        .mdsAssessmentLinked(true)

        .caTitle22Addressed(true)

        .completedCount(4)

        .build();

  }
  public IDTAcknowledgmentResponse getIDTAcknowledgment(Long id) {


    return IDTAcknowledgmentResponse.builder()

        .members(
            List.of(

                IDTAcknowledgmentResponse.Member.builder()
                    .role("Physician")
                    .name("Dr. Alan Cho")
                    .status("SIGNED")
                    .build(),


                IDTAcknowledgmentResponse.Member.builder()
                    .role("Dietary")
                    .name("Grace Liu")
                    .status("SIGNED")
                    .build()

            )
        )

        .build();

  }
  public CarePlanDetailResponse approveCarePlan(Long id) {


    CarePlan carePlan =
        carePlanRepository.findById(id)
            .orElseThrow(() ->
                new AppException(ErrorCode.CARE_PLAN_NOT_FOUND)
            );


    carePlan.setStatus("ACTIVE");


    carePlanRepository.save(carePlan);


    return getCarePlanDetail(id);

  }
  public CarePlanDetailResponse rejectCarePlan(
      Long id,
      String reason
  ) {


    CarePlan carePlan =
        carePlanRepository.findById(id)
            .orElseThrow(() ->
                new AppException(ErrorCode.CARE_PLAN_NOT_FOUND)
            );


    carePlan.setStatus("DRAFT");

    carePlan.setRejectionReason(reason);


    carePlanRepository.save(carePlan);


    return getCarePlanDetail(id);

  }
  public CarePlanDetailResponse approveWithSign(
      Long id,
      ESignApproveRequest request
  ){

    CarePlan carePlan =
        carePlanRepository.findById(id)
            .orElseThrow(() ->
                new AppException(
                    ErrorCode.CARE_PLAN_NOT_FOUND
                )
            );


    if(!request.getAccepted()){

      throw new AppException(
          ErrorCode.BUSINESS_EXCEPTION
      );

    }


    // tạm thời chưa check password thật

    carePlan.setStatus("ACTIVE");


    carePlanRepository.save(carePlan);


    carePlan.setStatus("ACTIVE");

    carePlanRepository.save(carePlan);


    AuditLog log = AuditLog.builder()
        .tableName("care_plans")
        .recordId(String.valueOf(id))
        .action("APPROVED")
        .newData("Care Plan activated")
        .performedAt(OffsetDateTime.now())
        .build();


    auditLogRepository.save(log);


    return getCarePlanDetail(id);

  }
}