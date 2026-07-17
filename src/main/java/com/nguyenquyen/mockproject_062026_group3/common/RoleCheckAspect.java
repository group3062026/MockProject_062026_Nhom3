package com.nguyenquyen.mockproject_062026_group3.common;

import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * Aspect để kiểm tra quyền dựa vào role của user trong HTTP header
 * 
 * Chú ý: Trong thực tế, bạn nên lấy role từ Authentication object sau khi xác thực
 * Hiện tại nó lấy từ header "X-User-Role" cho mục đích demo
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RoleCheckAspect {

    /**
     * Kiểm tra quyền trước khi thực hiện method có annotation @RequireRole
     */
    @Before("@annotation(requireRole)")
    public void checkRole(JoinPoint joinPoint, RequireRole requireRole) {
        String[] allowedRoles = requireRole.value();
        
        if (allowedRoles.length == 0) {
            // Nếu không chỉ định role, cho phép truy cập
            return;
        }
        
        // Lấy user role từ request header (trong thực tế nên lấy từ Authentication)
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.warn("No HTTP request found in context");
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        
        HttpServletRequest request = attributes.getRequest();
        String userRole = request.getHeader("X-User-Role");
        
        // Kiểm tra xem user role có trong danh sách allowed roles không
        if (userRole == null || !Arrays.asList(allowedRoles).contains(userRole)) {
            log.warn("Access denied for role: {} to access {}. Required roles: {}",
                    userRole, joinPoint.getSignature().getName(), Arrays.toString(allowedRoles));
            throw new AppException(ErrorCode.FORBIDDEN);
        }
        
        log.debug("Access granted for role: {} to method: {}", userRole, joinPoint.getSignature().getName());
    }
}

