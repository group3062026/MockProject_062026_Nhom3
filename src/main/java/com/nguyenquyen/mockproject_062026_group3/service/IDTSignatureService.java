package com.nguyenquyen.mockproject_062026_group3.service;

import com.nguyenquyen.mockproject_062026_group3.dto.request.IDTSignatureRequest;
import com.nguyenquyen.mockproject_062026_group3.dto.response.IDTSignatureResponse;

public interface IDTSignatureService {
    IDTSignatureResponse getIDTSignatures(Long carePlanId);
    IDTSignatureResponse.SignatureItem submitIDTSignature(Long carePlanId, IDTSignatureRequest request);
}
