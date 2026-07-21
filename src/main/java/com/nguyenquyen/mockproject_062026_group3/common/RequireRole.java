package com.nguyenquyen.mockproject_062026_group3.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**

 * Annotation to identify roles that have access to the method

 * Used in conjunction with RoleCheckAspect to check permissions

 *
 * Example: @RequireRole({"ADMIN", "MANAGER"})

 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    String[] value() default {};
}

