package com.nguyenquyen.mockproject_062026_group3.common;

import com.nguyenquyen.mockproject_062026_group3.entity.AuditLog;
import com.nguyenquyen.mockproject_062026_group3.entity.PhiAccessLog;
import com.nguyenquyen.mockproject_062026_group3.entity.User;
import com.nguyenquyen.mockproject_062026_group3.repository.AuditLogRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.PhiAccessLogRepository;
import com.nguyenquyen.mockproject_062026_group3.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.OffsetDateTime;

@Component
public class AuditLoggingHelper {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PhiAccessLogRepository phiAccessLogRepository;

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "127.0.0.1";
        }
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public User getCurrentUser() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            String userIdStr = request.getHeader("X-User-Id");
            if (userIdStr != null && !userIdStr.isEmpty()) {
                try {
                    Long userId = Long.parseLong(userIdStr);
                    return userRepository.findById(userId).orElse(null);
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
        }
        // Fallback: Return the first user in the DB (usually Admin seeded at ID 1)
        return userRepository.findAll().stream().findFirst().orElse(null);
    }

    public void logAudit(String tableName, String recordId, String action, String oldData, String newData) {
        User user = getCurrentUser();
        if (user == null) {
            return;
        }
        HttpServletRequest request = getCurrentRequest();
        String ipAddress = getClientIp(request);

        AuditLog auditLog = AuditLog.builder()
                .tableName(tableName)
                .recordId(recordId)
                .action(action)
                .oldData(oldData)
                .newData(newData)
                .performedBy(user)
                .performedAt(OffsetDateTime.now())
                .ipAddress(ipAddress)
                .build();

        auditLogRepository.save(auditLog);
    }

    public void logPhiAccess(String tableName, String recordId, String accessType, String accessReason) {
        User user = getCurrentUser();
        if (user == null) {
            return;
        }
        HttpServletRequest request = getCurrentRequest();
        String ipAddress = getClientIp(request);

        PhiAccessLog phiLog = PhiAccessLog.builder()
                .tableName(tableName)
                .recordId(recordId)
                .accessedBy(user)
                .accessType(accessType)
                .accessReason(accessReason != null ? accessReason : "Requested via API")
                .ipAddress(ipAddress)
                .accessedAt(OffsetDateTime.now())
                .build();

        phiAccessLogRepository.save(phiLog);
    }
}
