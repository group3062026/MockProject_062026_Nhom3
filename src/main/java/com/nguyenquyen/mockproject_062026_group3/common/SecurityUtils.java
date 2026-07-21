  package com.nguyenquyen.mockproject_062026_group3.common;

  import com.nguyenquyen.mockproject_062026_group3.exception.AppException;
  import com.nguyenquyen.mockproject_062026_group3.exception.ErrorCode;
  import jakarta.servlet.http.HttpServletRequest;
  import org.springframework.stereotype.Component;
  import org.springframework.web.context.request.RequestContextHolder;
  import org.springframework.web.context.request.ServletRequestAttributes;

  import java.util.Arrays;
  import java.util.List;

  @Component
  public class SecurityUtils {

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
              // No roles specified means any authenticated user is allowed
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
