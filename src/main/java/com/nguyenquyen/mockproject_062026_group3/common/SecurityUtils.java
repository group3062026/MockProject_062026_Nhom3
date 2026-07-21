package com.nguyenquyen.mockproject_062026_group3.common;

import com.nguyenquyen.mockproject_062026_group3.entity.User;
import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
import com.nguyenquyen.mockproject_062026_group3.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;

@Component
public class SecurityUtils {

    @Autowired
    private UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !("anonymousUser".equals(authentication.getPrincipal()))) {
            String email = authentication.getName();
            return userRepository.findByEmailAndIsDeletedFalse(email)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String userIdHeader = request.getHeader("X-User-Id");
            if (userIdHeader != null && !userIdHeader.isEmpty()) {
                try {
                    Long userId = Long.parseLong(userIdHeader);
                    return userRepository.findById(userId)
                            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                } catch (NumberFormatException ignored) {}
            }
        }

        return userRepository.findAll().stream()
                .filter(u -> u.getIsDeleted() == null || !u.getIsDeleted())
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
    }

    /**
     * Checks if the current request has one of the allowed roles.
     * If no header X-User-Role is present, throws UNAUTHORIZED (401).
     * If the role in X-User-Role is not in allowedRoles, throws FORBIDDEN (403).
     */
    public void checkRoles(String... allowedRoles) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String userRole = request.getHeader("X-User-Role");
        if (userRole == null || userRole.isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (allowedRoles == null || allowedRoles.length == 0) {
            return;
        }

        List<String> allowedList = Arrays.asList(allowedRoles);
        if (allowedList.contains("ALL_AUTHENTICATED")) {
            return;
        }

        if (!allowedList.contains(userRole)) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }
    }
}
