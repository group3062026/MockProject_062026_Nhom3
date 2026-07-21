package com.nguyenquyen.mockproject_062026_group3.service.impl;

import com.nguyenquyen.mockproject_062026_group3.common.SecurityUtils;
import com.nguyenquyen.mockproject_062026_group3.dto.request.IDTSignatureRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.response.IDTSignatureResponse;
import com.nguyenquyen.mockproject_062026_group3.entity.CarePlan;
import com.nguyenquyen.mockproject_062026_group3.entity.IDTSignature;
import com.nguyenquyen.mockproject_062026_group3.entity.User;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.repository.CarePlanRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.IDTSignatureRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.UserRepository;
import com.nguyenquyen.mockproject_062026_group3.service.IDTSignatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
//sc-036
@Service
public class IDTSignatureServiceImpl implements IDTSignatureService {

    @Autowired
    private IDTSignatureRepository idtSignatureRepository;

    @Autowired
    private CarePlanRepository carePlanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityUtils securityUtils;

    public IDTSignatureResponse getIDTSignatures(Long carePlanId)  {
        carePlanRepository.findById(carePlanId)
                .orElseThrow(() -> new AppException(ErrorCode.CARE_PLAN_NOT_FOUND));

        List<IDTSignatureResponse.SignatureItem> items = idtSignatureRepository
                .findByCarePlanId(carePlanId)
                .stream()
                .map(sig -> IDTSignatureResponse.SignatureItem.builder()
                        .id(sig.getId())
                        .userId(sig.getUser().getId())
                        .userName(sig.getUser().getFirstName() + " " + sig.getUser().getLastName())
                        .role(sig.getUser().getRole().getRoleName())
                        .comments(sig.getComments())
                        .signedAt(sig.getSignedAt())
                        .build())
                .toList();

        return IDTSignatureResponse.builder().signatures(items).build();
    }

    public IDTSignatureResponse.SignatureItem submitIDTSignature(Long carePlanId, IDTSignatureRequest request) {
        CarePlan carePlan = carePlanRepository.findById(carePlanId)
                .orElseThrow(() -> new AppException(ErrorCode.CARE_PLAN_NOT_FOUND));

        User user = securityUtils.getCurrentUser();

        if (idtSignatureRepository.existsByCarePlanIdAndUserId(carePlanId, user.getId())) {
            throw new AppException(ErrorCode.RESOURCE_ALREADY_EXISTS);
        }

        IDTSignature signature = IDTSignature.builder()
                .carePlan(carePlan)
                .user(user)
                .comments(request.getComments())
                .build();

        IDTSignature saved = idtSignatureRepository.save(signature);

        return IDTSignatureResponse.SignatureItem.builder()
                .id(saved.getId())
                .userId(user.getId())
                .userName(user.getFirstName() + " " + user.getLastName())
                .role(user.getRole().getRoleName())
                .comments(saved.getComments())
                .signedAt(saved.getSignedAt())
                .build();
    }
}
