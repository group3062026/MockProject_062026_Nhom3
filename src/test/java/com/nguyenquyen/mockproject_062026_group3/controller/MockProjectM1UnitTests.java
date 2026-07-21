package com.nguyenquyen.mockproject_062026_group3.controller;

import com.nguyenquyen.mockproject_062026_group3.common.AuditLoggingHelper;
import com.nguyenquyen.mockproject_062026_group3.common.EncryptionUtils;
import com.nguyenquyen.mockproject_062026_group3.dto.*;
import com.nguyenquyen.mockproject_062026_group3.dto.request.ESignApproveRequest;
import com.nguyenquyen.mockproject_062026_group3.entity.*;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.repository.*;
import com.nguyenquyen.mockproject_062026_group3.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MockProjectM1UnitTests {

  @Mock
  private CarePlanRepository carePlanRepository;

  @Mock
  private CareGoalRepository careGoalRepository;

  @Mock
  private CareInterventionRepository careInterventionRepository;

  @Mock
  private CareTaskRepository careTaskRepository;

  @Mock
  private ResidentCareLevelHistoryRepository residentCareLevelHistoryRepository;

  @Mock
  private AuditLogRepository auditLogRepository;


  @InjectMocks
  private CarePlanService carePlanService;

    @Mock
    private CareLevelRepository careLevelRepository;

    @Mock
    private ResidentCareLevelHistoryRepository careLevelHistoryRepository;

    @Mock
    private CareLevelRateRepository careLevelRateRepository;

    @Mock
    private ResidentRepository residentRepository;

    @Mock
    private BedRepository bedRepository;

    @Mock
    private ResidentSensitiveInfoRepository sensitiveInfoRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private EncryptionUtils encryptionUtils;

    @Mock
    private AuditLoggingHelper auditLoggingHelper;


    @InjectMocks
    private CareLevelService careLevelService;

    @InjectMocks
    private CareLevelRateService careLevelRateService;

    @InjectMocks
    private ResidentService residentService;

    @InjectMocks
    private ResidentSensitiveInfoService sensitiveInfoService;

    // --- CareLevelService Tests ---

    @Test
    public void testGetCareLevels_FilterDeleted() {
        CareLevel cl1 = CareLevel.builder().id(1L).levelCode("MEMORY_CARE").levelName("Memory Care").isDeleted(false).build();
        CareLevel cl2 = CareLevel.builder().id(2L).levelCode("HOSPICE").levelName("Hospice").isDeleted(true).build();

        when(careLevelRepository.findAllByIsDeleted(false)).thenReturn(Collections.singletonList(cl1));
        when(careLevelRepository.findAll()).thenReturn(Arrays.asList(cl1, cl2));

        List<CareLevelResponse> responseActive = careLevelService.getCareLevels(false);
        assertEquals(1, responseActive.size());
        assertEquals("MEMORY_CARE", responseActive.get(0).getLevelCode());

        List<CareLevelResponse> responseAll = careLevelService.getCareLevels(true);
        assertEquals(2, responseAll.size());
    }

    @Test
    public void testDeleteCareLevel_BlockedByActiveResident() {
        CareLevel cl = CareLevel.builder().id(1L).levelCode("MEMORY_CARE").levelName("Memory Care").isDeleted(false).build();
        when(careLevelRepository.findById(1L)).thenReturn(Optional.of(cl));
        when(careLevelHistoryRepository.existsByCareLevelIdAndEndDateIsNullAndResidentStatus(1L, "ACTIVE")).thenReturn(true);

        assertThrows(AppException.class, () -> careLevelService.deleteCareLevel(1L));
    }

    @Test
    public void testDeleteCareLevel_Success() {
        CareLevel cl = CareLevel.builder().id(1L).levelCode("MEMORY_CARE").levelName("Memory Care").isDeleted(false).build();
        when(careLevelRepository.findById(1L)).thenReturn(Optional.of(cl));
        when(careLevelHistoryRepository.existsByCareLevelIdAndEndDateIsNullAndResidentStatus(1L, "ACTIVE")).thenReturn(false);

        boolean result = careLevelService.deleteCareLevel(1L);
        assertTrue(result);
        assertTrue(cl.getIsDeleted());
        verify(careLevelRepository, times(1)).save(cl);
    }

    // --- CareLevelRateService Tests ---

    @Test
    public void testCreateCareLevelRate_AutoClosePreviousRate() {
        CareLevelRateCreateRequest request = CareLevelRateCreateRequest.builder()
                .careLevelId(1L)
                .facilityId(5L)
                .dailyRate(BigDecimal.valueOf(160.0))
                .effectiveFrom(LocalDate.of(2026, 7, 1))
                .build();

        CareLevel cl = CareLevel.builder().id(1L).levelCode("MEMORY_CARE").build();
        Facility f = Facility.builder().id(5L).facilityCode("FAC-01").build();

        CareLevelRate oldRate = CareLevelRate.builder()
                .id(10L)
                .careLevel(cl)
                .facility(f)
                .dailyRate(BigDecimal.valueOf(150.0))
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .effectiveTo(null)
                .build();

        when(careLevelRepository.findById(1L)).thenReturn(Optional.of(cl));
        when(facilityRepository.findById(5L)).thenReturn(Optional.of(f));
        when(careLevelRateRepository.findByFacilityIdAndCareLevelIdAndEffectiveToIsNull(5L, 1L))
                .thenReturn(Optional.of(oldRate));
        when(careLevelRateRepository.save(any(CareLevelRate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareLevelRateCreateResponse response = careLevelRateService.createCareLevelRate(request);

        assertNotNull(response.getPreviousRateClosed());
        assertEquals(10L, response.getPreviousRateClosed().getId());
        assertEquals(LocalDate.of(2026, 6, 30), response.getPreviousRateClosed().getEffectiveTo());
        assertEquals(LocalDate.of(2026, 6, 30), oldRate.getEffectiveTo());
        assertEquals(BigDecimal.valueOf(160.0), response.getNewRate().getDailyRate());
    }

    @Test
    public void testUpdateCareLevelRate_PastRate_ThrowsException() {
        CareLevelRate rate = CareLevelRate.builder()
                .id(12L)
                .effectiveFrom(LocalDate.now().minusDays(1))
                .build();

        when(careLevelRateRepository.findById(12L)).thenReturn(Optional.of(rate));

        CareLevelRateUpdateRequest updateRequest = CareLevelRateUpdateRequest.builder()
                .dailyRate(BigDecimal.valueOf(170.0))
                .build();

        assertThrows(AppException.class, () -> careLevelRateService.updateCareLevelRate(12L, updateRequest));
    }

    // --- ResidentService Tests ---

    @Test
    public void testUpdateResident_CnaBlockedIfLocked() {
        Resident resident = Resident.builder()
                .id(101L)
                .isChartLocked(true)
                .isDeleted(false)
                .build();

        when(residentRepository.findById(101L)).thenReturn(Optional.of(resident));

        ResidentUpdateRequest updateRequest = ResidentUpdateRequest.builder().firstName("Johnny").build();

        assertThrows(AppException.class, () -> residentService.updateResident(101L, updateRequest, "CNA"));
    }

    @Test
    public void testUpdateResidentStatus_DischargedReleaseBed() {
        Bed bed = Bed.builder().id(402L).status("OCCUPIED").build();
        Resident resident = Resident.builder()
                .id(101L)
                .status("ACTIVE")
                .bed(bed)
                .isDeleted(false)
                .build();

        when(residentRepository.findById(101L)).thenReturn(Optional.of(resident));
        when(residentRepository.save(any(Resident.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResidentStatusUpdateRequest statusRequest = ResidentStatusUpdateRequest.builder()
                .status("DISCHARGED")
                .build();

        ResidentResponse response = residentService.updateResidentStatus(101L, statusRequest);

        assertEquals("DISCHARGED", response.getStatus());
        assertNull(resident.getBed());
        assertEquals("AVAILABLE", bed.getStatus());
        verify(bedRepository, times(1)).save(bed);
    }

    @Test
    public void testAssignResidentBed_OccupiedBed_ThrowsException() {
        Resident resident = Resident.builder().id(101L).isDeleted(false).build();
        Bed bed = Bed.builder().id(402L).status("OCCUPIED").build();

        when(residentRepository.findById(101L)).thenReturn(Optional.of(resident));
        when(bedRepository.findById(402L)).thenReturn(Optional.of(bed));

        ResidentBedAssignRequest assignRequest = ResidentBedAssignRequest.builder().bedId(402L).build();

        assertThrows(AppException.class, () -> residentService.assignResidentBed(101L, assignRequest));
    }

    // --- ResidentSensitiveInfoService Tests ---

    @Test
    public void testGetSensitiveInfo_MaskedDefault() {
        ResidentSensitiveInfo info = ResidentSensitiveInfo.builder()
                .id(1L)
                .ssnEncrypted("encrypted_ssn")
                .medicalRecordNumberEncrypted("encrypted_mrn")
                .bankAccountEncrypted("encrypted_bank")
                .primaryInsuranceIdEncrypted("encrypted_ins")
                .build();

        when(sensitiveInfoRepository.findByResidentId(101L)).thenReturn(Optional.of(info));
        when(encryptionUtils.decrypt("encrypted_ssn")).thenReturn("123456789");
        when(encryptionUtils.decrypt("encrypted_mrn")).thenReturn("MRN12345");
        when(encryptionUtils.decrypt("encrypted_bank")).thenReturn("1234567890");
        when(encryptionUtils.decrypt("encrypted_ins")).thenReturn("INS-99231");

        ResidentSensitiveInfoResponse response = sensitiveInfoService.getSensitiveInfo(101L, false, null);

        assertEquals("***-**-6789", response.getSsnMasked());
        assertEquals("MRN-****5", response.getMedicalRecordNumberMasked());
        assertEquals("XXXXXX7890", response.getBankAccount());
        assertEquals("INS-99231", response.getPrimaryInsuranceId());
        assertNull(response.getSsn());
    }

    @Test
    public void testGetSensitiveInfo_RevealLogsAccess() {
        ResidentSensitiveInfo info = ResidentSensitiveInfo.builder()
                .id(1L)
                .ssnEncrypted("encrypted_ssn")
                .medicalRecordNumberEncrypted("encrypted_mrn")
                .bankAccountEncrypted("encrypted_bank")
                .primaryInsuranceIdEncrypted("encrypted_ins")
                .build();

        when(sensitiveInfoRepository.findByResidentId(101L)).thenReturn(Optional.of(info));
        when(encryptionUtils.decrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0) + "_decrypted");

        ResidentSensitiveInfoResponse response = sensitiveInfoService.getSensitiveInfo(101L, true, "Audit check");

        assertEquals("encrypted_ssn_decrypted", response.getSsn());
        verify(auditLoggingHelper, times(1)).logPhiAccess("resident_sensitive_info", "1", "VIEW", "Audit check");
    }
  // --- CarePlanService Tests ---

  @Test
  public void testGetCarePlanDetail_Success() {

    Resident resident = Resident.builder()
        .id(1L)
        .firstName("Robert")
        .lastName("Hayes")
        .build();


    CarePlan carePlan = CarePlan.builder()
        .id(1L)
        .status("PENDING_REVIEW")
        .significantChangeFlag(false)
        .resident(resident)
        .build();


    when(carePlanRepository.findById(1L))
        .thenReturn(Optional.of(carePlan));


    when(careGoalRepository.findByCarePlanId(1L))
        .thenReturn(Collections.emptyList());


    when(careInterventionRepository.findByCarePlanId(1L))
        .thenReturn(Collections.emptyList());


    when(careTaskRepository.findByCareInterventionCarePlanId(1L))
        .thenReturn(Collections.emptyList());


    CarePlanDetailResponse response =
        carePlanService.getCarePlanDetail(1L);


    assertEquals(
        1L,
        response.getId()
    );


    assertEquals(
        "Robert Hayes",
        response.getResidentName()
    );


    assertEquals(
        "PENDING_REVIEW",
        response.getStatus()
    );

  }



  @Test
  public void testGetCarePlanDetail_NotFound() {


    when(carePlanRepository.findById(99L))
        .thenReturn(Optional.empty());


    assertThrows(
        AppException.class,
        () ->
            carePlanService.getCarePlanDetail(99L)
    );

  }



  @Test
  public void testApproveWithSign_Success() {


    CarePlan carePlan = CarePlan.builder()
        .id(1L)
        .status("PENDING_REVIEW")
        .build();


    when(carePlanRepository.findById(1L))
        .thenReturn(Optional.of(carePlan));


    when(careGoalRepository.findByCarePlanId(1L))
        .thenReturn(Collections.emptyList());


    when(careInterventionRepository.findByCarePlanId(1L))
        .thenReturn(Collections.emptyList());


    when(careTaskRepository.findByCareInterventionCarePlanId(1L))
        .thenReturn(Collections.emptyList());


    ESignApproveRequest request =
        new ESignApproveRequest(
            "123456",
            true
        );


    carePlanService.approveWithSign(
        1L,
        request
    );


    assertEquals(
        "ACTIVE",
        carePlan.getStatus()
    );


    verify(
        carePlanRepository,
        times(1)
    )
        .save(carePlan);


    verify(
        auditLogRepository,
        times(1)
    )
        .save(any(AuditLog.class));

  }



  @Test
  public void testApproveWithSign_NotAccepted() {


    ESignApproveRequest request =
        new ESignApproveRequest(
            "123456",
            false
        );


    assertThrows(
        AppException.class,
        () ->
            carePlanService.approveWithSign(
                1L,
                request
            )
    );

  }
}
