package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.common.AuditLoggingHelper;
import com.nguyenquyen.mockproject_062026_group3.common.EncryptionUtils;
import com.nguyenquyen.mockproject_062026_group3.dto.ResidentSensitiveInfoCreateRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.ResidentSensitiveInfoResponse;
import com.nguyenquyen.mockproject_062026_group3.entity.Resident;
import com.nguyenquyen.mockproject_062026_group3.entity.ResidentSensitiveInfo;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.repository.ResidentRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.ResidentSensitiveInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResidentSensitiveInfoService {

    @Autowired
    private ResidentSensitiveInfoRepository sensitiveInfoRepository;

    @Autowired
    private ResidentRepository residentRepository;

    @Autowired
    private EncryptionUtils encryptionUtils;

    @Autowired
    private AuditLoggingHelper auditLoggingHelper;

    private String maskSsn(String ssn) {
        if (ssn == null || ssn.isEmpty()) return "";
        if (ssn.length() >= 4) {
            return "***-**-" + ssn.substring(ssn.length() - 4);
        }
        return "***-**-" + ssn;
    }

    private String maskMrn(String mrn) {
        if (mrn == null || mrn.isEmpty()) return "";
        if (mrn.length() >= 5) {
            return "MRN-****" + mrn.substring(mrn.length() - 1);
        }
        return "MRN-****" + mrn;
    }

    private String maskBankAccount(String bank) {
        if (bank == null || bank.isEmpty()) return "";
        if (bank.length() >= 4) {
            return "XXXXXX" + bank.substring(bank.length() - 4);
        }
        return "XXXXXX" + bank;
    }

    @Transactional
    public ResidentSensitiveInfoResponse getSensitiveInfo(Long residentId, Boolean reveal, String accessReason) {
        ResidentSensitiveInfo info = sensitiveInfoRepository.findByResidentId(residentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        String ssnDecrypted = encryptionUtils.decrypt(info.getSsnEncrypted());
        String mrnDecrypted = encryptionUtils.decrypt(info.getMedicalRecordNumberEncrypted());
        String bankDecrypted = encryptionUtils.decrypt(info.getBankAccountEncrypted());
        String insuranceDecrypted = encryptionUtils.decrypt(info.getPrimaryInsuranceIdEncrypted());

        if (reveal != null && reveal) {
            // Detailed PHI access log is required
            auditLoggingHelper.logPhiAccess("resident_sensitive_info", info.getId().toString(), "VIEW", 
                    accessReason != null ? accessReason : "Decrypted viewing requested");

            return ResidentSensitiveInfoResponse.builder()
                    .ssn(ssnDecrypted)
                    .medicalRecordNumber(mrnDecrypted)
                    .bankAccount(bankDecrypted)
                    .primaryInsuranceId(insuranceDecrypted)
                    .build();
        } else {
            return ResidentSensitiveInfoResponse.builder()
                    .ssnMasked(maskSsn(ssnDecrypted))
                    .medicalRecordNumberMasked(maskMrn(mrnDecrypted))
                    .bankAccount(maskBankAccount(bankDecrypted))
                    .primaryInsuranceId(insuranceDecrypted)
                    .build();
        }
    }

    @Transactional
    public Map<String, Object> createSensitiveInfo(Long residentId, ResidentSensitiveInfoCreateRequest request) {
        Resident resident = residentRepository.findById(residentId)
                .filter(r -> !r.getIsDeleted())
                .orElseThrow(() -> new AppException(ErrorCode.RESIDENT_NOT_FOUND));

        if (sensitiveInfoRepository.findByResidentId(residentId).isPresent()) {
            throw new AppException(ErrorCode.RESOURCE_ALREADY_EXISTS);
        }

        ResidentSensitiveInfo info = ResidentSensitiveInfo.builder()
                .resident(resident)
                .ssnEncrypted(encryptionUtils.encrypt(request.getSsn()))
                .medicalRecordNumberEncrypted(encryptionUtils.encrypt(request.getMedicalRecordNumber()))
                .bankAccountEncrypted(encryptionUtils.encrypt(request.getBankAccount()))
                .primaryInsuranceIdEncrypted(encryptionUtils.encrypt(request.getPrimaryInsuranceId()))
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        sensitiveInfoRepository.save(info);

        Map<String, Object> response = new HashMap<>();
        response.put("residentId", residentId);
        response.put("encrypted", true);
        return response;
    }

    @Transactional
    public Map<String, Object> updateSensitiveInfo(Long residentId, ResidentSensitiveInfoCreateRequest request) {
        ResidentSensitiveInfo info = sensitiveInfoRepository.findByResidentId(residentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        // Decrypt old values to determine changes
        String oldSsn = encryptionUtils.decrypt(info.getSsnEncrypted());
        String oldMrn = encryptionUtils.decrypt(info.getMedicalRecordNumberEncrypted());
        String oldBank = encryptionUtils.decrypt(info.getBankAccountEncrypted());
        String oldInsurance = encryptionUtils.decrypt(info.getPrimaryInsuranceIdEncrypted());

        List<String> changedFields = new ArrayList<>();
        if (request.getSsn() != null && !request.getSsn().equals(oldSsn)) {
            info.setSsnEncrypted(encryptionUtils.encrypt(request.getSsn()));
            changedFields.add("ssn");
        }
        if (request.getMedicalRecordNumber() != null && !request.getMedicalRecordNumber().equals(oldMrn)) {
            info.setMedicalRecordNumberEncrypted(encryptionUtils.encrypt(request.getMedicalRecordNumber()));
            changedFields.add("medicalRecordNumber");
        }
        if (request.getBankAccount() != null && !request.getBankAccount().equals(oldBank)) {
            info.setBankAccountEncrypted(encryptionUtils.encrypt(request.getBankAccount()));
            changedFields.add("bankAccount");
        }
        if (request.getPrimaryInsuranceId() != null && !request.getPrimaryInsuranceId().equals(oldInsurance)) {
            info.setPrimaryInsuranceIdEncrypted(encryptionUtils.encrypt(request.getPrimaryInsuranceId()));
            changedFields.add("primaryInsuranceId");
        }

        if (!changedFields.isEmpty()) {
            info.setUpdatedAt(OffsetDateTime.now());
            info = sensitiveInfoRepository.save(info);

            // Audit log changes (list fields, never plaintext values)
            auditLoggingHelper.logAudit("resident_sensitive_info", info.getId().toString(), "UPDATE", 
                    "Fields existed", "Changed fields: " + String.join(", ", changedFields));
        }

        Map<String, Object> sensitiveInfoMap = new HashMap<>();
        sensitiveInfoMap.put("id", info.getId());
        sensitiveInfoMap.put("residentId", residentId);

        Map<String, Object> response = new HashMap<>();
        response.put("sensitiveInfo", sensitiveInfoMap);
        return response;
    }

    @Transactional
    public boolean deleteSensitiveInfo(Long residentId) {
        ResidentSensitiveInfo info = sensitiveInfoRepository.findByResidentId(residentId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        sensitiveInfoRepository.delete(info);
        
        // Log deletion
        auditLoggingHelper.logAudit("resident_sensitive_info", info.getId().toString(), "DELETE", 
                "Record existed", "Record deleted");
        
        return true;
    }
}
