package com.nguyenquyen.mockproject_062026_group3;

import com.nguyenquyen.mockproject_062026_group3.common.AuditLoggingHelper;
import com.nguyenquyen.mockproject_062026_group3.dto.*;
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
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MockProjectM1NewApisUnitTests {

    @Mock
    private ResidentCareLevelHistoryRepository careLevelHistoryRepository;

    @Mock
    private ResidentRepository residentRepository;

    @Mock
    private CareLevelRepository careLevelRepository;

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private ResidentContactRepository residentContactRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private AuditLoggingHelper auditLoggingHelper;

    @InjectMocks
    private ResidentCareLevelHistoryService historyService;

    @InjectMocks
    private ContactService contactService;

    @InjectMocks
    private ResidentContactService residentContactService;

    // =====================================================================
    // ResidentCareLevelHistoryService Tests (#23-#27)
    // =====================================================================

    @Test
    public void testGetCareLevelHistory_Success() {
        Resident resident = Resident.builder().id(101L).isDeleted(false).build();
        CareLevel cl = CareLevel.builder().id(4L).levelCode("SKILLED_NURSING").build();
        ResidentCareLevelHistory h1 = ResidentCareLevelHistory.builder()
                .id(1L).resident(resident).careLevel(cl)
                .startDate(LocalDate.of(2025, 4, 1)).endDate(null).build();

        when(residentRepository.findById(101L)).thenReturn(Optional.of(resident));
        when(careLevelHistoryRepository.findByResidentIdOrderByStartDateDesc(101L))
                .thenReturn(Collections.singletonList(h1));

        List<CareLevelHistoryResponse> result = historyService.getCareLevelHistory(101L);

        assertEquals(1, result.size());
        assertEquals("SKILLED_NURSING", result.get(0).getLevelCode());
        assertNull(result.get(0).getEndDate());
    }

    @Test
    public void testGetCareLevelHistory_ResidentNotFound() {
        when(residentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> historyService.getCareLevelHistory(999L));
    }

    @Test
    public void testTransitionCareLevel_AutoCloseCurrentRecord() {
        Resident resident = Resident.builder().id(101L).isDeleted(false).build();
        CareLevel oldCl = CareLevel.builder().id(2L).levelCode("ASSISTED_LIVING").build();
        CareLevel newCl = CareLevel.builder().id(4L).levelCode("SKILLED_NURSING").build();

        ResidentCareLevelHistory currentRecord = ResidentCareLevelHistory.builder()
                .id(1L).resident(resident).careLevel(oldCl)
                .startDate(LocalDate.of(2025, 1, 1)).endDate(null).build();

        when(residentRepository.findById(101L)).thenReturn(Optional.of(resident));
        when(careLevelRepository.findById(4L)).thenReturn(Optional.of(newCl));
        when(careLevelHistoryRepository.findByResidentIdAndEndDateIsNull(101L))
                .thenReturn(Optional.of(currentRecord));
        when(careLevelHistoryRepository.save(any(ResidentCareLevelHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CareLevelHistoryTransitionRequest request = new CareLevelHistoryTransitionRequest();
        request.setCareLevelId(4L);
        request.setStartDate(LocalDate.of(2026, 7, 10));

        Map<String, Object> result = historyService.transitionCareLevel(101L, request);

        assertNotNull(result.get("newRecord"));
        assertNotNull(result.get("closedRecord"));
        assertEquals(LocalDate.of(2026, 7, 10), currentRecord.getEndDate());
    }

    @Test
    public void testTransitionCareLevel_InvalidStartDate_ThrowsException() {
        Resident resident = Resident.builder().id(101L).isDeleted(false).build();
        CareLevel newCl = CareLevel.builder().id(4L).levelCode("SKILLED_NURSING").build();

        ResidentCareLevelHistory currentRecord = ResidentCareLevelHistory.builder()
                .id(1L).resident(resident).careLevel(newCl)
                .startDate(LocalDate.of(2026, 6, 1)).endDate(null).build();

        when(residentRepository.findById(101L)).thenReturn(Optional.of(resident));
        when(careLevelRepository.findById(4L)).thenReturn(Optional.of(newCl));
        when(careLevelHistoryRepository.findByResidentIdAndEndDateIsNull(101L))
                .thenReturn(Optional.of(currentRecord));

        // startDate trước startDate hiện tại → lỗi
        CareLevelHistoryTransitionRequest request = new CareLevelHistoryTransitionRequest();
        request.setCareLevelId(4L);
        request.setStartDate(LocalDate.of(2026, 5, 1));

        assertThrows(AppException.class, () -> historyService.transitionCareLevel(101L, request));
    }

    @Test
    public void testUpdateCareLevelHistory_CurrentRecord_ThrowsException() {
        // Record hiện tại (endDate == null) không được phép sửa
        CareLevel cl = CareLevel.builder().id(4L).levelCode("SKILLED_NURSING").build();
        ResidentCareLevelHistory current = ResidentCareLevelHistory.builder()
                .id(1L).careLevel(cl).startDate(LocalDate.of(2025, 1, 1)).endDate(null).build();

        when(careLevelHistoryRepository.findById(1L)).thenReturn(Optional.of(current));

        CareLevelHistoryUpdateRequest request = new CareLevelHistoryUpdateRequest();
        request.setStartDate(LocalDate.of(2025, 1, 5));
        request.setReason("Correction");

        assertThrows(AppException.class, () -> historyService.updateCareLevelHistory(1L, request));
    }

    @Test
    public void testUpdateCareLevelHistory_ClosedRecord_Success() {
        CareLevel cl = CareLevel.builder().id(4L).levelCode("SKILLED_NURSING").build();
        ResidentCareLevelHistory closed = ResidentCareLevelHistory.builder()
                .id(1L).careLevel(cl)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 6, 1)).build();

        when(careLevelHistoryRepository.findById(1L)).thenReturn(Optional.of(closed));
        when(careLevelHistoryRepository.save(any(ResidentCareLevelHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CareLevelHistoryUpdateRequest request = new CareLevelHistoryUpdateRequest();
        request.setStartDate(LocalDate.of(2025, 1, 5));
        request.setReason("Correction of historical data-entry error");

        CareLevelHistoryResponse result = historyService.updateCareLevelHistory(1L, request);

        assertEquals(LocalDate.of(2025, 1, 5), result.getStartDate());
        verify(auditLoggingHelper, times(1)).logAudit(
                eq("resident_care_level_history"), eq("1"), eq("UPDATE"), anyString(), contains("Correction"));
    }

    @Test
    public void testUpdateCareLevelHistory_MissingReason_ThrowsException() {
        CareLevelHistoryUpdateRequest request = new CareLevelHistoryUpdateRequest();
        request.setStartDate(LocalDate.of(2025, 1, 5));
        request.setReason(null);

        assertThrows(AppException.class, () -> historyService.updateCareLevelHistory(1L, request));
    }

    @Test
    public void testGetActiveSummary_Success() {
        Object[] row = new Object[]{"MEMORY_CARE", 34L};
        when(careLevelHistoryRepository.countActiveResidentsByCareLevelForFacility(5L))
                .thenReturn(Collections.singletonList(row));

        List<CareLevelActiveSummaryResponse> result = historyService.getActiveSummary(5L);

        assertEquals(1, result.size());
        assertEquals("MEMORY_CARE", result.get(0).getLevelCode());
        assertEquals(34L, result.get(0).getActiveResidentCount());
    }

    // =====================================================================
    // ContactService Tests (#28-#33)
    // =====================================================================

    @Test
    public void testCreateContact_Success() {
        ContactCreateRequest request = new ContactCreateRequest();
        request.setFirstName("Michael");
        request.setLastName("Anderson");
        request.setPhonePrimary("1-212-555-0143");

        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> {
            Contact c = invocation.getArgument(0);
            c.setId(25L);
            return c;
        });

        ContactResponse result = contactService.createContact(request);

        assertEquals("Michael", result.getFirstName());
        assertEquals("Anderson", result.getLastName());
        verify(contactRepository, times(1)).save(any(Contact.class));
    }

    @Test
    public void testCreateContact_MissingPhonePrimary_ThrowsException() {
        ContactCreateRequest request = new ContactCreateRequest();
        request.setFirstName("Michael");
        request.setLastName("Anderson");
        request.setPhonePrimary(null);

        assertThrows(AppException.class, () -> contactService.createContact(request));
    }

    @Test
    public void testCreateContact_MissingFirstName_ThrowsException() {
        ContactCreateRequest request = new ContactCreateRequest();
        request.setFirstName(null);
        request.setLastName("Anderson");
        request.setPhonePrimary("1-212-555-0143");

        assertThrows(AppException.class, () -> contactService.createContact(request));
    }

    @Test
    public void testDeleteContact_Success() {
        Contact contact = Contact.builder()
                .id(1L).firstName("Michael").lastName("Anderson")
                .phonePrimary("1-212-555-0143").isDeleted(false)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();

        when(contactRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(contact));
        when(residentContactRepository
                .existsByContactIdAndIsGuarantorTrueAndResidentStatusAndResidentIsDeletedFalse(1L, "ACTIVE"))
                .thenReturn(false);
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContactResponse result = contactService.deleteContact(1L);

        assertTrue(result.getIsDeleted());
    }

    @Test
    public void testDeleteContact_BlockedByActiveGuarantor() {
        Contact contact = Contact.builder()
                .id(1L).firstName("Michael").lastName("Anderson")
                .phonePrimary("1-212-555-0143").isDeleted(false)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();

        when(contactRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(contact));
        when(residentContactRepository
                .existsByContactIdAndIsGuarantorTrueAndResidentStatusAndResidentIsDeletedFalse(1L, "ACTIVE"))
                .thenReturn(true);

        assertThrows(AppException.class, () -> contactService.deleteContact(1L));
    }

    @Test
    public void testGetResidentsByContact_Success() {
        Contact contact = Contact.builder().id(1L).firstName("Michael").lastName("Anderson").build();
        Resident resident = Resident.builder().id(101L).firstName("Robert").lastName("Anderson").build();
        ResidentContact link = ResidentContact.builder()
                .id(1L).contact(contact).resident(resident).relationshipType("SON").build();

        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));
        when(residentContactRepository.findByContactId(1L)).thenReturn(Collections.singletonList(link));

        List<ContactResidentResponse> result = contactService.getResidentsByContact(1L);

        assertEquals(1, result.size());
        assertEquals(101L, result.get(0).getResidentId());
        assertEquals("SON", result.get(0).getRelationshipType());
    }

    @Test
    public void testUpdateContact_PartialUpdate() {
        Contact contact = Contact.builder()
                .id(1L).firstName("Michael").lastName("Anderson")
                .phonePrimary("1-212-555-0143").phoneSecondary(null)
                .isDeleted(false).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();

        when(contactRepository.findByIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(contact));
        when(contactRepository.save(any(Contact.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContactUpdateRequest request = new ContactUpdateRequest();
        request.setPhoneSecondary("1-212-555-0199");

        ContactResponse result = contactService.updateContact(1L, request);

        assertEquals("1-212-555-0199", result.getPhoneSecondary());
        assertEquals("Michael", result.getFirstName());
    }

    // =====================================================================
    // ResidentContactService Tests (#34-#39)
    // =====================================================================

    @Test
    public void testCreateResidentContact_AutoUnsetPrimary() {
        Resident resident = Resident.builder().id(101L).isDeleted(false).build();
        Contact contact = Contact.builder().id(3L).firstName("Jane").lastName("Doe")
                .phonePrimary("1-555-0000").isDeleted(false).build();

        ResidentContact oldPrimary = ResidentContact.builder()
                .id(10L).resident(resident).contact(contact)
                .relationshipType("DAUGHTER").isPrimary(true).build();

        when(residentRepository.findById(101L)).thenReturn(Optional.of(resident));
        when(contactRepository.findByIdAndIsDeletedFalse(3L)).thenReturn(Optional.of(contact));
        when(residentContactRepository.findByResidentIdAndIsPrimaryTrue(101L))
                .thenReturn(Optional.of(oldPrimary));
        when(residentContactRepository.save(any(ResidentContact.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResidentContactCreateRequest request = new ResidentContactCreateRequest();
        request.setContactId(3L);
        request.setRelationshipType("LEGAL_GUARDIAN");
        request.setIsPrimary(true);
        request.setIsGuarantor(true);
        request.setIsEmergencyContact(true);
        request.setFinancialResponsibilityPct(BigDecimal.valueOf(40.0));

        ResidentContactResponse result = residentContactService.createResidentContact(101L, request);

        // Verify cũ bị unset
        assertFalse(oldPrimary.getIsPrimary());
        // Verify mới được set
        assertTrue(result.getIsPrimary());
        assertEquals("LEGAL_GUARDIAN", result.getRelationshipType());
    }

    @Test
    public void testCreateResidentContact_MissingContactId_ThrowsException() {
        ResidentContactCreateRequest request = new ResidentContactCreateRequest();
        request.setContactId(null);
        request.setRelationshipType("SON");

        assertThrows(AppException.class, () -> residentContactService.createResidentContact(101L, request));
    }

    @Test
    public void testDeleteResidentContact_Success() {
        Resident resident = Resident.builder().id(101L).isDeleted(false).build();
        Contact contact = Contact.builder().id(1L).build();
        ResidentContact link = ResidentContact.builder()
                .id(25L).resident(resident).contact(contact).relationshipType("SON").build();

        when(residentRepository.findById(101L)).thenReturn(Optional.of(resident));
        when(residentContactRepository.findByResidentIdAndId(101L, 25L)).thenReturn(Optional.of(link));

        boolean result = residentContactService.deleteResidentContact(101L, 25L);

        assertTrue(result);
        verify(residentContactRepository, times(1)).delete(link);
    }

    @Test
    public void testGetGuarantorContact_Success() {
        Resident resident = Resident.builder().id(101L).isDeleted(false).build();
        Contact contact = Contact.builder()
                .id(1L).firstName("Michael").lastName("Anderson")
                .phonePrimary("1-212-555-0143").isDeleted(false)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        ResidentContact link = ResidentContact.builder()
                .id(1L).resident(resident).contact(contact)
                .isGuarantor(true).build();

        when(residentRepository.findById(101L)).thenReturn(Optional.of(resident));
        when(residentContactRepository.findByResidentIdAndIsGuarantorTrue(101L))
                .thenReturn(Optional.of(link));

        ContactResponse result = residentContactService.getGuarantorContact(101L);

        assertEquals("Michael", result.getFirstName());
        assertTrue(contact.getIsDeleted() == false);
    }

    @Test
    public void testGetGuarantorContact_NotFound_ThrowsException() {
        Resident resident = Resident.builder().id(101L).isDeleted(false).build();
        when(residentRepository.findById(101L)).thenReturn(Optional.of(resident));
        when(residentContactRepository.findByResidentIdAndIsGuarantorTrue(101L))
                .thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> residentContactService.getGuarantorContact(101L));
    }

    @Test
    public void testGetPrimaryContact_Success() {
        Resident resident = Resident.builder().id(101L).isDeleted(false).build();
        Contact contact = Contact.builder()
                .id(1L).firstName("Michael").lastName("Anderson")
                .phonePrimary("1-212-555-0143").isDeleted(false)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        ResidentContact link = ResidentContact.builder()
                .id(1L).resident(resident).contact(contact)
                .isPrimary(true).build();

        when(residentRepository.findById(101L)).thenReturn(Optional.of(resident));
        when(residentContactRepository.findByResidentIdAndIsPrimaryTrue(101L))
                .thenReturn(Optional.of(link));

        ContactResponse result = residentContactService.getPrimaryContact(101L);

        assertEquals("Michael", result.getFirstName());
    }

    @Test
    public void testUpdateResidentContact_RevalidatePrimary() {
        Resident resident = Resident.builder().id(101L).isDeleted(false).build();
        Contact contact = Contact.builder().id(1L).build();

        ResidentContact existingLink = ResidentContact.builder()
                .id(1L).resident(resident).contact(contact)
                .relationshipType("SON").isPrimary(false).isGuarantor(false)
                .isEmergencyContact(false).build();

        ResidentContact oldPrimary = ResidentContact.builder()
                .id(10L).resident(resident).contact(contact)
                .isPrimary(true).build();

        when(residentRepository.findById(101L)).thenReturn(Optional.of(resident));
        when(residentContactRepository.findByResidentIdAndId(101L, 1L))
                .thenReturn(Optional.of(existingLink));
        when(residentContactRepository.findByResidentIdAndIsPrimaryTrue(101L))
                .thenReturn(Optional.of(oldPrimary));
        when(residentContactRepository.save(any(ResidentContact.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResidentContactUpdateRequest request = new ResidentContactUpdateRequest();
        request.setIsPrimary(true);
        request.setFinancialResponsibilityPct(BigDecimal.valueOf(50.0));

        ResidentContactResponse result = residentContactService.updateResidentContact(101L, 1L, request);

        assertTrue(result.getIsPrimary());
        assertFalse(oldPrimary.getIsPrimary());
        assertEquals(BigDecimal.valueOf(50.0), result.getFinancialResponsibilityPct());
    }
}
