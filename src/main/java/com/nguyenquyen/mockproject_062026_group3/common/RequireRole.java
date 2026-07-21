package com.nguyenquyen.mockproject_062026_group3.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation để xác định các role có quyền truy cập vào method
 * Sử dụng kèm với RoleCheckAspect để kiểm tra quyền
 * 
 * Ví dụ: @RequireRole({"ADMIN", "MANAGER"})
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    String[] value() default {};
}

